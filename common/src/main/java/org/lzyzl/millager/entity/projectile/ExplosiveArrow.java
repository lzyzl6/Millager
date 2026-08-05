package org.lzyzl.millager.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.MillagerEntityTypes;
import org.lzyzl.millager.MillagerItems;

public class ExplosiveArrow extends AbstractArrow {

    private int fireTicks = 0;

    public ExplosiveArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }
    public ExplosiveArrow(Level level, LivingEntity livingEntity, ItemStack itemStack) {
        super(MillagerEntityTypes.Explosive_Arrow.get(), livingEntity, level);
        this.setFireTicks(0);
    }

    public ExplosiveArrow(Level level, double d, double e, double f, ItemStack itemStack) {
        super(MillagerEntityTypes.Explosive_Arrow.get(), d, e, f, level);
        this.setFireTicks(0);
    }

    @Override
    public void tick() {
        super.tick();
        if(this.isOnFire()) {
            int i = this.getFireTicks();
            if(i > 40) explodeArrow();
            else this.setFireTicks(i + 1);
        }
        if (this.level().isClientSide() && !this.inGround) {
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putInt("fireTicks", this.fireTicks);
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setFireTicks(valueInput.getInt("fireTicks"));
    }


    @Override
    protected void onHitEntity(@NonNull EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        entity.invulnerableTime = 0;
        explodeArrow();
    }

    @Override
    protected void onHitBlock(@NonNull BlockHitResult blockHitResult) {
        explodeArrow();
        if(this.level() instanceof ServerLevel level) {
            BlockPos pos = blockHitResult.getBlockPos();
            BlockState state = level.getBlockState(pos);
            if(state.getBlock() instanceof TntBlock) {
                level.removeBlock(pos, false);
                PrimedTnt tntEntity = new PrimedTnt(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, this.getOwner() instanceof LivingEntity living ? living : null);
                level.addFreshEntity(tntEntity);
                level.playSound(null, tntEntity.getX(), tntEntity.getY(), tntEntity.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
    }

    @Override
    protected @NonNull ItemStack getPickupItem() {
        return new ItemStack(MillagerItems.explosiveArrow.get());
    }

    private int getFireTicks() {
        return fireTicks;
    }

    private void setFireTicks(int fireTicks) {
        this.fireTicks = fireTicks;
    }

    private void explodeArrow() {
        if (!this.level().isClientSide()) {
            Vec3 pos = this.getRightExplosionPos();
            this.discard();
            this.level().explode(
                    this,
                    this.damageSources().explosion(this, this.getOwner()),
                    null,
                    pos.x, pos.y, pos.z,
                    1.0f,
                    false,
                    Level.ExplosionInteraction.NONE
            );
            this.level().broadcastEntityEvent(this, (byte)3);

        }
    }

    private Vec3 getRightExplosionPos() {
        Vec3 currentPos = this.position().add(this.getDeltaMovement().multiply(0.7,0.7,0.7));
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
                    bestDirection.getStepX() * 0.1,
                    bestDirection.getStepY() * 0.1,
                    bestDirection.getStepZ() * 0.1
            );
        }
        return currentPos;
    }
}
