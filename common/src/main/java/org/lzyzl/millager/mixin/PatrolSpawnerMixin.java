package org.lzyzl.millager.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.MillagerGameRules;
import org.lzyzl.millager.behavior.MillagerEntityPool;
import org.lzyzl.millager.behavior.patrol.PatrolConfig;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.worldgen.MillagerStructures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


@Mixin(PatrolSpawner.class)
public class PatrolSpawnerMixin {

    @Unique private int millager$wildTimer = 0;

    @Unique private int millager$cpScanTimer = 0;
    @Unique private final Set<BlockPos> millager$visitedCommandPosts = new HashSet<>();
    @Unique private final Set<BlockPos> millager$visitedRuinedCommandPosts = new HashSet<>();
    @Unique private final Map<BlockPos, Integer> millager$pendingCommandPosts = new HashMap<>();
    @Unique private final Map<BlockPos, Integer> millager$pendingRuinedCommandPosts = new HashMap<>();

    @Inject(method = "tick", at = @At("HEAD"))
    private void millager$onTick(ServerLevel serverLevel, boolean bl, CallbackInfo ci) {
        if (!serverLevel.getGameRules().get(GameRules.SPAWN_MOBS)) return;
        if (!serverLevel.dimension().equals(Level.OVERWORLD)) return;

        RandomSource random = serverLevel.getRandom();

        millager$processPendingStructurePatrols(serverLevel, random);

        if (serverLevel.getGameRules().get(MillagerGameRules.ENABLE_WILDER_PATROLS.get())) {
            if (millager$wildTimer > 0) {
                millager$wildTimer--;
            } else {
                millager$wildTimer = PatrolConfig.WILD_TIMER_BASE
                        + (PatrolConfig.WILD_TIMER_RAND > 0
                                ? random.nextInt(PatrolConfig.WILD_TIMER_RAND)
                                : 0);
                if (random.nextFloat() < PatrolConfig.WILD_PATROL_SPAWN_CHANCE)
                    millager$trySpawnPatrol(serverLevel, random,
                            random.nextFloat() < PatrolConfig.WILD_CAVALRY_CHANCE);
            }
        }

        if (millager$cpScanTimer > 0) {
            millager$cpScanTimer--;
        } else {
            millager$cpScanTimer = PatrolConfig.COMMAND_POST_SCAN_INTERVAL;

            for (BlockPos found : millager$findNearStructures(serverLevel, MillagerStructures.COMMAND_POST_KEY)) {
                if (!millager$visitedCommandPosts.contains(found)) {
                    millager$pendingCommandPosts.putIfAbsent(found, PatrolConfig.STRUCTURE_PATROL_DELAY);
                }
            }

            for (BlockPos foundRuined : millager$findNearStructures(serverLevel, MillagerStructures.RUINED_COMMAND_POST_KEY)) {
                if (!millager$visitedRuinedCommandPosts.contains(foundRuined)) {
                    millager$pendingRuinedCommandPosts.putIfAbsent(foundRuined, PatrolConfig.STRUCTURE_PATROL_DELAY);
                }
            }
        }
    }

    @Unique
    private void millager$processPendingStructurePatrols(ServerLevel level, RandomSource random) {
        millager$processPendingStructurePatrolEntries(level, random, millager$pendingCommandPosts, millager$visitedCommandPosts, true);

        millager$processPendingStructurePatrolEntries(level, random, millager$pendingRuinedCommandPosts, millager$visitedRuinedCommandPosts, false);
    }

    @Unique
    private void millager$processPendingStructurePatrolEntries(ServerLevel level, RandomSource random, Map<BlockPos, Integer> pendingPatrols,
                                                                Set<BlockPos> visitedPatrols, boolean commandPost) {
        Iterator<Map.Entry<BlockPos, Integer>> pendingEntries = pendingPatrols.entrySet().iterator();
        while (pendingEntries.hasNext()) {
            Map.Entry<BlockPos, Integer> entry = pendingEntries.next();
            if (entry.getValue() > 0) {
                entry.setValue(entry.getValue() - 1);
                continue;
            }
            BlockPos center = entry.getKey();
            pendingEntries.remove();
            if (millager$chunksUnloaded(level, center.mutable())) continue;
            visitedPatrols.add(center);
            if (commandPost) {
                millager$spawnCommandPostPatrol(level, random, center);
                millager$spawnCommandPostPatrol(level, random, center);
            } else {
                millager$spawnVanillaPatrol(level, random, center);
            }
        }
    }

