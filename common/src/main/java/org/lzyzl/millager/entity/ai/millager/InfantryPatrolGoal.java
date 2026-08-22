package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.entity.millager.Rider;
import org.lzyzl.millager.util.MillagerTargetingHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class InfantryPatrolGoal extends AbstractMillagerPatrolGoal {

    private static final int POI_ARRIVED_DIST = 4;
    private static final int STALL_TICKS = 60;
    private static final double FOLLOW_SPACING = 2.5;
    private static final int SLOT_STOP_SQ = 9;
    private static final int INDOOR_KILL_CHECK_INTERVAL = 10;
    private static final double INDOOR_KILL_RADIUS = 16.0;

    private static final int SCOUT_WORKSITE_DETECT_DIST = 6;
    private static final double SCOUT_ARRIVED_DIST = 2.5;
    private static final int SCOUT_NAV_TIMEOUT = 220;
    private static final int SCOUT_CHECK_TICKS = 50;

    private static final Predicate<Holder<PoiType>> INFANTRY_POI_PREDICATE =
            holder -> holder.is(PoiTypeTags.ACQUIRABLE_JOB_SITE)
                    || holder.is(PoiTypes.HOME)
                    || holder.is(PoiTypes.MEETING);
    private ScoutPhase scoutPhase = ScoutPhase.NONE;
    @Nullable
    private BlockPos scoutTarget = null;
    @Nullable
    private BlockPos scoutReturnPos = null;
    private int scoutTimer = 0;
    private int indoorKillCheckTimer = 0;

    public InfantryPatrolGoal(AbstractMillager mob, double speed) {
        super(mob, speed);
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.getTarget() != null) return false;
        return this.scoutPhase != ScoutPhase.NONE || !isIndoors() || findNearestEnemy() == null;
    }

    @Override
    public void start() {
        super.start();
        this.indoorKillCheckTimer = 0;
        clearScoutState();
    }

    @Override
    public void stop() {
        super.stop();
        clearScoutState();
    }

    @Override
    public void tick() {
        if (this.scoutPhase == ScoutPhase.NONE) {
            if (++this.indoorKillCheckTimer >= INDOOR_KILL_CHECK_INTERVAL) {
                this.indoorKillCheckTimer = 0;
                if (isIndoors()) {
                    LivingEntity enemy = findNearestEnemy();
                    if (enemy != null) {
                        this.mob.setTarget(enemy);
                        return;
                    }
                }
            }
        }
        super.tick();
    }

    @Override
    protected boolean isSameType(AbstractMillager other) {
        return !(other instanceof Rider);
    }

    @Override
    protected void tickLeaderLogic() {
        if (this.scoutPhase != ScoutPhase.NONE) {
            tickScoutPhase();
            return;
        }
        super.tickLeaderLogic();
    }

    @Override
    protected void tickFollowerLogic(AbstractMillager leader, int index, int total) {
        if (leader.isScoutingPOI()) {
            this.mob.getNavigation().stop();
            this.mob.getLookControl().setLookAt(
                    leader.getX(), leader.getEyeY(), leader.getZ(), 30.0F, 30.0F);
            return;
        }
        super.tickFollowerLogic(leader, index, total);
    }

    @Override
    protected boolean onWaypointArrived(int waypointIndex) {
        if (!(this.mob.level() instanceof ServerLevel serverLevel)) return true;
        BlockPos arrivedSurface = (waypointIndex >= 0 && waypointIndex < this.patrolRoute.size())
                ? this.patrolRoute.get(waypointIndex)
                : this.mob.blockPosition();

        BlockPos worksite = findNearbyScoutableWorksite(serverLevel, arrivedSurface);
        if (worksite != null && isPositionReachable(worksite)) {
            this.scoutTarget = worksite;
            this.scoutReturnPos = arrivedSurface;
            this.scoutPhase = ScoutPhase.MOVING_IN;
            this.scoutTimer = 0;
            this.mob.setScoutingPOI(true);
            this.mob.getNavigation().moveTo(
                    worksite.getX(), worksite.getY(), worksite.getZ(), this.walkSpeed);
            return false;
        }
        return true;
    }

    @Override
    protected int getArrivedDist() {
        return this.mob.isPassenger() ? POI_ARRIVED_DIST + 2 : POI_ARRIVED_DIST;
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
    protected int getWildernessDist() {
        int scaled = 10 + (48 - 10) * (this.lastSquadSize - 1) / 9;
        return Math.max(10, Math.min(48, scaled));
    }

    @Override
    protected void buildPatrolRoute() {
        this.patrolRoute.clear();
        if (!(this.mob.level() instanceof ServerLevel serverLevel)) return;

        List<BlockPos> found = new ArrayList<>();
        serverLevel.getPoiManager().findAll(
                INFANTRY_POI_PREDICATE,
                pos -> true,
                this.mob.blockPosition(),
                ROUTE_SCAN_RADIUS,
                PoiManager.Occupancy.ANY
        ).forEach(found::add);

        if (found.isEmpty()) return;
        List<BlockPos> deduplicated = deduplicatePois(found);
        deduplicated.sort(Comparator.comparingInt(pos -> -(int) this.mob.blockPosition().distSqr(pos)));
        int startOffset = deduplicated.size() > 1 ? this.mob.getRandom().nextInt(deduplicated.size()) : 0;

        for (int i = 0; i < deduplicated.size(); i++) {
            BlockPos raw = deduplicated.get((i + startOffset) % deduplicated.size());
            this.patrolRoute.add(findSafeTarget(serverLevel, raw));
        }
    }

    @Nullable
    private BlockPos findNearbyScoutableWorksite(ServerLevel level, BlockPos center) {
        return level.getPoiManager().findClosest(
                holder -> holder.is(PoiTypeTags.ACQUIRABLE_JOB_SITE) || holder.is(PoiTypes.HOME),
                center,
                SCOUT_WORKSITE_DETECT_DIST,
                PoiManager.Occupancy.ANY
        ).orElse(null);
    }

    private static BlockPos findSafeTarget(ServerLevel level, BlockPos poi) {
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, poi);
        if (surface.getY() <= poi.getY() + 2) return surface;
        for (int radius = 3; radius <= 9; radius += 3) {
            for (int a = 0; a < 8; a++) {
                double angle = a * Math.PI / 4.0;
                int ox = (int) Math.round(Math.cos(angle) * radius);
                int oz = (int) Math.round(Math.sin(angle) * radius);
                BlockPos candidate = level.getHeightmapPos(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, poi.offset(ox, 0, oz));
                if (Math.abs(candidate.getY() - poi.getY()) <= 3) return candidate;
            }
        }
        return poi.above();
    }

    private void clearScoutState() {
        this.scoutPhase = ScoutPhase.NONE;
        this.scoutTarget = null;
        this.scoutReturnPos = null;
        this.scoutTimer = 0;
        this.mob.setScoutingPOI(false);
    }

    private boolean isIndoors() {
        if (!(this.mob.level() instanceof ServerLevel serverLevel)) return false;
        BlockPos pos = this.mob.blockPosition();
        BlockPos surface = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
        return surface.getY() > pos.getY() + 2;
    }

    @Nullable
    private LivingEntity findNearestEnemy() {
        if (!(this.mob.level() instanceof ServerLevel serverLevel)) return null;
        List<LivingEntity> candidates = new ArrayList<>();

        candidates.addAll(this.mob.level().getEntitiesOfClass(
                Mob.class,
                this.mob.getBoundingBox().inflate(INDOOR_KILL_RADIUS),
                m -> !m.isUnderWater() && MillagerTargetingHelper.canAttack(this.mob, m)
        ));

        candidates.addAll(this.mob.level().getEntitiesOfClass(
                Player.class,
                this.mob.getBoundingBox().inflate(INDOOR_KILL_RADIUS),
                player -> {
                    if (player.isCreative() || player.isSpectator()) return false;
                    if (player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE)) return false;
                    if (this.mob.isAngryAt(player, serverLevel)) return true;
                    ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
                    return head.is(MillagerItems.ILLAGER_HEAD.asItem()) || head.is(MillagerItems.VILLAGER_HEAD.asItem());
                }
        ));

        return candidates.stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(this.mob)))
                .orElse(null);
    }

    private void tickScoutPhase() {
        switch (this.scoutPhase) {
            case MOVING_IN -> tickScoutMovingIn();
            case CHECKING -> tickScoutChecking();
            case RETURNING -> tickScoutReturning();
            default -> {
                clearScoutState();
                advanceToNextWaypoint();
            }
        }
    }

    private void tickScoutMovingIn() {
        if (++this.scoutTimer > SCOUT_NAV_TIMEOUT) {
            clearScoutState();
            advanceToNextWaypoint();
            return;
        }
        assert this.scoutTarget != null;
        if (this.mob.blockPosition().closerThan(this.scoutTarget, SCOUT_ARRIVED_DIST)) {
            this.scoutPhase = ScoutPhase.CHECKING;
            this.scoutTimer = 0;
            this.mob.getNavigation().stop();
            return;
        }
        if (this.mob.getNavigation().isDone()) {
            if (isPositionReachable(this.scoutTarget)) {
                this.mob.getNavigation().moveTo(
                        this.scoutTarget.getX(), this.scoutTarget.getY(), this.scoutTarget.getZ(),
                        this.walkSpeed);
            } else {
                clearScoutState();
                advanceToNextWaypoint();
            }
        }
    }

    private void tickScoutChecking() {
        if (this.scoutTimer % INDOOR_KILL_CHECK_INTERVAL == 0) {
            LivingEntity enemy = findNearestEnemy();
            if (enemy != null) {
                broadcastTargetToMates(enemy);
                this.mob.setTarget(enemy);
                clearScoutState();
                return;
            }
        }
        if (++this.scoutTimer >= SCOUT_CHECK_TICKS) {
            this.scoutPhase = ScoutPhase.RETURNING;
            this.scoutTimer = 0;
            if (this.scoutReturnPos != null) {
                this.mob.getNavigation().moveTo(
                        this.scoutReturnPos.getX(), this.scoutReturnPos.getY(), this.scoutReturnPos.getZ(),
                        this.walkSpeed);
            }
        }
    }

    private void tickScoutReturning() {
        boolean returned = this.scoutReturnPos == null
                || this.mob.blockPosition().closerThan(this.scoutReturnPos, getArrivedDist() + 1.0);
        if (returned || ++this.scoutTimer > SCOUT_NAV_TIMEOUT) {
            clearScoutState();
            advanceToNextWaypoint();
        }
    }

    private void broadcastTargetToMates(LivingEntity target) {
        this.mob.level().getEntitiesOfClass(
                AbstractMillager.class,
                this.mob.getBoundingBox().inflate(32.0),
                m -> m != this.mob && isSameType(m)
        ).forEach(mate -> mate.setTarget(target));
    }

    private enum ScoutPhase {
        NONE,
        MOVING_IN,
        CHECKING,
        RETURNING
    }
}
