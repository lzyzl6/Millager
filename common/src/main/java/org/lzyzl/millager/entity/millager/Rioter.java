package org.lzyzl.millager.entity.millager;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BannerPatternTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.entity.ai.millager.RioterShieldGoal;
import org.lzyzl.millager.entity.ai.millager.RioterTauntGoal;
import org.lzyzl.millager.entity.ai.millager.RioterThrowGoal;
import org.lzyzl.millager.entity.projectile.MolotovCocktail;
import org.lzyzl.millager.entity.projectile.TNTOnAStick;
import org.lzyzl.millager.util.MiscHelper;
import org.lzyzl.millager.util.ResourceVariantHelper;

import java.util.*;

import static org.lzyzl.millager.util.MiscHelper.isAllyCaused;

public class Rioter extends AbstractMillager implements RangedAttackMob {

    private static final EntityDataAccessor<Boolean> IS_TAUNTING = SynchedEntityData.defineId(Rioter.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_THROWING = SynchedEntityData.defineId(Rioter.class, EntityDataSerializers.BOOLEAN);

    private int strafeTick = 0;

    private int activityTicks = 0;
    private int throwCooldown = 0;
    private int tauntCooldown = 0;
    private int disableCooldown = 0;

    private static final List<DyeColor> SHIELD_COLORS = Arrays.stream(DyeColor.values())
            .filter(color -> color != DyeColor.YELLOW)
            .toList();

    public Rioter(EntityType<? extends AbstractMillager> entityType, Level level) {
        super(entityType, level);
        this.inventory = new SimpleContainer(7);
    }

    @Override
    public MillagerPose getMillagerPose() {
        if(this.isTaunting()) return MillagerPose.TAUNTING;
        if(this.isThrowing()) return MillagerPose.THROWING;
        if(this.isAggressive()) return MillagerPose.SHIELD;
        return MillagerPose.NEUTRAL;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractMillager.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.35F)
                .add(Attributes.ARMOR,5)
                .add(Attributes.ARMOR_TOUGHNESS, 12)
                .add(Attributes.FALL_DAMAGE_MULTIPLIER, 0.7D)
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new RioterTauntGoal(this));
        this.goalSelector.addGoal(1, new RioterThrowGoal(this));
        this.goalSelector.addGoal(2, new RioterShieldGoal(this));
        super.registerGoals();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        int activityTicks = this.getActivityTicks();
        int tauntCooldown = this.getTauntCooldown();
        int throwCooldown = this.getThrowCooldown();
        int disableCooldown = this.getDisableCooldown();
        if (tauntCooldown > 0) this.setTauntCooldown(tauntCooldown - 1);
        if (activityTicks > 0) this.setActivityTicks(activityTicks - 1);
        if (throwCooldown > 0) this.setThrowCooldown(throwCooldown - 1);
        if (disableCooldown > 0) this.setDisableCooldown(disableCooldown - 1);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NonNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_TAUNTING, false);
        builder.define(IS_THROWING, false);
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        this.writeInventoryToTag(valueOutput);
        valueOutput.putBoolean("isTaunting", this.isTaunting());
        valueOutput.putBoolean("isThrowing", this.isThrowing());
        valueOutput.putInt("activityTicks", this.getActivityTicks());
        valueOutput.putInt("throwCooldown", this.getThrowCooldown());
        valueOutput.putInt("tauntCooldown", this.getTauntCooldown());
        valueOutput.putInt("disableCooldown", this.getDisableCooldown());
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.readInventoryFromTag(valueInput);
        this.setTaunting(valueInput.getBooleanOr("isTaunting",false));
        this.setThrowing(valueInput.getBooleanOr("isThrowing",false));
        this.setActivityTicks(valueInput.getIntOr("activityTicks",0));
        this.setThrowCooldown(valueInput.getIntOr("throwCooldown",0));
        this.setTauntCooldown(valueInput.getIntOr("tauntCooldown",0));
        this.setDisableCooldown(valueInput.getIntOr("disableCooldown",0));
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NonNull ServerLevelAccessor level, @NonNull DifficultyInstance difficulty, @NonNull EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        spawnGroupData = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
        this.setItemSlot(EquipmentSlot.OFFHAND, getRandomDecoratedShield(random, level.registryAccess()));
        this.enchantSpawnedWeapon(level, level.getRandom(), difficulty);
        return spawnGroupData;
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NonNull RandomSource random, @NonNull DifficultyInstance difficulty) {
    }

    @Override
    protected void enchantSpawnedWeapon(@NonNull ServerLevelAccessor serverLevelAccessor, @NonNull RandomSource randomSource, @NonNull DifficultyInstance difficultyInstance) {
        super.enchantSpawnedWeapon(serverLevelAccessor, randomSource, difficultyInstance);
        if (randomSource.nextInt(400 - difficultyInstance.getDifficulty().getId() * 50) == 0) {
            ItemStack itemStack = this.getItemBySlot(EquipmentSlot.OFFHAND);
            if (itemStack.is(Items.SHIELD)) {
                EnchantmentHelper.enchantItemFromProvider(
                        itemStack, serverLevelAccessor.registryAccess(), VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, difficultyInstance, randomSource
                );
            }
        }
    }

    @Override
    public void performRangedAttack(@NonNull LivingEntity target, float f) {
        ItemStack itemStack = this.getItemBySlot(EquipmentSlot.MAINHAND);

        if (this.level() instanceof ServerLevel serverLevel) {

            double speed = 0.75;
            double dist = this.distanceTo(target);

            double predictionTicks = (dist / speed) + 1.5;

            Vec3 targetMovement = target.getDeltaMovement();

            double predX = target.getX() + targetMovement.x * predictionTicks;
            double predZ = target.getZ() + targetMovement.z * predictionTicks;

            double predY;
            if (!target.onGround() && Math.abs(targetMovement.y) > 0.01) {
                predY = target.getY() + (targetMovement.y * predictionTicks);
            } else {
                predY = target.getY();
            }

            double dx = predX - this.getX();
            double dz = predZ - this.getZ();
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);

            double dy = (predY + target.getBbHeight() * 0.3333) - (this.getY() + 1.5);

            float gravityCompensation = 0.15F;

            float inaccuracy = (float)(14 - serverLevel.getDifficulty().getId() * 3);

            Projectile.spawnProjectileUsingShoot(
                    itemStack.is(MillagerItems.tntOnAStick.asItem()) ? TNTOnAStick::new : MolotovCocktail::new,
                    serverLevel,
                    itemStack,
                    this,
                    dx,
                    dy + horizontalDist * gravityCompensation,
                    dz,
                    (float)speed,
                    inaccuracy
            );
        }
        if (!this.isSilent()) {
            this.level().playSound(null,
                    this.getX(), this.getY(), this.getZ(),
                    itemStack.is(MillagerItems.tntOnAStick.asItem()) ? SoundEvents.TNT_PRIMED : SoundEvents.SPLASH_POTION_THROW,
                    this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
        }
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel serverLevel, @NonNull DamageSource damageSource, float f) {
        if(isAllyCaused(damageSource)) return false;
        if (this.isAggressive()) {
            ItemStack itemStack = this.getItemBySlot(EquipmentSlot.OFFHAND);
            BlocksAttacks blocksAttacks = itemStack.get(DataComponents.BLOCKS_ATTACKS);
            if (blocksAttacks != null
                    && !blocksAttacks.bypassedBy().map(damageSource::is).orElse(false)
                    && !(damageSource.getDirectEntity() instanceof AbstractArrow arrow && arrow.getPierceLevel() > 0)) {
                Vec3 sourcePos = damageSource.getSourcePosition();
                double angle;
                if (sourcePos != null) {
                    Vec3 facing = this.calculateViewVector(0.0F, this.getYHeadRot());
                    Vec3 toSource = new Vec3(sourcePos.x - this.getX(), 0.0, sourcePos.z - this.getZ()).normalize();
                    angle = Math.acos(toSource.dot(facing));
                } else {
                    angle = Math.PI;
                }
                float blocked = blocksAttacks.resolveBlockedDamage(damageSource, f, angle);
                if (f - blocked <= 0.0F) {
                    blocksAttacks.onBlocked(serverLevel, this);
                    if (f > 8.0F + serverLevel.getDifficulty().getId()) {
                        this.setDisableCooldown(Math.min(200, (int) (80 + f * 10 - serverLevel.getDifficulty().getId() * 20)));
                        blocksAttacks.disable(serverLevel, this, this.getDisableCooldown(), itemStack);
                    }
                    return false;
                }
            }
        }
        return super.hurtServer(serverLevel, damageSource, f);
    }

    @Override
    protected boolean wantsItem(ItemStack itemStack) {
        return itemStack.is(Items.SPORE_BLOSSOM) || itemStack.is(MillagerItems.liquor.asItem());
    }

    public void updateTargetingAndDistance() {
        LivingEntity target = this.getTarget();

        if (target == null || !target.isAlive() || target.isRemoved()) {
            this.getNavigation().stop();
            return;
        }

        double distSqr = this.distanceToSqr(target);
        boolean hasLOS = this.getSensing().hasLineOfSight(target);

        double throwSpeed = 0.75;
        double predictionTicks = (Math.sqrt(distSqr) / throwSpeed) + 2.0;
        Vec3 targetMotion = target.getDeltaMovement();

        double predX = target.getX() + targetMotion.x * predictionTicks;
        double predZ = target.getZ() + targetMotion.z * predictionTicks;
        double predY = target.onGround() ? target.getEyeY() : target.getEyeY() + targetMotion.y * predictionTicks;

        this.getLookControl().setLookAt(predX, predY, predZ, 40.0F, 40.0F);

        double dx = predX - this.getX();
        double dz = predZ - this.getZ();
        float targetYaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;

        this.setYRot(MiscHelper.rotateTowards(this.getYRot(), targetYaw, 30.0F));
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();

        double idealSqr = 64.0;
        double toleranceSqr = 25.0;
        double attackRadiusSqr = 225.0;

        if (distSqr <= attackRadiusSqr && hasLOS) {
            this.getNavigation().stop();

            float forward = 0.0F;
            if (distSqr < idealSqr - toleranceSqr) {
                forward = -0.5F;
            } else if (distSqr > idealSqr + toleranceSqr) {
                forward = 0.5F;
            }

            float sideways = ((this.strafeTick / 20) % 2 == 0) ? 0.4F : -0.4F;
            this.strafeTick++;

            this.getMoveControl().strafe(forward, sideways);

        } else {
            double speedModifier = 1.0;
            this.getNavigation().moveTo(target, speedModifier);
            this.strafeTick = 0;
        }
    }

    private ItemStack getRandomDecoratedShield(RandomSource random, RegistryAccess registryAccess) {
        ItemStack shield = new ItemStack(Items.SHIELD);
        var patternRegistry = registryAccess.lookupOrThrow(Registries.BANNER_PATTERN);

        Optional<List<Holder<BannerPattern>>> patternHolders = patternRegistry
                .get(BannerPatternTags.NO_ITEM_REQUIRED)
                .map(named -> new ArrayList<>(named.stream().toList()));

        if (patternHolders.isEmpty() || patternHolders.get().isEmpty()) {
            return shield;
        }

        List<Holder<BannerPattern>> pool = patternHolders.get();
        Collections.shuffle(pool, new Random(random.nextLong()));

        BannerPatternLayers.Builder builder = new BannerPatternLayers.Builder();

        int randomLayerCount = Math.min(random.nextIntBetweenInclusive(3, 5), pool.size());

        for (int i = 0; i < randomLayerCount; i++) {
            DyeColor randomColor = SHIELD_COLORS.get(random.nextInt(SHIELD_COLORS.size()));
            builder.add(pool.get(i), randomColor);
        }

        patternRegistry.get(ResourceVariantHelper.VILLAGER).ifPresent(holder -> {
            DyeColor villagerLayerColor = SHIELD_COLORS.get(random.nextInt(SHIELD_COLORS.size()));
            builder.add(holder, villagerLayerColor);
        });

        shield.set(DataComponents.BASE_COLOR, DyeColor.YELLOW);

        shield.set(DataComponents.BANNER_PATTERNS, builder.build());

        return shield;
    }

    public int getDisableCooldown() {
        return this.disableCooldown;
    }

    public void setDisableCooldown(int disableCooldown) {
        this.disableCooldown = disableCooldown;
    }

    public int getTauntCooldown() {
        return this.tauntCooldown;
    }

    public void setTauntCooldown(int tauntCooldown) {
        this.tauntCooldown = tauntCooldown;
    }

    public int getThrowCooldown() {
        return this.throwCooldown;
    }

    public void setThrowCooldown(int throwCooldown) {
        this.throwCooldown = throwCooldown;
    }

    public int getActivityTicks() {
        return this.activityTicks;
    }

    public void setActivityTicks(int activityTicks) {
        this.activityTicks = activityTicks;
    }

    public boolean isThrowing() {
        return this.entityData.get(IS_THROWING);
    }

    public void setThrowing(boolean isThrowing) {
        this.entityData.set(IS_THROWING, isThrowing);
    }

    public boolean isTaunting() {
        return this.entityData.get(IS_TAUNTING);
    }

    public void setTaunting(boolean isTaunting) {this.entityData.set(IS_TAUNTING, isTaunting);}
}
