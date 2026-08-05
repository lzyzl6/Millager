package org.lzyzl.millager.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.MillagerEntityTypes;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.advancement.MillagerCriteria;
import org.lzyzl.millager.entity.millager.Rioter;

import java.util.List;

public class TNTOnAStick extends ThrowableItemProjectile implements RioterProjectile {

    private int fuse = 80;
    private float rotationProgress = 190;
    private float lastYRot = 0;
    private static final float ROT_SPEED = 30.0f;

    private boolean isRioterProjectile;

    public TNTOnAStick(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
        this.isRioterProjectile = this.getOwner() instanceof Rioter;
    }

    public TNTOnAStick(Level level, LivingEntity livingEntity, ItemStack itemStack) {
        super(MillagerEntityTypes.TNT_Projectile.get(), livingEntity, level);
        this.setItem(itemStack);
        this.isRioterProjectile = this.getOwner() instanceof Rioter;
        this.setFuse(80);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.onGround()) {
            this.rotationProgress = this.getRotationProgress(1.0F);
        }

        if (this.isInWaterOrRain()) {
            this.clearFire();
        }

        int i=this.getFuse();
        if(this.isInLava()) {
            i-=3;
            this.setFuse(i);
        } else if(this.isOnFire()) {
            i-=2;
            this.setFuse(i);
        } else {
            i-=1;
            this.setFuse(i);
        }

