package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.util.MiscHelper;
import org.lzyzl.millager.util.OrcaAvoidance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public abstract class AbstractMillagerPatrolGoal extends Goal {

    protected static final int POI_WAIT_TICKS = 60;
    protected static final int WAYPOINT_TIMEOUT = 600;
    protected static final int ROUTE_SCAN_RADIUS = 128;
    protected static final int DEDUP_RADIUS = 4;
    protected static final int FOLLOWER_RECALC_INTERVAL = 20;
    protected static final int FOLLOWER_SLOT_MOVED_SQ = 9;
    protected static final int CATCHUP_THRESHOLD_SQ = 144;
    protected static final int STRAGGLER_THRESHOLD_SQ = 400;
    protected static final int MAX_LEADER_DISTANCE_SQ = 256;
    protected static final double CATCHUP_SPEED_MOD = 1.4;
    protected static final double STRAGGLER_SPEED_MOD = 1.8;
    protected static final float SPIN_YAW_THRESHOLD = 8.0f;
    protected static final int SPIN_COUNT_THRESHOLD = 12;
    protected static final int WILDERNESS_MAX_ATTEMPTS = 5;
    protected static final int NAV_CHECK_INTERVAL = 5;
    protected static final int CREEPER_CACHE_TTL = 40;
    protected static final int SQUAD_ASSIGNMENT_SCAN_INTERVAL = 60;
    protected static final double SQUAD_JOIN_RADIUS = 16.0D;
    protected static final double SQUAD_APPROACH_RADIUS = 24.0D;
    protected static final double SQUAD_FORMATION_RADIUS = 8.0D;

    protected final AbstractMillager mob;
    protected final double walkSpeed;
    protected final List<BlockPos> patrolRoute = new ArrayList<>();
    protected int patrolRouteIndex = -1;
    protected boolean routeBuilt = false;
    protected int poiWaitTimer = 0;
    protected int waypointTimeoutTimer = 0;
    protected int followerNavTimer = 0;
    protected int leaderStallTimer = 0;
    protected int leaderSpinTimer = 0;
    protected float lastLeaderYRot = 0.0f;
    protected BlockPos lastFollowerSlot = null;
    protected int leaderNavCheckTimer = 0;
    protected int lastCreeperScanTick = -999;
    protected boolean creeperNearbyCache = false;
    protected int lastSquadSize = 1;
    protected @Nullable AbstractMillager ungroupedMate;
    protected int ungroupedMateNavTimer = 0;

    protected AbstractMillagerPatrolGoal(AbstractMillager mob, double speed) {
        this.mob = mob;
        this.walkSpeed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    protected static List<BlockPos> deduplicatePois(List<BlockPos> pois) {
        List<BlockPos> result = new ArrayList<>();
        outer:
        for (BlockPos candidate : pois) {
            for (BlockPos existing : result) {
                if (existing.closerThan(candidate, DEDUP_RADIUS)) continue outer;
            }
            result.add(candidate);
        }
        return result;
    }

    protected static List<BlockPos> nearestNeighborSort(List<BlockPos> pois, BlockPos start) {
        List<BlockPos> remaining = new ArrayList<>(pois);
        List<BlockPos> result = new ArrayList<>();
        BlockPos current = start;
        while (!remaining.isEmpty()) {
            int bestIdx = 0, bestDist = Integer.MAX_VALUE;
            for (int i = 0; i < remaining.size(); i++) {
                int d = (int) remaining.get(i).distSqr(current);
                if (d < bestDist) {
                    bestDist = d;
                    bestIdx = i;
                }
            }
            current = remaining.remove(bestIdx);
            result.add(current);
        }
        return result;
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() == null && this.mob.getRandom().nextInt(40) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.getTarget() == null;
    }

    @Override
    public void start() {
        this.routeBuilt = false;
        this.patrolRouteIndex = -1;
        this.poiWaitTimer = 0;
        this.waypointTimeoutTimer = 0;
        this.followerNavTimer = 0;
        this.leaderStallTimer = 0;
        this.leaderSpinTimer = 0;
        this.lastLeaderYRot = this.mob.getYRot();
        this.lastFollowerSlot = null;
        this.leaderNavCheckTimer = 0;
        this.lastCreeperScanTick = -999;
        this.creeperNearbyCache = false;
        this.ungroupedMate = null;
        this.ungroupedMateNavTimer = 0;
        this.patrolRoute.clear();
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
        this.patrolRoute.clear();
        this.patrolRouteIndex = -1;
        this.routeBuilt = false;
    }

    @Override
    public void tick() {
        List<AbstractMillager> mates = new ArrayList<>(this.mob.level().getEntitiesOfClass(
                AbstractMillager.class,
                this.mob.getBoundingBox().inflate(32.0)
        ));
        UUID squadId = this.mob.getSquadId();
        mates.removeIf(e -> squadId != null
                ? !squadId.equals(e.getSquadId())
                : !this.isSameType(e) || e.getSquadId() != null);

        if (mates.isEmpty()) return;

        mates.sort(Comparator.comparing(AbstractMillager::getUUID));
        AbstractMillager leader;
        if (squadId == null) {
            if (this.mob.tickCount % SQUAD_ASSIGNMENT_SCAN_INTERVAL != 0) {
                if (!this.followUngroupedMate()) this.tickLeaderLogic();
                return;
            }
            AbstractMillager availableLeader = this.findAvailableSquadLeader();
            if (availableLeader != null) {
                this.mob.setSquad(availableLeader.getSquadId(), false);
                this.ungroupedMate = null;
                return;
            }
            mates.removeIf(mate -> mate != this.mob && (!mate.isAlive()
                    || this.mob.distanceToSqr(mate) > SQUAD_FORMATION_RADIUS * SQUAD_FORMATION_RADIUS
                    || !this.isPositionReachable(mate.blockPosition())));
            int squadIndex = mates.indexOf(this.mob) / AbstractMillager.MAX_SQUAD_SIZE;
            int firstIndex = squadIndex * AbstractMillager.MAX_SQUAD_SIZE;
            int lastIndex = Math.min(firstIndex + AbstractMillager.MAX_SQUAD_SIZE, mates.size());
            mates = new ArrayList<>(mates.subList(firstIndex, lastIndex));
            if (mates.size() < 2) {
                this.ungroupedMate = this.findReachableUngroupedMate();
                if (!this.followUngroupedMate()) this.tickLeaderLogic();
                return;
            }
            this.ungroupedMate = null;
            leader = mates.getFirst();
            UUID newSquadId = leader.getUUID();
            for (AbstractMillager mate : mates) {
                boolean isLeader = mate == leader;
                mate.setSquad(newSquadId, false);
                if (isLeader) mate.promoteToSquadLeader();
            }
        } else {
            leader = mates.stream().filter(AbstractMillager::isSquadLeader)
                    .findFirst().orElse(null);
        }
        if (leader == null) return;
        this.lastSquadSize = mates.size();
        List<AbstractMillager> squadMates = List.copyOf(mates);
        this.mob.markPatrolAvoidanceActive();
        if (this.mob == leader) {
            if (mates.stream().anyMatch(mate -> this.mob.distanceToSqr(mate) > MAX_LEADER_DISTANCE_SQ)) {
                this.mob.getNavigation().stop();
            } else {
                this.tickLeaderLogic();
            }
        } else {
            mates.remove(leader);
            int followerIndex = mates.indexOf(this.mob);
            if (followerIndex >= 0) this.tickFollowerLogic(leader, followerIndex, mates.size() + 1);
        }
        OrcaAvoidance.apply(this.mob, squadMates);
    }

    private @Nullable AbstractMillager findAvailableSquadLeader() {
        return this.mob.level().getEntitiesOfClass(
                        AbstractMillager.class,
                        this.mob.getBoundingBox().inflate(SQUAD_JOIN_RADIUS),
                        candidate -> candidate != this.mob && candidate.isAlive()
                                && this.isSameType(candidate) && candidate.isSquadLeader()
                                && candidate.getRaidReinforcementCenter() == null
                                && candidate.getSquadId() != null)
                .stream()
                .filter(AbstractMillager::hasNearbySquadCapacity)
                .filter(candidate -> this.isPositionReachable(candidate.blockPosition()))
                .min(Comparator.comparingDouble(this.mob::distanceToSqr))
                .orElse(null);
    }

    private @Nullable AbstractMillager findReachableUngroupedMate() {
        return this.mob.level().getEntitiesOfClass(
                        AbstractMillager.class,
                        this.mob.getBoundingBox().inflate(SQUAD_APPROACH_RADIUS),
                        candidate -> candidate != this.mob && candidate.isAlive()
                                && this.isSameType(candidate) && candidate.getSquadId() == null)
                .stream()
                .filter(candidate -> this.isPositionReachable(candidate.blockPosition()))
                .min(Comparator.comparingDouble(this.mob::distanceToSqr))
                .orElse(null);
    }

    private boolean followUngroupedMate() {
        AbstractMillager mate = this.ungroupedMate;
        if (mate == null) return false;
        if (!mate.isAlive() || !this.isSameType(mate) || mate.getSquadId() != null
                || this.mob.distanceToSqr(mate) > SQUAD_APPROACH_RADIUS * SQUAD_APPROACH_RADIUS) {
            this.ungroupedMate = null;
            this.ungroupedMateNavTimer = 0;
            return false;
        }
        if (this.mob.distanceToSqr(mate) <= SQUAD_FORMATION_RADIUS * SQUAD_FORMATION_RADIUS) {
            this.mob.getNavigation().stop();
            return true;
        }
        this.ungroupedMateNavTimer++;
        if (this.ungroupedMateNavTimer >= FOLLOWER_RECALC_INTERVAL || this.mob.getNavigation().isDone()) {
            this.mob.getNavigation().moveTo(mate, this.walkSpeed);
            this.ungroupedMateNavTimer = 0;
        }
        return true;
    }

    protected abstract boolean isSameType(AbstractMillager other);

    protected void tickLeaderLogic() {
        if (!this.routeBuilt) {
            this.buildPatrolRoute();
            this.routeBuilt = true;
        }

        BlockPos currentTarget = this.mob.getPatrolTarget();
        boolean arrived = currentTarget != null
                && this.mob.blockPosition().closerThan(currentTarget, this.getArrivedDist());
        boolean timedOut = currentTarget != null && this.waypointTimeoutTimer <= 0;

        double movementSq = this.mob.getDeltaMovement().horizontalDistanceSqr();
        float currentYRot = this.mob.getYRot();
        float yawDelta = Math.abs(currentYRot - this.lastLeaderYRot);
        if (yawDelta > 180.0f) yawDelta = 360.0f - yawDelta;
        this.lastLeaderYRot = currentYRot;

        boolean isSpinning = movementSq < 0.004 && yawDelta > SPIN_YAW_THRESHOLD;

        this.leaderNavCheckTimer++;
        boolean checkNav = this.leaderNavCheckTimer >= NAV_CHECK_INTERVAL;
        if (checkNav) this.leaderNavCheckTimer = 0;

        if (checkNav && this.mob.getNavigation().isInProgress()) {
            this.leaderStallTimer = (movementSq < 0.001) ? this.leaderStallTimer + NAV_CHECK_INTERVAL : 0;
            this.leaderSpinTimer = isSpinning ? this.leaderSpinTimer + NAV_CHECK_INTERVAL : 0;
        }
        if (!checkNav && !this.mob.getNavigation().isInProgress()) {
            this.leaderStallTimer = 0;
            this.leaderSpinTimer = 0;
        }

        boolean isStuck = this.leaderStallTimer > this.getStallTicks()
                || this.leaderSpinTimer > SPIN_COUNT_THRESHOLD;

        if (isStuck || timedOut) {
            this.leaderStallTimer = 0;
            this.leaderSpinTimer = 0;
            this.advanceToNextWaypoint();
            return;
        }

        if (currentTarget == null || arrived) {
            if (arrived && this.poiWaitTimer < POI_WAIT_TICKS) {
                this.mob.getNavigation().stop();
                this.poiWaitTimer++;
                return;
            }
            this.poiWaitTimer = 0;
            if (this.onWaypointArrived(this.patrolRouteIndex)) {
                this.advanceToNextWaypoint();
            }
        } else {
            if (checkNav && (this.mob.getNavigation().isDone() || this.mob.getNavigation().isStuck())) {
                this.mob.getNavigation().moveTo(
                        currentTarget.getX(), currentTarget.getY(), currentTarget.getZ(),
                        this.walkSpeed);
            }
            this.waypointTimeoutTimer--;
        }
    }

    protected void tickFollowerLogic(AbstractMillager leader, int index, int total) {
        PathNavigation nav = this.mob.getNavigation();
        if (this.mob.distanceToSqr(leader) > MAX_LEADER_DISTANCE_SQ) {
            if (this.lastFollowerSlot != null || this.followerNavTimer >= FOLLOWER_RECALC_INTERVAL || nav.isDone()) {
                nav.moveTo(leader, this.walkSpeed * STRAGGLER_SPEED_MOD);
                this.lastFollowerSlot = null;
                this.followerNavTimer = 0;
            } else {
                this.followerNavTimer++;
            }
            return;
        }
        double spacing = this.getFollowSpacing();
        boolean isAttacking = leader.getTarget() != null;

        double offsetX, offsetZ;
        if (isAttacking) {
            int row = index / 2 + 1;
            int side = (index % 2 == 0) ? 1 : -1;
            offsetX = side * row * spacing;
            offsetZ = -row * spacing;
        } else {
            int columns = this.getFormationColumns(total);
            int row = index / columns + 1;
            int col = index % columns;
            offsetX = (col - (columns - 1) / 2.0) * spacing;
            offsetZ = -row * spacing;
        }

        BlockPos leaderTarget = leader.getPatrolTarget();
        double leaderSpeedSq = leader.getDeltaMovement().horizontalDistanceSqr();
        float yaw;

        if (leaderTarget != null && leaderSpeedSq > 0.005) {
            double dx = leaderTarget.getX() - leader.getX();
            double dz = leaderTarget.getZ() - leader.getZ();
            yaw = (dx * dx + dz * dz > 4.0)
                    ? (float) Math.toDegrees(Math.atan2(-dx, dz))
                    : leader.getYRot();
        } else {
            yaw = leader.getYRot();
        }

        double rad = Math.toRadians(-yaw);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double rotX = offsetX * cos - offsetZ * sin;
        double rotZ = offsetX * sin + offsetZ * cos;

        BlockPos slotPos = leader.blockPosition().offset((int) rotX, 0, (int) rotZ);
        if (this.mob.level() instanceof ServerLevel serverLevel) {
            BlockPos candidatePos = new BlockPos(slotPos.getX(), leader.blockPosition().getY(), slotPos.getZ());
            if (this.mob.level().getBlockState(candidatePos).isAir()
                    && this.mob.level().getBlockState(candidatePos.above()).isAir()) {
                slotPos = candidatePos;
            } else {
                slotPos = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, slotPos);
            }
        }

        int distSq = (int) this.mob.blockPosition().distSqr(slotPos);
        if (distSq <= this.getSlotStopThreshSq() && !isOverlappingAlly()) {
            nav.stop();
            this.mob.getLookControl().setLookAt(
                    leader.getX(), leader.getEyeY(), leader.getZ(), 30.0F, 30.0F);
            this.lastFollowerSlot = slotPos;
            this.followerNavTimer = 0;
            return;
        }

        boolean slotMoved = this.lastFollowerSlot == null
                || this.lastFollowerSlot.distSqr(slotPos) > FOLLOWER_SLOT_MOVED_SQ;
        this.followerNavTimer++;
        boolean shouldRecalc = slotMoved || this.followerNavTimer >= FOLLOWER_RECALC_INTERVAL;
        if (!shouldRecalc) return;

        this.followerNavTimer = 0;
        this.lastFollowerSlot = slotPos;

        var path = nav.createPath(slotPos, 0);
        if (path == null || !path.canReach()) {
            nav.moveTo(leader, this.walkSpeed);
        } else {
            double speedMod = (distSq > STRAGGLER_THRESHOLD_SQ) ? STRAGGLER_SPEED_MOD :
                    (distSq > CATCHUP_THRESHOLD_SQ) ? CATCHUP_SPEED_MOD : 1.0;
            nav.moveTo(path, this.walkSpeed * speedMod);
        }
    }

    protected abstract void buildPatrolRoute();

    protected abstract int getArrivedDist();

    protected abstract int getStallTicks();

    protected void advanceToNextWaypoint() {
        if (this.patrolRoute.isEmpty()) {
            this.setWildernessTarget();
            return;
        }

        int attempts = this.patrolRoute.size() + 1;
        while (attempts-- > 0) {
            this.patrolRouteIndex++;
            if (this.patrolRouteIndex >= this.patrolRoute.size()) {
                this.buildPatrolRoute();
                if (this.patrolRoute.isEmpty()) {
                    this.setWildernessTarget();
                    return;
                }
                this.patrolRouteIndex = 0;
                attempts = this.patrolRoute.size();
            }

            BlockPos next = this.patrolRoute.get(this.patrolRouteIndex);
            if (isPositionReachable(next) && isCreeperFree(next)) {
                this.mob.setPatrolTarget(next);
                this.waypointTimeoutTimer = WAYPOINT_TIMEOUT;
                this.mob.getNavigation().moveTo(next.getX(), next.getY(), next.getZ(), this.walkSpeed);
                return;
            }

            this.patrolRoute.remove(this.patrolRouteIndex);
            this.patrolRouteIndex--;
            if (this.patrolRoute.isEmpty()) break;
        }

        this.setWildernessTarget();
    }

    protected boolean onWaypointArrived(int waypointIndex) {
        return true;
    }

    protected abstract double getFollowSpacing();

    protected boolean isOverlappingAlly() {
        AABB ownBox = MiscHelper.getMillagerCollisionBox(this.mob);
        return this.mob.level().getEntitiesOfClass(
                AbstractMillager.class,
                ownBox.inflate(2.0D),
                ally -> ally != this.mob && this.mob.isAlliedTo(ally)
        ).stream().anyMatch(ally -> ownBox.intersects(MiscHelper.getMillagerCollisionBox(ally)));
    }

    protected abstract int getSlotStopThreshSq();

    protected int getFormationColumns(int total) {
        return Math.max(1, (int) Math.ceil(Math.sqrt(total)));
    }

    protected void setWildernessTarget() {
        BlockPos fallback = null;
        for (int attempt = 0; attempt < WILDERNESS_MAX_ATTEMPTS; attempt++) {
            BlockPos candidate = this.getWildernessPos();
            if (fallback == null) fallback = candidate;
            if (isPositionReachable(candidate) && isCreeperFree(candidate)) {
                this.applyWildernessTarget(candidate);
                return;
            }
        }
        if (fallback == null) fallback = this.getWildernessPos();
        this.applyWildernessTarget(fallback);
    }

    protected boolean isPositionReachable(BlockPos pos) {
        var path = this.mob.getNavigation().createPath(pos, 0);
        return path != null && path.canReach();
    }

    protected boolean isCreeperFree(BlockPos pos) {
        if (this.mob.tickCount - this.lastCreeperScanTick < CREEPER_CACHE_TTL) {
            return !this.creeperNearbyCache;
        }
        this.lastCreeperScanTick = this.mob.tickCount;
        this.creeperNearbyCache = !this.mob.level().getEntitiesOfClass(
                Creeper.class,
                new AABB(pos).inflate(13.0)
        ).isEmpty();
        return !this.creeperNearbyCache;
    }

    protected BlockPos getWildernessPos() {
        int dist = getWildernessDist();
        Vec3 vec = LandRandomPos.getPos(this.mob, dist, 7);
        if (vec != null) return new BlockPos((int) vec.x, (int) vec.y, (int) vec.z);
        int range = dist * 3;
        BlockPos randomPos = this.mob.blockPosition().offset(
                this.mob.getRandom().nextInt(range * 2) - range,
                0,
                this.mob.getRandom().nextInt(range * 2) - range
        );
        return this.mob.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, randomPos);
    }

    protected int getWildernessDist() {
        return 10;
    }

    protected void applyWildernessTarget(BlockPos pos) {
        this.mob.setPatrolTarget(pos);
        this.waypointTimeoutTimer = WAYPOINT_TIMEOUT;
        this.mob.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), this.walkSpeed);
    }
}
