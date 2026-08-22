package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Path;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.MillagerGameRules;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.util.MiscHelper;

import java.util.EnumSet;

import static org.lzyzl.millager.Millager.MOD_ID;

public class RaidRetreatGoal extends Goal {

    private static final ResourceLocation SPEED_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "raid_retreat_speed");
    private static final double RETREAT_DISTANCE = 32.0D;
    private static final double EXIT_DISTANCE = 32.0D;
    private static final int RETREAT_TIMEOUT_TICKS = 300;
    private static final int[][] EXIT_DIRECTIONS = {
            {1, 0}, {1, 1}, {0, 1}, {-1, 1},
            {-1, 0}, {-1, -1}, {0, -1}, {1, -1}
    };

    private final AbstractMillager millager;
    private @Nullable LivingEntity acceleratedEntity;
    private @Nullable BlockPos destination;
    private int retreatTicks;

    public RaidRetreatGoal(AbstractMillager millager) {
        this.millager = millager;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        BlockPos center = this.millager.getRaidReinforcementCenter();
        if (center == null || !(this.millager.level() instanceof ServerLevel serverLevel)) return false;
        if (this.millager.getGoetyRaidOwner() != null) return false;
        if (!serverLevel.getGameRules().getRule(MillagerGameRules.RAID_DEFENDERS_RETREAT).get()) return false;
        Raid raid = serverLevel.getRaidAt(center);
        return raid == null || raid.isOver() || raid.isStopped();
    }

    @Override
    public boolean canContinueToUse() {
        return this.millager.isAlive()
                && this.millager.getRaidReinforcementCenter() != null
                && this.millager.level() instanceof ServerLevel serverLevel
                && serverLevel.getGameRules().getRule(MillagerGameRules.RAID_DEFENDERS_RETREAT).get();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.retreatTicks = 0;
        this.clearCombat();
        this.updateSpeedModifier();
        this.moveToNearestExit();
    }

    @Override
    public void tick() {
        this.clearCombat();
        this.updateSpeedModifier();
        BlockPos center = this.millager.getRaidReinforcementCenter();
        if (center == null) return;
        this.retreatTicks++;
        if (this.destination == null
                || this.retreatTicks >= RETREAT_TIMEOUT_TICKS
                || this.millager.blockPosition().distSqr(center) >= RETREAT_DISTANCE * RETREAT_DISTANCE) {
            this.disappear();
        }
    }

    @Override
    public void stop() {
        this.removeSpeedModifier();
        this.destination = null;
        MiscHelper.getMillagerNavigation(this.millager).stop();
    }

    private void clearCombat() {
        this.millager.setTarget(null);
        this.millager.setAggressive(false);
    }

    private void updateSpeedModifier() {
        this.acceleratedEntity = MiscHelper.updateMillagerSpeedModifier(this.millager, this.acceleratedEntity, SPEED_ID);
    }

    private void removeSpeedModifier() {
        MiscHelper.removeMillagerSpeedModifier(this.acceleratedEntity, SPEED_ID);
        this.acceleratedEntity = null;
    }

    private void moveToNearestExit() {
        BlockPos center = this.millager.getRaidReinforcementCenter();
        if (center == null || !(this.millager.level() instanceof ServerLevel serverLevel)) return;
        PathNavigation navigation = MiscHelper.getMillagerNavigation(this.millager);
        LivingEntity mover = MiscHelper.getMillagerMover(this.millager);
        AttributeInstance followRange = mover.getAttribute(Attributes.FOLLOW_RANGE);
        double oldBaseValue = followRange == null ? 0.0D : followRange.getBaseValue();
        if (followRange != null && followRange.getValue() < EXIT_DISTANCE + 16.0D) {
            followRange.setBaseValue(oldBaseValue + EXIT_DISTANCE + 16.0D - followRange.getValue());
        }
        try {
            boolean[] checked = new boolean[EXIT_DIRECTIONS.length];
            for (int attempt = 0; attempt < EXIT_DIRECTIONS.length; attempt++) {
                int nearest = -1;
                double nearestDistance = Double.MAX_VALUE;
                for (int i = 0; i < EXIT_DIRECTIONS.length; i++) {
                    if (checked[i]) continue;
                    int[] direction = EXIT_DIRECTIONS[i];
                    double length = Math.sqrt(direction[0] * direction[0] + direction[1] * direction[1]);
                    double targetX = center.getX() + direction[0] * EXIT_DISTANCE / length;
                    double targetZ = center.getZ() + direction[1] * EXIT_DISTANCE / length;
                    double deltaX = targetX - this.millager.getX();
                    double deltaZ = targetZ - this.millager.getZ();
                    double distance = deltaX * deltaX + deltaZ * deltaZ;
                    if (distance >= nearestDistance) continue;
                    nearest = i;
                    nearestDistance = distance;
                }
                checked[nearest] = true;
                int[] direction = EXIT_DIRECTIONS[nearest];
                double length = Math.sqrt(direction[0] * direction[0] + direction[1] * direction[1]);
                int targetX = center.getX() + (int) Math.round(direction[0] * EXIT_DISTANCE / length);
                int targetZ = center.getZ() + (int) Math.round(direction[1] * EXIT_DISTANCE / length);
                int targetY = serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetX, targetZ);
                BlockPos candidate = new BlockPos(targetX, targetY, targetZ);
                if (!serverLevel.getFluidState(candidate).isEmpty()) continue;
                Path path = navigation.createPath(candidate, 0);
                if (!this.isSafeExitPath(path)) continue;
                this.destination = candidate;
                navigation.moveTo(path, 1.15D);
                return;
            }
        } finally {
            if (followRange != null) followRange.setBaseValue(oldBaseValue);
        }
        this.destination = null;
    }

    private boolean isSafeExitPath(Path path) {
        if (path == null || !path.canReach()) return false;
        Entity mover = MiscHelper.getMillagerMover(this.millager);
        int previousY = mover.blockPosition().getY();
        for (int i = 0; i < path.getNodeCount(); i++) {
            BlockPos pos = path.getNodePos(i);
            if (!this.millager.level().getFluidState(pos).isEmpty()
                    || Math.abs(pos.getY() - previousY) > 1) return false;
            previousY = pos.getY();
        }
        return true;
    }

    private void disappear() {
        if (this.millager.level() instanceof ServerLevel serverLevel) {
            serverLevel.broadcastEntityEvent(this.millager, (byte) 60);
        }
        Entity vehicle = this.millager.getVehicle();
        this.millager.stopRiding();
        this.millager.discard();
        if (vehicle instanceof Horse) vehicle.discard();
    }
}