        if (i <= 0) {
            this.discard();
            if (!this.level().isClientSide()) {
                this.explode();
            }
        } else {
            this.updateInWaterStateAndDoFluidPushing();
            if (this.level().isClientSide()) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + (double)0.28F, this.getZ(), 0.0F, 0.0F, 0.0F);
            }
        }
    }

    @Override
    public boolean isRioterProjectile() {
        return this.isRioterProjectile;
    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putInt("fuse", this.fuse);
        valueOutput.putFloat("rotationProgress", this.rotationProgress);
        valueOutput.putFloat("lastYRot", this.lastYRot);
        valueOutput.putBoolean("isRioterProjectile", this.isRioterProjectile);
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setFuse(valueInput.contains("fuse") ? valueInput.getInt("fuse") : 80);
        this.rotationProgress = valueInput.contains("rotationProgress") ? valueInput.getFloat("rotationProgress") : 190.0F;
        this.lastYRot = valueInput.getFloat("lastYRot");
        this.isRioterProjectile = valueInput.getBoolean("isRioterProjectile");
    }

    @Override
    protected void onHitBlock(@NonNull BlockHitResult blockHitResult) {
        float currentPitch = this.xRotO;
        if(!this.onGround()) {
            float currentYaw = this.yRotO;
            this.lastYRot = currentYaw;
            this.setDeltaMovement(Vec3.ZERO);
            this.setXRot(currentPitch);
            this.setYRot(currentYaw);
            this.yRotO = currentYaw;
            this.xRotO = currentPitch;
        } else {
            this.setDeltaMovement(Vec3.ZERO);
            this.setXRot(currentPitch);
            this.setYRot(this.lastYRot);
            this.xRotO = currentPitch;
            this.yRotO =this.lastYRot;
        }
        super.onHitBlock(blockHitResult);
        this.setOnGround(true);

    }

    @Override
    protected void onHitEntity(@NonNull EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        if (!this.isRioterProjectile || (entity instanceof LivingEntity && !(entity instanceof ArmorStand))) {
            super.onHitEntity(entityHitResult);
            Level level = this.level();
            if(level instanceof ServerLevel) {
                entity.hurt(this.damageSources().thrown(this, this.getOwner()), 0);
            }
        }
        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.05, -0.05, -0.05));
    }

    @Override
    protected @NonNull Item getDefaultItem() {
        return MillagerItems.tntOnAStick.get();
    }

    public float getRotationProgress(float partialTick) {
        if(this.isFalling()) this.rotationProgress = approach360(this.rotationProgress, 1.0f);
        return !this.onGround() ? this.rotationProgress + this.getRotationDelta() * partialTick : this.rotationProgress;
    }

    private float getRotationDelta() {
        Vec3 movement = this.getDeltaMovement();
        float downwardSpeed = (float)Math.max(0.0D, -movement.y);
        float horizontalSpeed = (float)movement.horizontalDistance();
        float alignment = Mth.clamp(downwardSpeed / 0.3F, 0.0F, 1.0F);
        alignment = alignment * alignment * (3.0F - 2.0F * alignment);
        float verticalAlignment = Mth.clamp((0.2F - horizontalSpeed) / 0.1F, 0.0F, 1.0F);
        verticalAlignment = verticalAlignment * verticalAlignment * (3.0F - 2.0F * verticalAlignment);
        float fallingAlignment = Mth.clamp(downwardSpeed / 0.03F, 0.0F, 1.0F);
        fallingAlignment = fallingAlignment * fallingAlignment * (3.0F - 2.0F * fallingAlignment);
        verticalAlignment *= fallingAlignment;
        float targetRotation = -(float)Math.toDegrees(Mth.atan2(horizontalSpeed, downwardSpeed));
        targetRotation = Mth.lerp(verticalAlignment, targetRotation, 0.0F);
        alignment += verticalAlignment * (1.0F - alignment);
        float maxRotationStep = Mth.lerp(verticalAlignment,
                Mth.lerp(alignment, ROT_SPEED * 0.2F, ROT_SPEED * 0.4F), ROT_SPEED * 0.6F);
        float rotationDifference = Mth.wrapDegrees(targetRotation - this.rotationProgress);
        if (rotationDifference == -180.0F) rotationDifference = 180.0F;
        float alignmentDelta = Mth.clamp(rotationDifference * 0.25F, -maxRotationStep, maxRotationStep);
        return Mth.lerp(alignment, ROT_SPEED, alignmentDelta);
    }

    public float getLastYRot() {
        return this.lastYRot;
    }

    private int getFuse() {
        return fuse;
    }

    private void setFuse(int fuse) {
        this.fuse = fuse;
    }

    private void explode() {
        Level level = this.level();
        if(level instanceof ServerLevel serverLevel){
            Vec3 pos = this.getRightExplosionPos();
            float radius = this.isRioterProjectile ? 3.0f : 4.0f;
            List<LivingEntity> candidates = serverLevel.getEntitiesOfClass(LivingEntity.class,
                    new AABB(pos.x, pos.y, pos.z, pos.x, pos.y, pos.z).inflate(radius + 2.0),
                    entity -> entity instanceof Enemy && entity.isAlive());
            serverLevel.explode(
                    this,
                    this.damageSources().explosion(this, this.getOwner()),
                    null,
                    pos.x, pos.y, pos.z,
                    radius,
                    false,
                    this.isRioterProjectile ? Level.ExplosionInteraction.NONE : Level.ExplosionInteraction.TNT
            );
            if (this.getOwner() instanceof ServerPlayer shooter) {
                int kills = 0;
                for (LivingEntity entity : candidates) {
                    if (entity.isDeadOrDying()) kills++;
                }
                if (kills > 0) MillagerCriteria.TNT_KILL.get().trigger(shooter, kills);
            }
        }
    }

    private Vec3 getRightExplosionPos() {
        Vec3 currentPos = this.position();
        BlockPos bp = this.blockPosition();
        Level level = this.level();
        BlockState state = level.getBlockState(bp);

        if (state.isCollisionShapeFullBlock(level, bp)) {
            double minDistance = Double.MAX_VALUE;
            Direction bestDirection = Direction.UP;
            for (Direction dir : Direction.values()) {
                double axisPos = (dir.getAxis() == Direction.Axis.X) ? currentPos.x :
                        (dir.getAxis() == Direction.Axis.Y) ? currentPos.y : currentPos.z;
                double facePos = (dir.getAxisDirection() == Direction.AxisDirection.POSITIVE) ?
                        (Math.floor(axisPos) + 1.0) : Math.floor(axisPos);
                double dist = Math.abs(axisPos - facePos);
                if (level.getBlockState(bp.relative(dir)).isAir()) {
                    if (dist < minDistance) {
                        minDistance = dist;
                        bestDirection = dir;
                    }
                }
            }
            return currentPos.add(
                    bestDirection.getStepX() * 0.4,
                    bestDirection.getStepY() * 0.4,
                    bestDirection.getStepZ() * 0.4
            );
        }
        return currentPos.add(0, 0.05, 0);
    }

    public boolean isFalling() {
        BlockPos blockPos = this.blockPosition();
        BlockState blockState = this.level().getBlockState(blockPos.below());
        return this.onGround() && blockState.isAir();
    }

    private float approach360(float rotationProgress, float step) {
        float angleInCircle = rotationProgress % 360.0f;
        if (angleInCircle < 0) angleInCircle += 360.0f;

        if (angleInCircle > 0.1f && angleInCircle < 359.9f) {
            if (angleInCircle <= 180.0f) {
                rotationProgress -= Math.min(step, angleInCircle);
            } else {
                rotationProgress += Math.min(step, 360.0f - angleInCircle);
            }
        } else {
            rotationProgress = Math.round(rotationProgress / 360.0f) * 360.0f;
        }
        return rotationProgress;
    }

}
