package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.raid.Raid;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.util.MiscHelper;

import java.util.EnumSet;

import static org.lzyzl.millager.Millager.MOD_ID;

public class RaidReinforcementGoal extends Goal {

    private static final Identifier SPEED_ID = Identifier.fromNamespaceAndPath(MOD_ID, "raid_reinforcement_speed");
    private static final double ARRIVAL_DISTANCE = 14.0D;

    private final AbstractMillager millager;
    private @Nullable LivingEntity acceleratedEntity;
    private @Nullable BlockPos destination;
    private int navigationRefreshTimer;

    public RaidReinforcementGoal(AbstractMillager millager) {
        this.millager = millager;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        BlockPos center = this.millager.getRaidReinforcementCenter();
        if (center == null || !(this.millager.level() instanceof ServerLevel serverLevel)) return false;
        Raid raid = serverLevel.getRaidAt(center);
        return raid != null && raid.isActive() && !raid.isOver() && !raid.isStopped()
                && this.millager.getTarget() == null
                && this.needsToReach(center);
    }

    @Override
    public boolean canContinueToUse() {
        BlockPos center = this.millager.getRaidReinforcementCenter();
        if (center == null || !this.millager.isAlive()) return false;
        if (this.millager.getTarget() != null) return false;
        if (!(this.millager.level() instanceof ServerLevel serverLevel)) return false;
        Raid raid = serverLevel.getRaidAt(center);
        return raid != null && raid.isActive() && !raid.isOver()
                && this.needsToReach(center);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.updateSpeedModifier();
        this.navigationRefreshTimer = 0;
        this.destination = this.findDestination();
        this.moveToDestination();
    }

    @Override
    public void tick() {
        this.updateSpeedModifier();
        BlockPos center = this.millager.getRaidReinforcementCenter();
        if (center == null) return;
        AbstractMillager leader = this.findSquadLeader();
        boolean followingLeader = leader != null && leader != this.millager;
        this.navigationRefreshTimer++;
        boolean needsDestination = this.destination == null
                ? this.navigationRefreshTimer >= 20
                : MiscHelper.getMillagerNavigation(this.millager).isDone()
                || followingLeader && this.navigationRefreshTimer >= 10;
        if (needsDestination) {
            this.navigationRefreshTimer = 0;
            this.destination = this.findDestination();
            this.moveToDestination();
        }
    }

    @Override
    public void stop() {
        this.removeSpeedModifier();
        this.destination = null;
        this.navigationRefreshTimer = 0;
        MiscHelper.getMillagerNavigation(this.millager).stop();
    }

    private boolean needsToReach(BlockPos center) {
        if (this.millager.isRaidReinforcementArrived()) return false;
        AbstractMillager leader = this.findSquadLeader();
        boolean arrived = this.millager.blockPosition().closerThan(center, ARRIVAL_DISTANCE)
                || leader != null && leader != this.millager
                && leader.blockPosition().closerThan(center, ARRIVAL_DISTANCE)
                && this.millager.blockPosition().closerThan(leader.blockPosition(), 6.0D);
        if (arrived) this.millager.setRaidReinforcementArrived(true);
        return !arrived;
    }

    private AbstractMillager findSquadLeader() {
        return this.millager.findNearbySquadLeader();
    }

    private @Nullable BlockPos findDestination() {
        AbstractMillager leader = this.findSquadLeader();
        if (leader != null && leader != this.millager) return leader.blockPosition();
        return this.millager.getRaidReinforcementCenter();
    }

    private void updateSpeedModifier() {
        this.acceleratedEntity = MiscHelper.updateMillagerSpeedModifier(this.millager, this.acceleratedEntity, SPEED_ID);
    }

    private void removeSpeedModifier() {
        MiscHelper.removeMillagerSpeedModifier(this.acceleratedEntity, SPEED_ID);
        this.acceleratedEntity = null;
    }

    private void moveToDestination() {
        if (this.destination == null) return;
        MiscHelper.getMillagerNavigation(this.millager).moveTo(
                this.destination.getX() + 0.5D,
                this.destination.getY(),
                this.destination.getZ() + 0.5D,
                1.15D
        );
    }
}