    @Unique
    private void millager$trySpawnPatrol(ServerLevel level, RandomSource random, boolean cavalry) {
        if (level.getOverworldClockTime() / 24000L < PatrolConfig.MIN_DAYS_PLAYED) return;

        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) return;

        ServerPlayer player = players.get(random.nextInt(players.size()));
        if (player.isSpectator()) return;

        int spawnDistanceRange = PatrolConfig.SPAWN_MAX_DIST - PatrolConfig.SPAWN_MIN_DIST + 1;
        int offsetX = (PatrolConfig.SPAWN_MIN_DIST + random.nextInt(spawnDistanceRange))
                * (random.nextBoolean() ? 1 : -1);
        int offsetZ = (PatrolConfig.SPAWN_MIN_DIST + random.nextInt(spawnDistanceRange))
                * (random.nextBoolean() ? 1 : -1);

        BlockPos.MutableBlockPos mPos = player.blockPosition().mutable().move(offsetX, 0, offsetZ);

        if (millager$chunksUnloaded(level, mPos)) return;

        if (level.isCloseToVillage(mPos, 2)) return;

        if (millager$tooManyNearby(level, mPos)) return;

        List<MillagerEntityPool.Entry> pool = cavalry
                ? MillagerEntityPool.CAVALRY
                : MillagerEntityPool.INFANTRY;

        int min = cavalry ? PatrolConfig.CAVALRY_MIN_SIZE : PatrolConfig.INFANTRY_MIN_SIZE;
        int max = cavalry ? PatrolConfig.CAVALRY_MAX_SIZE : PatrolConfig.INFANTRY_MAX_SIZE;
        int count = min + random.nextInt(max - min + 1);

