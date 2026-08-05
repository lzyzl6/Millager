package org.lzyzl.millager.entity.ai.millager;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.lzyzl.millager.MillagerBlocks;
import org.lzyzl.millager.entity.millager.Doctor;
import org.lzyzl.millager.entity.golem.IronGolemAccessor;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static net.minecraft.world.level.block.Block.UPDATE_CLIENTS;

public class DoctorSpawnIronGoal extends Goal {

    private final Doctor doctor;
    private InteractionHand activeHand = InteractionHand.MAIN_HAND;
    private boolean shouldAngry = true;

    // 建造相关状态
    @Nullable
    private BlockPos structureOrigin;
    private Direction facing;
    private final List<BlockPos> placedPositions = new ArrayList<>();
    private int buildStage = 0;
    private int tickCounter = 0;
    private float healthAtStart;
    private boolean failedByBreak = false;

    private int golemScanCooldown = 0;
    private int cachedNearbyGolemCount = 0;

    // 配置
    private static final int TICKS_PER_STAGE = 8; // 放置间隔
    private static final int TOTAL_STAGES = 5; // 4个铁块 + 1个南瓜
    private static final int GOLEM_SCAN_INTERVAL = 100; // 每100tick扫描一次附近傀儡

    public DoctorSpawnIronGoal(Doctor doctor) {
        this.doctor = doctor;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.doctor.consumeCleanupRequest()) {
            forceCleanupOrphanedBlocks();
            this.shouldAngry = false;
            this.stop();
            this.doctor.setIronBuildCooldown(0);
            return false;
        }

        if (this.structureOrigin != null && this.buildStage > 0) return true;

        if (this.doctor.getIronGolemsBuilt() >= 3) return false;

        if (this.doctor.getIronBuildCooldown() > 0) return false;

        if (this.doctor.getTarget() != null) return false;

        if (this.golemScanCooldown > 0) {
            this.golemScanCooldown--;
        } else {
            this.golemScanCooldown = GOLEM_SCAN_INTERVAL;
            AABB area = this.doctor.getBoundingBox().inflate(64.0D);
            List<IronGolem> golems = this.doctor.level().getEntitiesOfClass(IronGolem.class, area);
            this.cachedNearbyGolemCount = golems.size();
        }

        if (this.cachedNearbyGolemCount > 2) return false;

