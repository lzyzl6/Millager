package org.lzyzl.millager.behavior.raid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.behavior.MillagerEntityPool;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.entity.millager.Rider;
import org.lzyzl.millager.entity.millager.Scouter;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class RaidReinforcementSpawner {

    private RaidReinforcementSpawner() {
    }

    public static WaveResult spawnWave(ServerLevel level, BlockPos center, RandomSource random, int wave,
                                       int previousSquadCount, PlayerTeam team) {
        return spawnWave(level, center, random, wave, previousSquadCount, team, entity -> {});
    }

    public static WaveResult spawnWave(ServerLevel level, BlockPos center, RandomSource random, int wave,
                                       int previousSquadCount, PlayerTeam team,
                                       Consumer<AbstractMillager> initializer) {
        int squadCount;
        if (wave == 1) {
            squadCount = 1;
        } else {
            int baseSquads = Math.max(2, previousSquadCount + 1);
            squadCount = Math.max(1, baseSquads + random.nextInt(
                    DefenderConfig.SQUAD_COUNT_VARIANCE * 2 + 1) - DefenderConfig.SQUAD_COUNT_VARIANCE);
        }
        squadCount = Math.min(squadCount, DefenderConfig.MAX_SQUADS_PER_WAVE);

        int totalHp = 0;
        int squadsSpawned = 0;
        double startAngle = random.nextDouble() * Math.PI * 2.0D;
        for (int squad = 0; squad < squadCount; squad++) {
            boolean cavalry = wave == 1 || random.nextBoolean();
            double angle = startAngle + Math.PI * 2.0D * squad / squadCount;
            BlockPos pos = findSquadSpawnPos(level, center, random, angle, cavalry);
            if (pos == null) continue;
            int squadHp = spawnSquad(level, center, pos, random, cavalry, team, initializer);
            if (squadHp <= 0) continue;
            totalHp += squadHp;
            squadsSpawned++;
        }
        return new WaveResult(squadsSpawned, totalHp);
    }

    public static int countValidBeds(ServerLevel level, BlockPos center) {
        StructureStart start = level.structureManager().getStructureWithPieceAt(center, StructureTags.VILLAGE);
        int radius;
        if (start != StructureStart.INVALID_START) {
            var box = start.getBoundingBox();
            radius = Math.max((box.maxX() - box.minX()) / 2, (box.maxZ() - box.minZ()) / 2) + 16;
        } else {
            radius = 64;
        }
        return (int) level.getPoiManager()
                .getInRange(holder -> holder.is(PoiTypes.HOME), center, radius, PoiManager.Occupancy.HAS_SPACE)
                .count();
    }

    public static int computeMaxTimer(int validBeds, int lastWaveHp) {
        int base = Math.max(0, DefenderConfig.TIMER_BASE_REAL - validBeds * DefenderConfig.TIMER_PER_BED_REAL);
        return Math.max(DefenderConfig.TIMER_REAL_MIN,
                Math.min(DefenderConfig.TIMER_REAL_MAX, base + lastWaveHp));
    }

    public static int getTickDecrement(ServerLevel level) {
        return switch (Math.max(1, level.getDifficulty().getId())) {
            case 1 -> DefenderConfig.TICK_DECREMENT_EASY;
            case 3 -> DefenderConfig.TICK_DECREMENT_HARD;
            default -> DefenderConfig.TICK_DECREMENT_NORMAL;
        };
    }

    public static int getMaxWaves(ServerLevel level) {
        return switch (level.getDifficulty().getId()) {
            case 1 -> 3;
            case 2 -> 5;
            case 3 -> 7;
            default -> 0;
        };
    }

    private static @Nullable BlockPos findSquadSpawnPos(ServerLevel level, BlockPos center, RandomSource random,
                                                         double angle, boolean cavalry) {
        for (int attempt = 0; attempt < DefenderConfig.SPAWN_SEARCH_ATTEMPTS; attempt++) {
            int distance = DefenderConfig.SQUAD_SPAWN_MIN_DISTANCE + random.nextInt(
                    DefenderConfig.SQUAD_SPAWN_MAX_DISTANCE - DefenderConfig.SQUAD_SPAWN_MIN_DISTANCE + 1);
            double spread = (random.nextDouble() - 0.5D) * 0.7D;
            int x = center.getX() + (int) Math.round(Math.cos(angle + spread) * distance);
            int z = center.getZ() + (int) Math.round(Math.sin(angle + spread) * distance);
            if (level.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z),
                    ChunkStatus.FULL, false) == null) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos candidate = new BlockPos(x, y, z);
            if (Math.abs(y - center.getY()) > DefenderConfig.MAX_HEIGHT_DIFF) continue;
            if (!level.getFluidState(candidate).isEmpty()) continue;
            if (!level.getBlockState(candidate.below()).isSolidRender(level, candidate.below())) continue;
            if (!level.getBlockState(candidate).isAir()) continue;
            if (!level.getBlockState(candidate.above()).isAir()) continue;
            if (cavalry && hasCavalrySpawnObstruction(level, candidate)) continue;
            return candidate;
        }
        return null;
    }

    private static int spawnSquad(ServerLevel level, BlockPos center, BlockPos pos, RandomSource random,
                                  boolean cavalry, PlayerTeam team, Consumer<AbstractMillager> initializer) {
        List<MillagerEntityPool.Entry> pool = cavalry ? MillagerEntityPool.CAVALRY : MillagerEntityPool.INFANTRY;
        int count = DefenderConfig.SQUAD_MIN_SIZE + random.nextInt(
                DefenderConfig.SQUAD_MAX_SIZE - DefenderConfig.SQUAD_MIN_SIZE + 1);
        DifficultyInstance difficulty = level.getCurrentDifficultyAt(pos);
        UUID squadId = UUID.randomUUID();
        int totalHp = 0;
        int spawnedCount = 0;
        for (int i = 0; i < count; i++) {
            BlockPos memberPos = findSquadMemberSpawnPos(level, pos, random, cavalry);
            if (memberPos == null) continue;
            MillagerEntityPool.Entry entry = MillagerEntityPool.weightedPick(pool, random);
            if (entry == null) break;
            AbstractMillager entity = entry.type().create(level);
            if (entity == null) break;
            entity.moveTo(memberPos.getX() + 0.5D, memberPos.getY(), memberPos.getZ() + 0.5D,
                    random.nextFloat() * 360.0F, 0.0F);
            entity.finalizeSpawn(level, difficulty, MobSpawnType.EVENT, null, null);
            ensureCavalryHorseArmor(entity, random);
            entity.setRaidReinforcementCenter(center);
            entity.setPatrolTarget(center);
            entity.setSquad(squadId, spawnedCount == 0);
            entity.setPersistenceRequired();
            initializer.accept(entity);
            if (spawnedCount == 0) entity.equipSquadLeaderBanner();
            level.addFreshEntityWithPassengers(entity);
            level.getScoreboard().addPlayerToTeam(entity.getScoreboardName(), team);
            if (entity instanceof Scouter scouter) scouter.setPendingRaidToot(true);
            totalHp += (int) entry.maxHealth() * difficulty.getDifficulty().getId();
            spawnedCount++;
        }
        return totalHp;
    }

    private static void ensureCavalryHorseArmor(AbstractMillager entity, RandomSource random) {
        if (entity.getVehicle() instanceof Horse horse && horse.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            horse.inventory.setItem(1, new ItemStack(Rider.getRandomHorseArmor(random, 3)));
        }
    }

    private static @Nullable BlockPos findSquadMemberSpawnPos(ServerLevel level, BlockPos origin, RandomSource random,
                                                               boolean cavalry) {
        if (!cavalry) {
            int x = origin.getX() + random.nextInt(5) - 2;
            int z = origin.getZ() + random.nextInt(5) - 2;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos candidate = new BlockPos(x, y, z);
            return level.getFluidState(candidate).isEmpty()
                    && level.getBlockState(candidate).isAir()
                    && level.getBlockState(candidate.above()).isAir() ? candidate : origin;
        }
        int radius = DefenderConfig.SQUAD_MEMBER_SPAWN_RADIUS;
        for (int attempt = 0; attempt < DefenderConfig.SPAWN_SEARCH_ATTEMPTS; attempt++) {
            int x = attempt == 0 ? origin.getX() : origin.getX() + random.nextInt(radius * 2 + 1) - radius;
            int z = attempt == 0 ? origin.getZ() : origin.getZ() + random.nextInt(radius * 2 + 1) - radius;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos candidate = new BlockPos(x, y, z);
            if (!level.getFluidState(candidate).isEmpty()) continue;
            if (!level.getBlockState(candidate.below()).isSolidRender(level, candidate.below())) continue;
            if (!level.getBlockState(candidate).isAir()) continue;
            if (!level.getBlockState(candidate.above()).isAir()) continue;
            if (hasCavalrySpawnObstruction(level, candidate)) continue;
            return candidate;
        }
        return null;
    }

    private static boolean hasCavalrySpawnObstruction(ServerLevel level, BlockPos pos) {
        AABB box = EntityType.HORSE.getDimensions().makeBoundingBox(
                pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D).inflate(
                DefenderConfig.CAVALRY_SPAWN_CLEARANCE, 0.0D, DefenderConfig.CAVALRY_SPAWN_CLEARANCE);
        return !level.noCollision(box);
    }

    public record WaveResult(int squadsSpawned, int totalHp) {
    }
}