        int initY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mPos.getX(), mPos.getZ());
        BlockPos basePos = new BlockPos(mPos.getX(), initY, mPos.getZ());
        DifficultyInstance difficulty = level.getCurrentDifficultyAt(basePos);
        UUID squadId = UUID.randomUUID();
        int spawnedCount = 0;

        boolean firstPlaced = false;
        for (int attempt = 0; attempt < 10 && !firstPlaced; attempt++) {
            if (millager$canSpawnAt(level, mPos)) {
                MillagerEntityPool.Entry entry = MillagerEntityPool.weightedPick(pool, random);
                if (millager$newMillager(level, mPos, basePos, difficulty, entry, squadId, true)) {
                    firstPlaced = true;
                    spawnedCount++;
                }
            } else {
                mPos.setX(mPos.getX() + random.nextInt(5) - random.nextInt(5));
                mPos.setZ(mPos.getZ() + random.nextInt(5) - random.nextInt(5));
            }
        }
        if (!firstPlaced) return;

        for (int i = 1; i < count; i++) {
            mPos.setX(mPos.getX() + random.nextInt(5) - random.nextInt(5));
            mPos.setZ(mPos.getZ() + random.nextInt(5) - random.nextInt(5));
            if (millager$canSpawnAt(level, mPos)) {
                MillagerEntityPool.Entry entry = MillagerEntityPool.weightedPick(pool, random);
                if (millager$newMillager(level, mPos, basePos, difficulty, entry, squadId,
                        spawnedCount % 5 == 0)) {
                    spawnedCount++;
                    if (spawnedCount % 5 == 0) squadId = UUID.randomUUID();
                }
            }
        }
    }

    @Unique
    private List<BlockPos> millager$findNearStructures(ServerLevel level, ResourceKey<Structure> key) {
        var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        var holderOpt = registry.get(key);
        if (holderOpt.isEmpty()) return List.of();
        Structure target = holderOpt.get().value();
        List<BlockPos> found = new ArrayList<>();

        int r = PatrolConfig.COMMAND_POST_SCAN_RADIUS;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) continue;
            ChunkPos playerChunk = player.chunkPosition();
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    var chunk = level.getChunk(playerChunk.x() + dx, playerChunk.z() + dz,
                            ChunkStatus.FULL, false);
                    if (chunk == null) continue;
                    StructureStart start = chunk.getAllStarts().get(target);
                    if (start == null || !start.isValid()) continue;
                    var bb = start.getBoundingBox();
                    found.add(new BlockPos(
                            (bb.minX() + bb.maxX()) / 2,
                            bb.minY(),
                            (bb.minZ() + bb.maxZ()) / 2));
                }
            }
        }
        return found;
    }

    @Unique
    private void millager$spawnCommandPostPatrol(ServerLevel level, RandomSource random, BlockPos cpCenter) {
        int d = PatrolConfig.COMMAND_POST_PATROL_NEAR_DIST;
        BlockPos.MutableBlockPos mPos = millager$getMutableBlockPos(level, random, cpCenter, d);
        if (mPos == null) return;
        int count = Math.min(PatrolConfig.COMMAND_POST_PATROL_SIZE,
                PatrolConfig.STRUCTURE_PATROL_CAP - millager$countNearbyMillagers(level, cpCenter));
        if (count <= 0) return;
        DifficultyInstance difficulty = level.getCurrentDifficultyAt(mPos);
        UUID squadId = UUID.randomUUID();
        int spawnedCount = 0;

        boolean firstPlaced = false;
        for (int attempt = 0; attempt < 10 && !firstPlaced; attempt++) {
            if (millager$canSpawnAt(level, mPos)) {
                MillagerEntityPool.Entry entry = MillagerEntityPool.weightedPick(MillagerEntityPool.INFANTRY, random);
                if (millager$newMillager(level, mPos, cpCenter, difficulty, entry, squadId, true)) {
                    firstPlaced = true;
                    spawnedCount++;
                }
            } else {
                mPos.setX(mPos.getX() + random.nextInt(5) - random.nextInt(5));
                mPos.setZ(mPos.getZ() + random.nextInt(5) - random.nextInt(5));
            }
        }
        if (!firstPlaced) return;

        for (int i = 1; i < count; i++) {
            mPos.setX(mPos.getX() + random.nextInt(5) - random.nextInt(5));
            mPos.setZ(mPos.getZ() + random.nextInt(5) - random.nextInt(5));
            if (millager$canSpawnAt(level, mPos)) {
                MillagerEntityPool.Entry entry = MillagerEntityPool.weightedPick(MillagerEntityPool.INFANTRY, random);
                if (millager$newMillager(level, mPos, cpCenter, difficulty, entry, squadId,
                        spawnedCount % 5 == 0)) {
                    spawnedCount++;
                    if (spawnedCount % 5 == 0) squadId = UUID.randomUUID();
                }
            }
        }
    }

    @Unique
    private void millager$spawnVanillaPatrol(ServerLevel level, RandomSource random, BlockPos cpCenter) {
        int d = PatrolConfig.RUINED_CP_PATROL_NEAR_DIST;
        BlockPos.MutableBlockPos mPos = millager$getMutableBlockPos(level, random, cpCenter, d);
        if (mPos == null) return;
        int count = Math.min(PatrolConfig.RUINED_CP_PATROL_SIZE_MIN
                        + random.nextInt(PatrolConfig.RUINED_CP_PATROL_SIZE_MAX - PatrolConfig.RUINED_CP_PATROL_SIZE_MIN + 1),
                PatrolConfig.STRUCTURE_PATROL_CAP - millager$countNearbyRaiders(level, cpCenter));
        if (count <= 0) return;
        DifficultyInstance difficulty = level.getCurrentDifficultyAt(mPos);

        boolean firstPlaced = false;
        for (int attempt = 0; attempt < 10 && !firstPlaced; attempt++) {
            if (millager$canSpawnAt(level, mPos)) {
                var pillager = EntityTypes.PILLAGER.create(level, EntitySpawnReason.EVENT);
                if (pillager == null) return;
                pillager.setPos(mPos.getX() + 0.5, mPos.getY(), mPos.getZ() + 0.5);
                pillager.finalizeSpawn(level, difficulty, EntitySpawnReason.EVENT, null);
                pillager.setPatrolLeader(true);
                pillager.setItemSlot(EquipmentSlot.HEAD, Raid.getOminousBannerInstance(pillager.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)));
                pillager.setDropChance(EquipmentSlot.HEAD, 2.0F);
                pillager.setPatrolTarget(cpCenter);
                level.addFreshEntity(pillager);
                firstPlaced = true;
            } else {
                mPos.setX(mPos.getX() + random.nextInt(5) - random.nextInt(5));
                mPos.setZ(mPos.getZ() + random.nextInt(5) - random.nextInt(5));
            }
        }
        if (!firstPlaced) return;

        for (int i = 1; i < count; i++) {
            mPos.setX(mPos.getX() + random.nextInt(5) - random.nextInt(5));
            mPos.setZ(mPos.getZ() + random.nextInt(5) - random.nextInt(5));
            if (millager$canSpawnAt(level, mPos)) {
                var pillager = EntityTypes.PILLAGER.create(level, EntitySpawnReason.EVENT);
                if (pillager == null) break;
                pillager.setPos(mPos.getX() + 0.5, mPos.getY(), mPos.getZ() + 0.5);
                pillager.finalizeSpawn(level, difficulty, EntitySpawnReason.EVENT, null);
                pillager.setPatrolTarget(cpCenter);
                level.addFreshEntity(pillager);
            }
        }
    }

    @Unique
    private static BlockPos.@Nullable MutableBlockPos millager$getMutableBlockPos(ServerLevel level, RandomSource random, BlockPos cpCenter, int d) {
        int distanceX = d <= 8 ? Math.max(0, d) : 8 + random.nextInt(d - 7);
        int distanceZ = d <= 8 ? Math.max(0, d) : 8 + random.nextInt(d - 7);
        int offsetX = distanceX * (random.nextBoolean() ? 1 : -1);
        int offsetZ = distanceZ * (random.nextBoolean() ? 1 : -1);

        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos(
                cpCenter.getX() + offsetX, 0, cpCenter.getZ() + offsetZ);

        if (millager$chunksUnloaded(level, mPos)) return null;

        int initY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mPos.getX(), mPos.getZ());
        mPos.setY(initY);
        return mPos;
    }

    @Unique
    private static boolean millager$chunksUnloaded(ServerLevel level, BlockPos.MutableBlockPos center) {
        int minCX = SectionPos.blockToSectionCoord(center.getX() - 10);
        int maxCX = SectionPos.blockToSectionCoord(center.getX() + 10);
        int minCZ = SectionPos.blockToSectionCoord(center.getZ() - 10);
        int maxCZ = SectionPos.blockToSectionCoord(center.getZ() + 10);
        for (int cx = minCX; cx <= maxCX; cx++) {
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                if (level.getChunk(cx, cz, ChunkStatus.FULL, false) == null) return true;
            }
        }
        return false;
    }

    @Unique
    private boolean millager$canSpawnAt(ServerLevel level, BlockPos.MutableBlockPos mPos) {
        mPos.setY(level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mPos.getX(), mPos.getZ()));
        return level.getFluidState(mPos).isEmpty()
                && level.getBlockState(mPos).isAir()
                && level.getBlockState(mPos.above()).isAir();
    }

    @Unique
    private static boolean millager$tooManyNearby(ServerLevel level, BlockPos center) {
        AABB box = new AABB(center).inflate(PatrolConfig.MILLAGER_CAP_RADIUS);
        return level.getEntitiesOfClass(AbstractMillager.class, box).size() >= PatrolConfig.MAX_NEARBY_MILLAGERS;
    }

    @Unique
    private static int millager$countNearbyMillagers(ServerLevel level, BlockPos center) {
        AABB box = new AABB(center).inflate(PatrolConfig.STRUCTURE_PATROL_CAP_RADIUS);
        return level.getEntitiesOfClass(AbstractMillager.class, box).size();
    }

    @Unique
    private static int millager$countNearbyRaiders(ServerLevel level, BlockPos center) {
        AABB box = new AABB(center).inflate(PatrolConfig.STRUCTURE_PATROL_CAP_RADIUS);
        return level.getEntitiesOfClass(Raider.class, box).size();
    }

    @Unique
    private boolean millager$newMillager(ServerLevel level, BlockPos.MutableBlockPos mPos, BlockPos basePos,
                                          DifficultyInstance difficulty, MillagerEntityPool.Entry entry, UUID squadId, boolean leader) {
        if (entry == null) return false;
        AbstractMillager entity = entry.type().create(level, EntitySpawnReason.EVENT);
        if (entity == null) return false;
        entity.setPos(mPos.getX() + 0.5, mPos.getY(), mPos.getZ() + 0.5);
        entity.setPatrolTarget(basePos);
        entity.finalizeSpawn(level, difficulty, EntitySpawnReason.EVENT, null);
        entity.setSquad(squadId, false);
        if (leader) entity.promoteToSquadLeader();
        level.addFreshEntity(entity);
        return true;
    }
}