        this.structureOrigin = findSuitableGround();
        return this.structureOrigin != null;
    }

    @Override
    public void start() {
        if (this.buildStage == 0) {
            this.placedPositions.clear();
            this.facing = this.doctor.getDirection();
        }

        this.tickCounter = 0;
        this.healthAtStart = this.doctor.getHealth();
        this.doctor.setBlockingGolem(true);
        this.activeHand = InteractionHand.MAIN_HAND;

        if(structureOrigin != null) {
            BlockPos center = structureOrigin.above();
            this.doctor.getLookControl().setLookAt(center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D, 30.0F, 30.0F);
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.doctor.isAlive() || this.doctor.getTarget() != null) return false;

        if (this.doctor.getHealth() < this.healthAtStart) return false;

        if (!checkBlocksIntegrity()) {
            this.failedByBreak = true;
            return false;
        }

        return this.buildStage < TOTAL_STAGES;
    }

    @Override
    public void tick() {
        if (structureOrigin == null) return;

        BlockPos centerPos = structureOrigin.above();
        this.doctor.getLookControl().setLookAt(centerPos.getX() + 0.5D, centerPos.getY() + 0.5D, centerPos.getZ() + 0.5D, 30.0F, 30.0F);
        this.doctor.getNavigation().stop();

        this.tickCounter++;
        if (this.tickCounter >= TICKS_PER_STAGE) {
            this.tickCounter = 0;
            placeNextBlock();
        }
    }

    @Override
    public void stop() {
        if (this.buildStage >= TOTAL_STAGES) {
            cleanupFakeBlocks();
            spawnGolem();
            this.doctor.setIronBuildCooldown(2400);
        } else {
            cleanupFakeBlocks();
            if(this.failedByBreak) this.doctor.setIronBuildCooldown(600);

            if(this.shouldAngry && (this.buildStage > 1 || this.failedByBreak)) {
                if(this.doctor.level() instanceof ServerLevel level) {
                    level.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.doctor.getX(), this.doctor.getY(), this.doctor.getZ(), 20,
                            0.5, 1, 0.5,
                            0.1);
                }
                this.doctor.playSound(SoundEvents.VILLAGER_NO);
            }
        }
        this.buildStage = 0;
        this.doctor.setBlockingGolem(false);
        this.activeHand = InteractionHand.MAIN_HAND;
        this.shouldAngry = true;
        this.failedByBreak = false;
        this.structureOrigin = null;
        this.placedPositions.clear();
    }

    private void forceCleanupOrphanedBlocks() {
        BlockPos origin = this.doctor.getPendingCleanupPos();
        if (origin == null) return;

        List<BlockPos> targets = new ArrayList<>();
        targets.add(origin.above());    // 腿
        targets.add(origin.above(2));   // 身
        targets.add(origin.above(3));   // 头

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            targets.add(origin.above(2).relative(dir));
        }

        for (BlockPos p : targets) {
            BlockState state = this.doctor.level().getBlockState(p);
            if (state.is(MillagerBlocks.FAKE_IRON_BLOCK.get()) || state.is(MillagerBlocks.FAKE_CARVED_PUMPKIN.get())) {
                this.doctor.level().setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_NEIGHBORS | UPDATE_CLIENTS);
                if (this.doctor.level() instanceof ServerLevel serverLevel) {
                    serverLevel.levelEvent(2001, p, Block.getId(state));
                }
            }
        }
    }

    @Nullable
    private BlockPos getPosForStage(int stage) {
        if (this.structureOrigin == null || this.facing == null) return null;

        return switch (stage) {
            case 0 -> structureOrigin.above();
            case 1 -> structureOrigin.above(2);
            case 2 -> structureOrigin.above(2).relative(this.facing.getClockWise()); // 右臂
            case 3 -> structureOrigin.above(2).relative(this.facing.getCounterClockWise()); // 左臂
            case 4 -> structureOrigin.above(3);
            default -> null;
        };
    }

    private void placeNextBlock() {
        BlockPos targetPos = getPosForStage(this.buildStage);
        if (targetPos == null) return;

        this.activeHand = (buildStage < 4) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        BlockState stateToPlace = (buildStage < 4) ? MillagerBlocks.FAKE_IRON_BLOCK.get().defaultBlockState() : MillagerBlocks.FAKE_CARVED_PUMPKIN.get().defaultBlockState();

        if (this.doctor.level().getBlockState(targetPos).isAir()) {
            this.doctor.level().setBlock(targetPos, stateToPlace, Block.UPDATE_NEIGHBORS | UPDATE_CLIENTS);
            this.placedPositions.add(targetPos);

            this.doctor.swing(this.activeHand);
            this.doctor.level().playSound(null, targetPos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        this.buildStage++;
    }

    private boolean checkBlocksIntegrity() {
        for (BlockPos pos : placedPositions) {
            if (this.doctor.level().getBlockState(pos).isAir()) {
                return false;
            }
        }
        return true;
    }

    private void cleanupFakeBlocks() {
        for (BlockPos pos : placedPositions) {

            BlockState state = this.doctor.level().getBlockState(pos);
            this.doctor.level().levelEvent(2001, pos, Block.getId(state));

            this.doctor.level().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_NEIGHBORS | UPDATE_CLIENTS);
        }
    }

    private void spawnGolem() {
        if (this.doctor.level() instanceof ServerLevel serverLevel && structureOrigin != null) {
            IronGolem golem = EntityType.IRON_GOLEM.create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
            if (golem != null) {
                golem.getNavigation().moveTo(doctor.getX(), doctor.getY(), doctor.getZ(), 1.0F);
                golem.setPos(structureOrigin.getX() + 0.5D, structureOrigin.getY() + 1.0D, structureOrigin.getZ() + 0.5D);
                serverLevel.addFreshEntity(golem);
                ((IronGolemAccessor)golem).millager$setDoctorCreated(true);
                this.doctor.setIronGolemsBuilt(this.doctor.getIronGolemsBuilt() + 1);
                this.cachedNearbyGolemCount++;
                this.golemScanCooldown = GOLEM_SCAN_INTERVAL;
            }
        }
    }

    @Nullable
    private BlockPos findSuitableGround() {
        for (int i = 2; i <= 3; i++) {
            BlockPos forwardPos = this.doctor.blockPosition().relative(this.doctor.getDirection(), i);

            for (int dy = 1; dy >= -2; dy--) {
                BlockPos checkPos = forwardPos.above(dy);
                if (isPosSuitableForBase(checkPos)) {
                    return checkPos;
                }
            }
        }
        return null;
    }

    private boolean isPosSuitableForBase(BlockPos pos) {

        if (!this.doctor.level().getBlockState(pos).isSolidRender()) return false;

        BlockPos up1 = pos.above();
        BlockPos up2 = pos.above(2);
        BlockPos up3 = pos.above(3);
        Direction cw = this.doctor.getDirection().getClockWise();
        Direction ccw = this.doctor.getDirection().getCounterClockWise();

        return this.doctor.level().getBlockState(up1).isAir() &&
                this.doctor.level().getBlockState(up2).isAir() &&
                this.doctor.level().getBlockState(up3).isAir() &&
                this.doctor.level().getBlockState(up2.relative(cw)).isAir() &&
                this.doctor.level().getBlockState(up2.relative(ccw)).isAir();
    }

    public @Nullable BlockPos getStructureOrigin() {
        return structureOrigin;
    }

    public int getBuildStage() {
        return buildStage;
    }

}
