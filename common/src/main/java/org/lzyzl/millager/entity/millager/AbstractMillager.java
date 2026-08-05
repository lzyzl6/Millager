package org.lzyzl.millager.entity.millager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.entity.ai.millager.CavalryPatrolGoal;
import org.lzyzl.millager.entity.ai.millager.InfantryPatrolGoal;
import org.lzyzl.millager.entity.ai.millager.MillagerDefendVillageGoal;
import org.lzyzl.millager.entity.ai.millager.RaidReinforcementGoal;
import org.lzyzl.millager.entity.ai.millager.RaidRetreatGoal;
import org.lzyzl.millager.entity.ai.vanilla.ExtendedDefendVillageTargetGoal;
import org.lzyzl.millager.entity.golem.BeeGolem;
import org.lzyzl.millager.util.MiscHelper;
import org.lzyzl.millager.util.MultigolemDetector;
import org.lzyzl.millager.util.VillageBannerHelper;
import org.lzyzl.millager.worldgen.MillagerStructures;

import java.util.Comparator;
import java.util.UUID;

public abstract class AbstractMillager extends PathfinderMob implements NeutralMob, InventoryCarrier {

    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private static final int SQUAD_LEADER_CHECK_INTERVAL = 60;
    private static final int SQUAD_LEADER_MISSING_SCANS = 3;
    private static final int SQUAD_TARGET_SHARE_INTERVAL = 20;
    private static final double SQUAD_LEADER_SEARCH_RADIUS = 48.0D;
    private static final double SQUAD_LEADER_VALID_DISTANCE_SQ = 256.0D;
    private static final int SQUAD_MERGE_CHECK_INTERVAL = 200;
    private static final double SQUAD_MERGE_RADIUS = 16.0D;
    private static final int TARGET_REACHABILITY_CHECK_INTERVAL = 100;
    public static final int MAX_SQUAD_SIZE = 5;
    protected SimpleContainer inventory;
    private @Nullable BlockPos patrolTarget;
    private long persistentAngerEndTime;
    private @Nullable EntityReference<LivingEntity> persistentAngerTarget;
    private @Nullable UUID professionOrderOwner;
    private boolean scoutingPOI = false;
    private @Nullable BlockPos raidReinforcementCenter;
    private @Nullable UUID squadId;
    private boolean squadLeader;
    private boolean squadLeaderBanner;
    private boolean raidReinforcementArrived;
    private int squadLeaderCheckTimer;
    private int squadLeaderMissingScans;
    private int squadTargetShareTimer;
    private int squadMergeCheckTimer;
    private int patrolAvoidanceTick = -2;
    protected AbstractMillager(EntityType<? extends AbstractMillager> entityType, Level level) {
        super(entityType, level);
        this.getNavigation().setCanOpenDoors(true);
        this.getNavigation().setCanFloat(true);
        this.setCanPickUpLoot(true);
        this.xpReward = 5;
    }

