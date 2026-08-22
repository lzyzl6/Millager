package org.lzyzl.millager.entity.golem;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.MillagerSounds;
import org.lzyzl.millager.entity.ai.golem.*;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.util.MillagerTargetingHelper;

import java.util.Objects;
import java.util.UUID;

public class BeeGolem extends AbstractGolem implements FlyingAnimal {
    private static final EntityDataAccessor<Integer> LIFE_TICKS = SynchedEntityData.defineId(BeeGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_ATTACKING = SynchedEntityData.defineId(BeeGolem.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> OWNER_UUID = SynchedEntityData.defineId(BeeGolem.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> IS_SUMMONED = SynchedEntityData.defineId(BeeGolem.class, EntityDataSerializers.BOOLEAN);

    public BeeGolem(EntityType<? extends AbstractGolem> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.getNavigation().setMaxVisitedNodesMultiplier(2.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 16.0F);
        this.setPathfindingMalus(PathType.FENCE, -1.0F);
    }

    @Override
    protected boolean isFlapping() {
        return this.isFlying();
    }

    @Override
    public boolean causeFallDamage(double d, float f, @NonNull DamageSource damageSource) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isAttacking() ? SoundEvents.BEE_LOOP_AGGRESSIVE : SoundEvents.BEE_LOOP;
    }

    @Override
    protected SoundEvent getHurtSound(@NonNull DamageSource damageSource) {
        return MillagerSounds.BEE_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BEE_DEATH;
    }

    @Override
    public void tick() {
        super.tick();
        this.setLifeTicks(this.getLifeTicks() + 1);
        if (!this.level().isClientSide()) {
            if (this.getTarget() != null && (!this.getTarget().isAlive() || !MillagerTargetingHelper.canBeeGolemAttack(this.getTarget()))) {
                this.setTarget(null);
            }
            if (this.getTarget() == null && this.isAttacking()) this.setAttacking(false);
        }
    }

    @Override
    protected @NonNull PathNavigation createNavigation(@NonNull Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level) {
            @Override
            public boolean isStableDestination(BlockPos blockPos) {
                return !this.level.getBlockState(blockPos.below()).isAir();
            }
        };
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setRequiredPathLength(48.0F);
        return nav;
    }

    @Override
    public void travel(@NonNull Vec3 vec3) {
        float f = (float)this.getAttributeValue(Attributes.FLYING_SPEED) * 5.0F / 3.0F;
        this.travelFlying(vec3, f, f, f);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(-1, new BeeGolemShutDownGoal(this));
        this.goalSelector.addGoal(0, new BeeGolemAttackGoal(this));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new BeeGolemWanderGoal(this));

        this.targetSelector.addGoal(1, new BeeGolemAvoidAllyGoal(this, true));
        this.targetSelector.addGoal(1, new BeeGolemNearestTargetGoal<>(this, Mob.class, 5, true, true, (entity, level) -> MillagerTargetingHelper.canBeeGolemAttack(entity)));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this, IronGolem.class, BeeGolem.class, AbstractMillager.class).setAlertOthers());
    }

    @Override
    public boolean canAttack(@NonNull LivingEntity target) {
        if (MillagerTargetingHelper.hasBeeGolemOverride(target)) return MillagerTargetingHelper.canBeeGolemAttack(target);
        return super.canAttack(target);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractGolem.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.FLYING_SPEED, 0.15)
                .add(Attributes.MOVEMENT_SPEED, 0.15)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, DamageSource source, float amount) {
        if(source.getDirectEntity() instanceof BeeGolem || source.getEntity() instanceof BeeGolem) return false;
        return super.hurtServer(level, source, amount);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NonNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LIFE_TICKS, 0);
        builder.define(IS_ATTACKING, false);
        builder.define(OWNER_UUID, "");
        builder.define(IS_SUMMONED, false);
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putInt("lifeTicks", this.getLifeTicks());
        valueOutput.putBoolean("isAttacking", this.isAttacking());
        valueOutput.putString("owner", this.getOwnerUUID());
        valueOutput.putBoolean("isSummoned", this.isSummoned());
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setLifeTicks(valueInput.getIntOr("lifeTicks", 0));
        this.setAttacking(valueInput.getBooleanOr("isAttacking", false));
        this.setOwnerUUID(valueInput.getStringOr("owner", ""));
        this.setSummoned(valueInput.getBooleanOr("isSummoned", false));
    }

    @Override
    public float getWalkTargetValue(@NonNull BlockPos blockPos, LevelReader levelReader) {
        return levelReader.getBlockState(blockPos).isAir() ? 10.0F : 0.0F;
    }

    public Player getOwner() {
        String uuid = this.getOwnerUUID();
        return !Objects.equals(uuid, "") ? level().getPlayerByUUID(UUID.fromString(uuid)) : null;

    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    public void setLifeTicks(int lifeTicks) {
        this.entityData.set(LIFE_TICKS, lifeTicks);
    }

    public int getLifeTicks() {
        return this.entityData.get(LIFE_TICKS);
    }

    public void setAttacking(boolean active) {
        this.entityData.set(IS_ATTACKING, active);
    }

    public boolean isAttacking() {
        return this.entityData.get(IS_ATTACKING);
    }

    public String getOwnerUUID() {
        return this.entityData.get(OWNER_UUID);
    }

    public void setOwnerUUID(String uuid) {
        this.entityData.set(OWNER_UUID, uuid);
    }

    public void setSummoned(boolean bl) {
        this.entityData.set(IS_SUMMONED, bl);
    }

    public boolean isSummoned() {
        return this.entityData.get(IS_SUMMONED);
    }
}
