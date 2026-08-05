package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.phys.Vec3;
import org.lzyzl.millager.entity.millager.AbstractMillager;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class MillagerDefendVillageGoal extends Goal {

    private static final int POI_SEARCH_RADIUS = 48;
    private static final int MAX_VISITED = 3;
    private static final double ARRIVED_DIST = 1.5;
    private static final int STUCK_TIMEOUT = 200;

    private final AbstractMillager millager;
    private final double speedModifier;
    private final boolean avoidIndoors;
    private final List<BlockPos> visited = new ArrayList<>();
    private BlockPos targetPoi;
    private boolean stuck;
    private int stuckTimer;

    public MillagerDefendVillageGoal(AbstractMillager millager, double speedModifier, boolean avoidIndoors) {
        this.millager = millager;
        this.speedModifier = speedModifier;
        this.avoidIndoors = avoidIndoors;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.millager.getTarget() != null) return false;
        if (this.millager.level() instanceof ServerLevel serverLevel && isNoRaid(serverLevel)) return false;
        this.stuck = false;
        this.stuckTimer = 0;
        return findPoi();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.millager.getTarget() != null) return false;
        if (this.millager.level() instanceof ServerLevel serverLevel && isNoRaid(serverLevel)) return false;
        return !this.stuck;
    }

    @Override
    public void start() {
        if (this.targetPoi != null) {
            this.millager.getNavigation().moveTo(
                    this.targetPoi.getX(), this.targetPoi.getY(), this.targetPoi.getZ(),
                    this.speedModifier);
        }
    }

    @Override
    public void stop() {
        this.millager.getNavigation().stop();
        this.targetPoi = null;
        this.stuck = false;
        this.stuckTimer = 0;
        trimVisited();
    }

    @Override
    public void tick() {
        if (this.targetPoi == null) {
            this.stuck = true;
            return;
        }

        if (this.millager.blockPosition().closerThan(this.targetPoi, ARRIVED_DIST)) {
            this.visited.add(this.targetPoi);
            trimVisited();
            if (!findPoi()) {
                this.stuck = true;
                return;
            }
            this.millager.getNavigation().moveTo(
                    this.targetPoi.getX(), this.targetPoi.getY(), this.targetPoi.getZ(),
                    this.speedModifier);
            this.stuckTimer = 0;
            return;
        }

        if (this.millager.getNavigation().isDone()) {
            if (++this.stuckTimer >= STUCK_TIMEOUT) {
                this.stuck = true;
                return;
            }
            Vec3 towards = Vec3.atBottomCenterOf(this.targetPoi);
            Vec3 rand = DefaultRandomPos.getPosTowards(this.millager, 16, 7, towards, Math.PI / 10.0);
            if (rand == null) {
                rand = DefaultRandomPos.getPosTowards(this.millager, 8, 7, towards, Math.PI / 2.0);
            }
            if (rand == null) {
                this.stuck = true;
                return;
            }
            this.millager.getNavigation().moveTo(rand.x, rand.y, rand.z, this.speedModifier);
        } else {
            this.stuckTimer = 0;
        }
    }

    private void trimVisited() {
        while (this.visited.size() > MAX_VISITED) {
            this.visited.removeFirst();
        }
    }

    private boolean isNoRaid(ServerLevel serverLevel) {
        BlockPos reinforcementCenter = this.millager.getRaidReinforcementCenter();
        Raid raid = serverLevel.getRaidAt(reinforcementCenter != null
                ? reinforcementCenter : this.millager.blockPosition());
        return raid == null || !raid.isActive() || raid.isOver();
    }

    private boolean findPoi() {
        if (!(this.millager.level() instanceof ServerLevel serverLevel)) return false;
        Optional<BlockPos> optional = serverLevel.getPoiManager().getRandom(
                holder -> holder.is(PoiTypes.HOME),
                pos -> hasNotVisited(pos) && (!avoidIndoors || serverLevel.canSeeSky(pos.above())),
                PoiManager.Occupancy.ANY,
                this.millager.blockPosition(),
                POI_SEARCH_RADIUS,
                this.millager.getRandom()
        );
        if (optional.isEmpty()) return false;
        this.targetPoi = optional.get().immutable();
        return true;
    }

    private boolean hasNotVisited(BlockPos pos) {
        for (BlockPos v : this.visited) {
            if (v.equals(pos)) return false;
        }
        return true;
    }
}