    @Override
    public void tick() {
        super.tick();
        this.updateSquadLeader();
        this.shareSquadTarget();
        this.updateSwingTime();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new RaidRetreatGoal(this));
        this.goalSelector.addGoal(0, new RaidReinforcementGoal(this));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new MillagerDefendVillageGoal(this, 1.05D, this instanceof Rider));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Creeper.class,
                6.0F, 1.3D, 1.5D, EntitySelector.LIVING_ENTITY_STILL_ALIVE));
        this.goalSelector.addGoal(4,
                this instanceof Rider ?
                        new CavalryPatrolGoal(this, 0.85D) : new InfantryPatrolGoal(this, 0.9D)
        );
        this.goalSelector.addGoal(4, new MoveBackToVillageGoal(this, 1.2d, true));
        this.goalSelector.addGoal(5, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new ExtendedDefendVillageTargetGoal(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this, AbstractMillager.class, BeeGolem.class, IronGolem.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, (livingEntity, serverLevel) -> {
            if (livingEntity instanceof Player player) {
                if (this.isProfessionOrderOwner(player)) return false;
                if (player.isCreative() || player.isSpectator()) return false;
                if (player.getLastHurtMob() instanceof AbstractMillager && this.tickCount - player.getLastHurtMobTimestamp() < 100 + serverLevel.getDifficulty().getId() * 40) {
                    this.setPersistentAngerTarget(EntityReference.of(player));
                    return true;
                }
                if (this.isAngryAt(player, serverLevel)) return true;
                ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
                if (head.is(MillagerItems.VILLAGER_HEAD.asItem())) {
                    serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY(), this.getZ(), 20,
                            0.5, 1, 0.5,
                            0.1);
                    this.playSound(SoundEvents.VILLAGER_NO, 1.2f, 1.0f);
                    return true;
                }
                if (player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE)) return false;
                if (player.getY() <= -20 && serverLevel.registryAccess().lookupOrThrow(Registries.STRUCTURE).get(MillagerStructures.STRONG_ROOM_KEY)
                        .map(holder -> serverLevel.structureManager().getStructureWithPieceAt(player.blockPosition(), holder.value()) != StructureStart.INVALID_START).orElse(false)) return true;
                if (head.is(MillagerItems.ILLAGER_HEAD.asItem())) {
                    return true;
                }
            }
            return false;
        }));
        this.targetSelector
                .addGoal(
                        3,
                        new NearestAttackableTargetGoal<>(
                                this, Mob.class, 5, false, false, (livingEntity, serverLevel) -> {
                            if (livingEntity.isUnderWater()) return false;
                            if(livingEntity instanceof IronGolem golem) return MultigolemDetector.isZombieGolem(golem);
                            if (this instanceof RangedAttackMob && livingEntity instanceof Creeper creeper && creeper.getTarget() instanceof AbstractMillager) return true;
                            return livingEntity instanceof Enemy && !(livingEntity instanceof Creeper);
                        }
                        )
                );
        this.targetSelector.addGoal(4, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.VILLAGER_AMBIENT;
    }

    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
            this.updatePersistentAnger((ServerLevel) this.level(), true);
            if ((this.tickCount + this.getId()) % TARGET_REACHABILITY_CHECK_INTERVAL == 0) {
                this.clearUnreachableTarget();
            }
        }
    }

    private void clearUnreachableTarget() {
        LivingEntity target = this.getTarget();
        if (target == null) return;
        if (!target.isAlive()) {
            this.setTarget(null);
            return;
        }
        Path path = MiscHelper.getMillagerNavigation(this).createPath(target, 0);
        if (path == null || !path.canReach()) this.setTarget(null);
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        this.addPersistentAngerSaveData(output);
        if (this.professionOrderOwner != null) {
            output.store("professionOrderOwner", UUIDUtil.CODEC, this.professionOrderOwner);
        }
        if (this.raidReinforcementCenter != null) {
            output.store("raidReinforcementCenter", BlockPos.CODEC, this.raidReinforcementCenter);
        }
        if (this.squadId != null) {
            output.store("squadId", UUIDUtil.CODEC, this.squadId);
        }
        output.putBoolean("squadLeader", this.squadLeader);
        output.putBoolean("squadLeaderBanner", this.squadLeaderBanner);
        output.putBoolean("raidReinforcementArrived", this.raidReinforcementArrived);
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        this.readPersistentAngerSaveData(this.level(), input);
        this.professionOrderOwner = input.read("professionOrderOwner", UUIDUtil.CODEC).orElse(null);
        this.raidReinforcementCenter = input.read("raidReinforcementCenter", BlockPos.CODEC).orElse(null);
        this.squadId = input.read("squadId", UUIDUtil.CODEC)
                .or(() -> input.read("raidReinforcementSquadId", UUIDUtil.CODEC)).orElse(null);
        this.squadLeader = input.getBooleanOr("squadLeader",
                input.getBooleanOr("raidReinforcementLeader", false));
        this.squadLeaderBanner = input.getBooleanOr("squadLeaderBanner", false);
        this.raidReinforcementArrived = input.getBooleanOr("raidReinforcementArrived", false);
    }

    @Override
    protected void pickUpItem(@NonNull ServerLevel serverLevel, ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        if (this.wantsItem(itemStack)) {
            this.onItemPickup(itemEntity);
            ItemStack itemStack2 = this.inventory.addItem(itemStack);
            if (itemStack2.isEmpty()) {
                itemEntity.discard();
            } else {
                itemStack.setCount(itemStack2.getCount());
            }
        }
    }

    protected abstract boolean wantsItem(ItemStack itemStack);

    @Override
    public boolean shouldDropExperience() {
        return true;
    }

    @Override
    protected void dropEquipment(@NonNull ServerLevel serverLevel) {
        super.dropEquipment(serverLevel);
        this.inventory.removeAllItems().forEach((itemStack) -> this.spawnAtLocation(serverLevel, itemStack));
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(@NonNull DamageSource damageSource) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Override
    public long getPersistentAngerEndTime() {
        return this.persistentAngerEndTime;
    }

    @Override
    public void setPersistentAngerEndTime(long l) {
        this.persistentAngerEndTime = l;
    }

    @Nullable
    @Override
    public EntityReference<LivingEntity> getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable EntityReference<LivingEntity> entityReference) {
        this.persistentAngerTarget = entityReference;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setTimeToRemainAngry(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    public boolean isProfessionOrderOwner(Entity other) {
        return this.professionOrderOwner != null && this.professionOrderOwner.equals(other.getUUID());
    }

    public void setProfessionOrderOwner(UUID owner) {
        this.professionOrderOwner = owner;
    }

    @Override
    public @NonNull SimpleContainer getInventory() {
        return this.inventory;
    }

    public @Nullable BlockPos getPatrolTarget() {
        return this.patrolTarget;
    }

    public void setPatrolTarget(@Nullable BlockPos pos) {
        this.patrolTarget = pos;
    }

    public @Nullable BlockPos getRaidReinforcementCenter() {
        return this.raidReinforcementCenter;
    }

    public void setRaidReinforcementCenter(@Nullable BlockPos center) {
        this.raidReinforcementCenter = center;
        this.raidReinforcementArrived = false;
    }

    public boolean isRaidReinforcementArrived() {
        return this.raidReinforcementArrived;
    }

    public void setRaidReinforcementArrived(boolean arrived) {
        this.raidReinforcementArrived = arrived;
    }

    public @Nullable UUID getSquadId() {
        return this.squadId;
    }

    public boolean isSquadLeader() {
        return this.squadLeader;
    }

    public void setSquad(UUID squadId, boolean leader) {
        this.squadId = squadId;
        this.squadLeader = leader;
    }

    public void clearSquad() {
        this.squadId = null;
        if (this.squadLeaderBanner) this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        this.squadLeader = false;
        this.squadLeaderBanner = false;
    }

    public void promoteToSquadLeader() {
        this.squadLeader = true;
    }

    public void equipSquadLeaderBanner() {
        this.squadLeaderBanner = true;
        this.setItemSlot(EquipmentSlot.HEAD, VillageBannerHelper.create(
                this.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)));
    }

    public @Nullable AbstractMillager findNearbySquadLeader() {
        UUID squadId = this.squadId;
        if (squadId == null) return null;
        if (this.squadLeader) return this;
        return this.level().getEntitiesOfClass(AbstractMillager.class,
                        this.getBoundingBox().inflate(SQUAD_LEADER_SEARCH_RADIUS),
                        entity -> entity.isAlive() && entity.isSquadLeader() && squadId.equals(entity.getSquadId()))
                .stream().min(Comparator.comparing(AbstractMillager::getUUID)).orElse(null);
    }

    public boolean hasNearbySquadCapacity() {
        UUID squadId = this.squadId;
        return squadId != null && this.level().getEntitiesOfClass(AbstractMillager.class,
                this.getBoundingBox().inflate(SQUAD_LEADER_SEARCH_RADIUS),
                entity -> squadId.equals(entity.getSquadId())).size() < MAX_SQUAD_SIZE;
    }

    public void markPatrolAvoidanceActive() {
        this.patrolAvoidanceTick = this.tickCount;
    }

    public boolean shouldAvoidAllies() {
        return this.tickCount - this.patrolAvoidanceTick > 1;
    }

    private void updateSquadLeader() {
        UUID squadId = this.squadId;
        if (this.level().isClientSide() || squadId == null) {
            this.squadLeaderCheckTimer = 0;
            this.squadLeaderMissingScans = 0;
            return;
        }
        if (this.squadLeader) {
            if (++this.squadLeaderCheckTimer >= SQUAD_LEADER_CHECK_INTERVAL) {
                this.squadLeaderCheckTimer = 0;
                if (!this.hasSquadMember(squadId)) {
                    this.clearSquad();
                    return;
                }
            }
            if (++this.squadMergeCheckTimer >= SQUAD_MERGE_CHECK_INTERVAL) {
                this.squadMergeCheckTimer = 0;
                this.mergeNearbySquad();
            }
            return;
        }
        this.squadMergeCheckTimer = 0;
        if (++this.squadLeaderCheckTimer < SQUAD_LEADER_CHECK_INTERVAL) return;
        this.squadLeaderCheckTimer = 0;
        AbstractMillager replacement = this;
        AbstractMillager invalidLeader = null;
        AbstractMillager otherLeader = null;
        for (AbstractMillager candidate : this.level().getEntitiesOfClass(
                AbstractMillager.class,
                this.getBoundingBox().inflate(SQUAD_LEADER_SEARCH_RADIUS),
                AbstractMillager::isAlive)) {
            UUID candidateSquadId = candidate.getSquadId();
            if (squadId.equals(candidateSquadId)) {
                if (candidate.isSquadLeader()) {
                    if (this.isValidSquadLeader(candidate)) {
                        this.squadLeaderMissingScans = 0;
                        return;
                    }
                    invalidLeader = candidate;
                } else if (this.isValidSquadLeader(candidate)
                        && candidate.getUUID().compareTo(replacement.getUUID()) < 0) {
                    replacement = candidate;
                }
            } else if (candidateSquadId != null && candidate.isSquadLeader()
                    && this.hasCompatibleSquadCenter(candidate)
                    && (otherLeader == null || candidate.getUUID().compareTo(otherLeader.getUUID()) < 0)) {
                otherLeader = candidate;
            }
        }
        if (++this.squadLeaderMissingScans < SQUAD_LEADER_MISSING_SCANS) return;
        if (otherLeader != null) {
            this.setSquad(otherLeader.getSquadId(), false);
            this.squadLeaderMissingScans = 0;
        } else if (replacement == this) {
            if (invalidLeader != null) invalidLeader.clearSquad();
            this.setSquad(squadId, false);
            this.promoteToSquadLeader();
            this.squadLeaderMissingScans = 0;
        }
    }

    private boolean isValidSquadLeader(AbstractMillager candidate) {
        if (this.distanceToSqr(candidate) > SQUAD_LEADER_VALID_DISTANCE_SQ) return false;
        var path = this.getNavigation().createPath(candidate.blockPosition(), 0);
        return path != null && path.canReach();
    }

    private boolean hasSquadMember(UUID squadId) {
        return !this.level().getEntitiesOfClass(AbstractMillager.class,
                this.getBoundingBox().inflate(SQUAD_LEADER_SEARCH_RADIUS), candidate -> candidate != this
                        && candidate.isAlive() && squadId.equals(candidate.getSquadId())).isEmpty();
    }

    private void shareSquadTarget() {
        if (++this.squadTargetShareTimer < SQUAD_TARGET_SHARE_INTERVAL) return;
        this.squadTargetShareTimer = 0;
        UUID squadId = this.squadId;
        LivingEntity target = this.getTarget();
        if (this.level().isClientSide() || squadId == null || target == null || !target.isAlive()) return;
        for (AbstractMillager member : this.level().getEntitiesOfClass(AbstractMillager.class,
                this.getBoundingBox().inflate(SQUAD_LEADER_SEARCH_RADIUS), member -> member != this
                        && squadId.equals(member.getSquadId()) && member.getTarget() == null)) {
            member.setTarget(target);
        }
    }

    private void mergeNearbySquad() {
        UUID id = this.squadId;
        if (id == null) return;
        AbstractMillager other = this.level().getEntitiesOfClass(AbstractMillager.class, this.getBoundingBox().inflate(SQUAD_MERGE_RADIUS), e -> e != this && e.isAlive() && e.isSquadLeader() && !id.equals(e.getSquadId()) && this.hasMatchingSquadCenter(e)).stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
        if (other == null || other.getSquadId() == null) return;
        UUID otherId = other.getSquadId();
        var members = this.level().getEntitiesOfClass(AbstractMillager.class, this.getBoundingBox().inflate(SQUAD_LEADER_SEARCH_RADIUS), e -> id.equals(e.getSquadId()) || otherId.equals(e.getSquadId()));
        if (members.size() > MAX_SQUAD_SIZE) return;
        AbstractMillager leader = this.squadLeaderBanner != other.squadLeaderBanner ? this.squadLeaderBanner ? this : other
                : this.getHealth() > other.getHealth() || this.getHealth() == other.getHealth() && this.getUUID().compareTo(other.getUUID()) < 0 ? this : other;
        UUID leaderId = leader.getSquadId();
        if (leaderId == null) return;
        if (leader != this) this.clearSquad();
        if (leader != other) other.clearSquad();
        for (AbstractMillager member : members) member.setSquad(leaderId, false);
        leader.promoteToSquadLeader();
    }

    private boolean hasMatchingSquadCenter(AbstractMillager other) { return this.raidReinforcementCenter == null ? other.getRaidReinforcementCenter() == null : this.raidReinforcementCenter.equals(other.getRaidReinforcementCenter()); }

    private boolean hasCompatibleSquadCenter(AbstractMillager candidate) {
        BlockPos center = this.raidReinforcementCenter;
        BlockPos candidateCenter = candidate.getRaidReinforcementCenter();
        return center != null && center.equals(candidateCenter);
    }

    public boolean isScoutingPOI() {
        return scoutingPOI;
    }

    public void setScoutingPOI(boolean scouting) {
        this.scoutingPOI = scouting;
    }

    @Override
    public void die(@NonNull DamageSource damageSource) {
        if (this instanceof Rider && this.isPassenger()
                && this.getVehicle() instanceof Horse horse) {
            horse.addTag("millager_fast_despawn");
        }
        super.die(damageSource);
    }

    public abstract MillagerPose getMillagerPose();

    public enum MillagerPose {
        NEUTRAL,
        ATTACKING,
        BASHING,
        //Archer
        BOW_AND_ARROW,
        CRAFTING_ARROW,
        //Lancer
        SPEAR,
        SPELLCASTING,
        //Doctor
        HOLDING_ITEM,
        SWINGING_ARM,
        SUMMONING,
        //Swordmaster,
        SWORD_SHIELDING,
        REGAINING_SWORD,
        APPROACHING,
        //Mauler
        FIXING,
        //Rioter
        SHIELD,
        THROWING,
        TAUNTING,
        //Scouter
        TOOT_HORN,
        CROSSBOW_HOLD,
        CROSSBOW_CHARGE
    }
}
