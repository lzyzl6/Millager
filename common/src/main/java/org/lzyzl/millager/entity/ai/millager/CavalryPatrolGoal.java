package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.entity.millager.Rider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class CavalryPatrolGoal extends AbstractMillagerPatrolGoal {

    private static final int POI_ARRIVED_DIST = 8;
    private static final int STALL_TICKS = 30;
    private static final double FOLLOW_SPACING = 4.5;
    private static final int SLOT_STOP_SQ = 25;
    private static final int ROAD_SCAN_RADIUS = 128;
    private static final int ROAD_SCAN_STEP = 4;
    private static final int ROAD_Y_CHECK = 3;
    private static final int MIN_ROAD_STOPS = 3;
    private static final int MIN_ROAD_DIST = 25;
    private static final int FORMATION_CACHE_TTL = 40;
    private static final int LOCAL_ROAD_CHECK_RADIUS = 16;
    private static final int LOCAL_ROAD_CHECK_STEP = 8;

    private boolean cachedWildFormation = false;
    private int wildFormationCacheTick = -999;

    public CavalryPatrolGoal(AbstractMillager mob, double speed) {
        super(mob, speed);
    }

    @Override
    protected int getArrivedDist() {
        return POI_ARRIVED_DIST;
    }

    @Override
    protected int getStallTicks() {
        return STALL_TICKS;
    }

    @Override
    protected double getFollowSpacing() {
        return FOLLOW_SPACING;
    }

    @Override
    protected int getSlotStopThreshSq() {
        return SLOT_STOP_SQ;
    }

    @Override
    protected boolean isSameType(AbstractMillager other) {
        return other instanceof Rider;
    }

    @Override
    protected int getFormationColumns(int total) {
        if (this.mob.tickCount - this.wildFormationCacheTick > FORMATION_CACHE_TTL) {
            this.cachedWildFormation = !hasNearbyRoads();
            this.wildFormationCacheTick = this.mob.tickCount;
        }
        if (!this.cachedWildFormation) return 2;
        return computeWildernessColumns(total);
    }

    private boolean hasNearbyRoads() {
        if (!(this.mob.level() instanceof ServerLevel serverLevel)) return false;
        BlockPos center = this.mob.blockPosition();
        for (int x = -LOCAL_ROAD_CHECK_RADIUS; x <= LOCAL_ROAD_CHECK_RADIUS; x += LOCAL_ROAD_CHECK_STEP) {
            for (int z = -LOCAL_ROAD_CHECK_RADIUS; z <= LOCAL_ROAD_CHECK_RADIUS; z += LOCAL_ROAD_CHECK_STEP) {
                BlockPos surface = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center.offset(x, 0, z));
                for (int dy = 0; dy <= ROAD_Y_CHECK; dy++) {
                    if (serverLevel.getBlockState(surface.below(dy)).is(Blocks.DIRT_PATH)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static int computeWildernessColumns(int total) {
        if (total <= 2) return 1;
        for (int col = 1; col <= 10; col++) {
            int row = (int) Math.ceil((double) total / col);
            double ratio = (double) col / row;
            if (ratio >= 0.4 && ratio <= 1.0) return col;
        }
        return (int) Math.ceil(Math.sqrt(total));
    }

    @Override
    protected void buildPatrolRoute() {
        this.patrolRoute.clear();
        if (!(this.mob.level() instanceof ServerLevel serverLevel)) return;

        BlockPos center = this.mob.blockPosition();

        List<BlockPos> pathBlocks = new ArrayList<>();
        for (int x = -ROAD_SCAN_RADIUS; x <= ROAD_SCAN_RADIUS; x += ROAD_SCAN_STEP) {
            for (int z = -ROAD_SCAN_RADIUS; z <= ROAD_SCAN_RADIUS; z += ROAD_SCAN_STEP) {
                BlockPos col = center.offset(x, 0, z);
                BlockPos surface = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, col);
                for (int dy = 0; dy <= ROAD_Y_CHECK; dy++) {
                    if (serverLevel.getBlockState(surface.below(dy)).is(Blocks.DIRT_PATH)) {
                        pathBlocks.add(surface.below(dy).above());
                        break;
                    }
                }
            }
        }

        if (pathBlocks.isEmpty()) return;

        int minDistSq = MIN_ROAD_DIST * MIN_ROAD_DIST;
        List<BlockPos> distant = new ArrayList<>();
        for (BlockPos pos : pathBlocks) {
            if (center.distSqr(pos) >= minDistSq) {
                distant.add(pos);
            }
        }

        if (distant.isEmpty()) return;
        List<BlockPos> deduplicated = deduplicatePois(distant);

        if (deduplicated.size() < MIN_ROAD_STOPS) return;

        BlockPos farthest = deduplicated.stream()
                .max(Comparator.comparingInt(pos -> (int) center.distSqr(pos)))
                .orElse(center);
        List<BlockPos> sorted = nearestNeighborSort(deduplicated, farthest);

        for (BlockPos pos : sorted) {
            this.patrolRoute.add(pos);
        }
    }

    @Override
    protected int getWildernessDist() {
        int scaled = 24 + (48 - 24) * (this.lastSquadSize - 1) / 9;
        return Math.max(24, Math.min(48, scaled));
    }
}
