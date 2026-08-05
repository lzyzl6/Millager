package org.lzyzl.millager.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.lzyzl.millager.MillagerGameRules;
import org.lzyzl.millager.advancement.MillagerCriteria;
import org.lzyzl.millager.behavior.MillagerEntityPool;
import org.lzyzl.millager.behavior.raid.DefenderConfig;
import org.lzyzl.millager.behavior.raid.MillagerRaidsData;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.entity.millager.Scouter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin(Raid.class)
public class RaidDefenderMixin {

    @Shadow private boolean started;
    @Shadow private int groupsSpawned;
    @Shadow private int raidOmenLevel;
    @Shadow private BlockPos center;
    @Final @Shadow private int numGroups;
    @Final @Shadow private Map<Integer, Set<Raider>> groupRaiderMap;
    @Final @Shadow private ServerBossEvent raidEvent;
    @Final @Shadow private RandomSource random;

    @Unique private int millager$defenderTickTimer = 0;
    @Unique private int millager$maxTimer = 0;
    @Unique private int millager$defenderSpawnsCount = 0;
    @Unique private boolean millager$defenseStarted = false;
    @Unique private boolean millager$defenseComplete = false;
    @Unique private int millager$deployedDisplayTimer = 0;
    @Unique private ServerBossEvent millager$villagerBossEvent = null;
    @Unique private int millager$cachedValidBeds = 0;
    @Unique private int millager$bedCacheTimer = 0;
    @Unique private int millager$bossBarUpdateTimer = 0;
    @Unique private int millager$lastWaveHp = 0;
    @Unique private int millager$previousSquadCount = 0;
    @Unique private long millager$enemyWaveStart = 0L;
    @Unique private int millager$enemyWaveCount = 0;
    @Unique private double millager$weightedEnemyCount = 0.0D;
    @Unique private int millager$trackedEnemyWave = 0;
    @Unique private int millager$surgesUsed = 0;
    @Unique private boolean millager$surgePending = false;
    @Unique private int millager$failedSpawnAttempts = 0;
    @Unique private boolean millager$finalDeploymentDisplay = false;
    @Unique private boolean millager$grandBattleFired = false;
    @Unique private int millager$raidId = -1;
    @Unique private ServerLevel millager$level = null;

    @Inject(method = "tick", at = @At("HEAD"))
    private void millager$onTick(ServerLevel level, CallbackInfo ci) {
        millager$defenderTick(level);
    }

    @Unique
    private void millager$defenderTick(ServerLevel level) {
        Raid self = (Raid) (Object) this;
        millager$level = level;
        millager$raidId = level.getRaids().getId(self).orElse(millager$raidId);

        if (millager$defenseComplete && !millager$grandBattleFired && self.isVictory()) {
            millager$grandBattleFired = true;
            if (raidEvent != null) {
                for (ServerPlayer player : raidEvent.getPlayers()) {
                    MillagerCriteria.GRAND_BATTLE.get().trigger(player);
                }
            }
        }

        if (self.isOver()) {
            if (millager$defenseStarted) {
                MillagerRaidsData.getOrCreate(level).removeState(millager$raidId, center.asLong());
                millager$defenseStarted = false;
                millager$defenseComplete = false;
                if (millager$villagerBossEvent != null) {
                    millager$villagerBossEvent.removeAllPlayers();
                    millager$villagerBossEvent.setVisible(false);
                    millager$villagerBossEvent = null;
                }
                PlayerTeam defTeam = level.getScoreboard().getPlayerTeam(DefenderConfig.TEAM_NAME);
                if (defTeam != null && defTeam.getPlayers().isEmpty()) {
                    level.getScoreboard().removePlayerTeam(defTeam);
                }
            }
            return;
        }

        if (!level.getGameRules().get(MillagerGameRules.RAID_DEFENSES.get())) return;
        if (!this.started) return;

        if (!millager$defenseStarted && this.groupsSpawned >= 1) {
            millager$initDefender(level);
        }
        if (!millager$defenseStarted) return;

        millager$syncVillagerBossBar();

        if (!self.isActive()) return;
        millager$trackEnemyWave(level);
        if (millager$defenseComplete) {
            if (millager$finalDeploymentDisplay && --millager$deployedDisplayTimer <= 0) {
                millager$finishDeploymentDisplay();
            }
            return;
        }

        if (millager$bedCacheTimer > 0) {
            millager$bedCacheTimer--;
        } else {
            millager$cachedValidBeds = millager$countValidBeds(level);
            millager$bedCacheTimer = DefenderConfig.BED_CACHE_INTERVAL;
        }

        if (millager$deployedDisplayTimer > 0) {
            millager$deployedDisplayTimer--;
        }

        millager$bossBarUpdateTimer++;
        if (millager$bossBarUpdateTimer >= 20) {
            millager$bossBarUpdateTimer = 0;
            millager$updateVillagerBossBar();
            millager$saveState(level);
        }

        if (millager$deployedDisplayTimer > 0) return;
        if (raidEvent == null || raidEvent.getPlayers().isEmpty()) return;

        if (millager$defenderTickTimer > 0) {
            int dec = millager$getTickDecrement(level);
            millager$defenderTickTimer = Math.max(0, millager$defenderTickTimer - dec);
            if (millager$defenderTickTimer > 0) return;
        }

        boolean waveSpawned = millager$spawnWave(level);

        if (waveSpawned && millager$defenderSpawnsCount >= millager$getMaxReinforcementWaves()) {
            millager$defenseComplete = true;
            millager$finalDeploymentDisplay = true;
            millager$saveState(level);
        } else {
            millager$maxTimer = millager$computeMaxTimer();
            millager$defenderTickTimer = waveSpawned ? millager$maxTimer : 20;
        }
    }

    @Unique
    private void millager$initDefender(ServerLevel level) {
        millager$cachedValidBeds = millager$countValidBeds(level);
        millager$bedCacheTimer = DefenderConfig.BED_CACHE_INTERVAL;

        if (!millager$loadState(level)) {
            millager$maxTimer = millager$computeMaxTimer();
            millager$defenderTickTimer = millager$maxTimer;
        }
        millager$defenseComplete = millager$defenderSpawnsCount >= millager$getMaxReinforcementWaves();

        millager$villagerBossEvent = new ServerBossEvent(
                Component.translatable("raid.millager.reinforcements"),
                BossEvent.BossBarColor.GREEN,
                BossEvent.BossBarOverlay.PROGRESS
        );
        millager$villagerBossEvent.setProgress(0f);
        millager$villagerBossEvent.setVisible(true);
        millager$updateVillagerBossBar();

        if (raidEvent != null) {
            for (ServerPlayer player : raidEvent.getPlayers()) {
                millager$villagerBossEvent.addPlayer(player);
            }
        }

        millager$defenseStarted = true;
        millager$saveState(level);
    }

    @Unique
    private int millager$countValidBeds(ServerLevel level) {
        StructureStart start = level.structureManager().getStructureWithPieceAt(
                center, StructureTags.VILLAGE);
        int radius;
        if (start != StructureStart.INVALID_START) {
            var box = start.getBoundingBox();
            radius = Math.max((box.maxX() - box.minX()) / 2,
                    (box.maxZ() - box.minZ()) / 2) + 16;
        } else {
            radius = 64;
        }
        return (int) level.getPoiManager()
                .getInRange(h -> h.is(PoiTypes.HOME), center, radius, PoiManager.Occupancy.HAS_SPACE)
                .count();
    }

    @Unique
    private void millager$updateVillagerBossBar() {
        if (millager$villagerBossEvent == null) return;
        if (millager$defenseComplete) {
            millager$villagerBossEvent.setProgress(1.0f);
            millager$villagerBossEvent.setName(Component.translatable(millager$failedSpawnAttempts >= 3
                    ? "raid.millager.reinforcements.unavailable"
                    : "raid.millager.defense.complete"));
            return;
        }
        if (millager$deployedDisplayTimer > 0) {
            millager$villagerBossEvent.setProgress(1.0f);
            return;
        }
        float progress = millager$maxTimer > 0
                ? 1.0f - (float) millager$defenderTickTimer / millager$maxTimer
                : 0f;
        millager$villagerBossEvent.setProgress(Math.max(0f, Math.min(1f, progress)));
        millager$villagerBossEvent.setName(Component.translatable("raid.millager.reinforcements"));
    }

    @Unique
    private void millager$finishDeploymentDisplay() {
        millager$finalDeploymentDisplay = false;
        millager$deployedDisplayTimer = 0;
        millager$updateVillagerBossBar();
    }

    @Unique
    private void millager$syncVillagerBossBar() {
        if (millager$villagerBossEvent == null || raidEvent == null) return;
        millager$villagerBossEvent.setVisible(raidEvent.isVisible());
        Set<ServerPlayer> raidPlayers = new HashSet<>(raidEvent.getPlayers());
        Set<ServerPlayer> barPlayers = new HashSet<>(millager$villagerBossEvent.getPlayers());

        for (ServerPlayer p : raidPlayers) {
            if (!barPlayers.contains(p)) millager$villagerBossEvent.addPlayer(p);
        }
        for (ServerPlayer p : List.copyOf(barPlayers)) {
            if (!raidPlayers.contains(p)) millager$villagerBossEvent.removePlayer(p);
        }
    }

    @Unique
    private void millager$saveState(ServerLevel level) {
        if (!millager$defenseStarted) return;
        MillagerRaidsData.getOrCreate(level).setState(millager$raidId,
                new MillagerRaidsData.DefenseState(
                        this.groupsSpawned,
                        millager$defenderTickTimer,
                        millager$maxTimer,
                        millager$defenderSpawnsCount,
                        millager$deployedDisplayTimer,
                        millager$previousSquadCount,
                        millager$enemyWaveStart,
                        millager$enemyWaveCount,
                        millager$weightedEnemyCount,
                        millager$trackedEnemyWave,
                        millager$surgesUsed,
                        millager$surgePending,
                        millager$failedSpawnAttempts
                ));
    }

    @Unique
    private int millager$getMaxReinforcementWaves() {
        return this.numGroups;
    }

    @Unique
    private void millager$trackEnemyWave(ServerLevel level) {
        Raid self = (Raid) (Object) this;
        if (millager$enemyWaveCount <= 0 || millager$trackedEnemyWave != this.groupsSpawned
                || self.getTotalRaidersAlive() > 0) return;

        long elapsed = Math.max(0L, level.getGameTime() - millager$enemyWaveStart);
        double weightedEnemyCount = millager$weightedEnemyCount > 0.0D
                ? millager$weightedEnemyCount
                : millager$enemyWaveCount;
        long threshold = Math.round(millager$computeFastWaveSeconds(weightedEnemyCount) * 20.0D);
        int maxSurges = Math.max(0, (this.numGroups + 1) / 2);
        int finalWave = millager$getFinalEnemyWave();
        boolean hasNextWave = this.groupsSpawned < finalWave;
        boolean finalWaveNext = this.groupsSpawned == finalWave - 1;
        boolean fastEarlyWave = elapsed < threshold && millager$surgesUsed < maxSurges - 1;
        if (hasNextWave && (finalWaveNext || fastEarlyWave)) {
            millager$surgePending = true;
            millager$surgesUsed = Math.min(maxSurges, millager$surgesUsed + 1);
            if (raidEvent != null) {
                for (ServerPlayer player : raidEvent.getPlayers()) {
                    player.sendSystemMessage(Component.translatable("raid.millager.enemy_surge"));
                }
            }
        }
        millager$enemyWaveCount = 0;
        millager$weightedEnemyCount = 0.0D;
        millager$saveState(level);
    }

    @Unique
    private int millager$getFinalEnemyWave() {
        return this.numGroups + (this.raidOmenLevel > 1 ? 1 : 0);
    }

    @Unique
    private double millager$computeFastWaveSeconds(double enemyCount) {
        double firstSegment = Math.min(enemyCount, DefenderConfig.FAST_WAVE_FIRST_SEGMENT_SIZE);
        double secondSegment = Math.min(Math.max(enemyCount - firstSegment, 0.0D),
                DefenderConfig.FAST_WAVE_SECOND_SEGMENT_SIZE);
        double remaining = Math.max(enemyCount - firstSegment - secondSegment, 0.0D);
        return firstSegment * DefenderConfig.FAST_WAVE_FIRST_SEGMENT_SECONDS
                + secondSegment * DefenderConfig.FAST_WAVE_SECOND_SEGMENT_SECONDS
                + remaining * DefenderConfig.FAST_WAVE_REMAINING_SECONDS;
    }

    @Unique
    private double millager$getSurgeEnemyWeight(ServerLevel level) {
        return switch (level.getDifficulty().getId()) {
            case 1 -> DefenderConfig.SURGE_ENEMY_WEIGHT_EASY;
            case 3 -> DefenderConfig.SURGE_ENEMY_WEIGHT_HARD;
            default -> DefenderConfig.SURGE_ENEMY_WEIGHT_NORMAL;
        };
    }

    @Unique
    private int millager$getTickDecrement(ServerLevel level) {
        int diffId = Math.max(1, level.getDifficulty().getId());
        return switch (diffId) {
            case 1 -> DefenderConfig.TICK_DECREMENT_EASY;
            case 3 -> DefenderConfig.TICK_DECREMENT_HARD;
            default -> DefenderConfig.TICK_DECREMENT_NORMAL;
        };
    }

    @Unique
    private boolean millager$spawnWave(ServerLevel level) {
        int reinforcementWave = millager$defenderSpawnsCount + 1;

        int squadCount;
        if (reinforcementWave == 1) {
            squadCount = 1;
        } else {
            int baseSquads = Math.max(2, millager$previousSquadCount + 1);
            squadCount = Math.max(1, baseSquads + this.random.nextInt(
                    DefenderConfig.SQUAD_COUNT_VARIANCE * 2 + 1) - DefenderConfig.SQUAD_COUNT_VARIANCE);
        }
        squadCount = Math.min(squadCount, DefenderConfig.MAX_SQUADS_PER_WAVE);

        int totalHp = 0;
        int squadsSpawned = 0;
        double startAngle = this.random.nextDouble() * Math.PI * 2.0D;
        for (int squad = 0; squad < squadCount; squad++) {
            boolean cavalry = reinforcementWave == 1 || this.random.nextBoolean();
            double angle = startAngle + Math.PI * 2.0D * squad / squadCount;
            BlockPos pos = millager$findSquadSpawnPos(level, angle, cavalry);
            if (pos == null) continue;
            int squadHp = millager$spawnSquad(level, pos, cavalry);
            if (squadHp <= 0) continue;
            totalHp += squadHp;
            squadsSpawned++;
        }

        if (squadsSpawned == 0) {
            millager$failedSpawnAttempts++;
            millager$defenderTickTimer = 20;
            if (millager$failedSpawnAttempts >= 3) {
                millager$defenseComplete = true;
                millager$finalDeploymentDisplay = true;
                millager$deployedDisplayTimer = DefenderConfig.DEPLOYED_DISPLAY_TICKS;
                if (raidEvent != null) {
                    for (ServerPlayer player : raidEvent.getPlayers()) {
                        player.sendSystemMessage(Component.translatable("raid.millager.reinforcements.unavailable"));
                    }
                }
                millager$updateVillagerBossBar();
            }
            millager$saveState(level);
            return false;
        }

        millager$failedSpawnAttempts = 0;
        millager$defenderSpawnsCount = reinforcementWave;
        millager$previousSquadCount = squadsSpawned;
        millager$lastWaveHp = totalHp;

        millager$deployedDisplayTimer = DefenderConfig.DEPLOYED_DISPLAY_TICKS;
        if (millager$villagerBossEvent != null) {
            millager$villagerBossEvent.setProgress(1.0f);
            millager$villagerBossEvent.setName(Component.translatable(
                    "raid.millager.deployed.squads", squadsSpawned));
        }
        return true;
    }

    @Unique
    private BlockPos millager$findSquadSpawnPos(ServerLevel level, double angle, boolean cavalry) {
        for (int attempt = 0; attempt < DefenderConfig.SPAWN_SEARCH_ATTEMPTS; attempt++) {
            int distance = DefenderConfig.SQUAD_SPAWN_MIN_DISTANCE + this.random.nextInt(
                    DefenderConfig.SQUAD_SPAWN_MAX_DISTANCE - DefenderConfig.SQUAD_SPAWN_MIN_DISTANCE + 1);
            double spread = (this.random.nextDouble() - 0.5D) * 0.7D;
            int x = center.getX() + (int) Math.round(Math.cos(angle + spread) * distance);
            int z = center.getZ() + (int) Math.round(Math.sin(angle + spread) * distance);
            int chunkX = SectionPos.blockToSectionCoord(x);
            int chunkZ = SectionPos.blockToSectionCoord(z);
            if (level.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false) == null) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos candidate = new BlockPos(x, y, z);
            if (Math.abs(y - center.getY()) > DefenderConfig.MAX_HEIGHT_DIFF) continue;
            if (!level.getFluidState(candidate).isEmpty()) continue;
            if (!level.getBlockState(candidate.below()).isSolidRender()) continue;
            if (!level.getBlockState(candidate).isAir()) continue;
            if (!level.getBlockState(candidate.above()).isAir()) continue;
            if (cavalry && millager$hasCavalrySpawnObstruction(level, candidate)) continue;
            return candidate;
        }
        return null;
    }

    @Unique
    private int millager$computeMaxTimer() {
        int baseReal = Math.max(0, DefenderConfig.TIMER_BASE_REAL
                - millager$cachedValidBeds * DefenderConfig.TIMER_PER_BED_REAL);
        return Math.max(DefenderConfig.TIMER_REAL_MIN,
                Math.min(DefenderConfig.TIMER_REAL_MAX, baseReal + millager$lastWaveHp));
    }

    @Unique
    private boolean millager$loadState(ServerLevel level) {
        MillagerRaidsData.DefenseState state = MillagerRaidsData.getOrCreate(level)
                .getStateOrNull(millager$raidId, center.asLong());
        if (state == null) return false;
        millager$defenderTickTimer = state.timer();
        millager$maxTimer = state.maxTimer();
        millager$defenderSpawnsCount = state.spawns();
        millager$deployedDisplayTimer = state.deployedDisplay();
        millager$previousSquadCount = state.previousSquads();
        millager$enemyWaveStart = state.enemyWaveStart();
        millager$enemyWaveCount = state.enemyWaveCount();
        millager$weightedEnemyCount = state.weightedEnemyCount() > 0.0D
                ? state.weightedEnemyCount()
                : state.enemyWaveCount();
        millager$trackedEnemyWave = state.trackedEnemyWave();
        millager$surgesUsed = state.surgesUsed();
        millager$surgePending = state.surgePending();
        millager$failedSpawnAttempts = state.failedSpawnAttempts();
        return true;
    }

    @Unique private PlayerTeam millager$getMillagerTeam(Scoreboard scoreboard) {
        PlayerTeam defTeam = scoreboard.getPlayerTeam(DefenderConfig.TEAM_NAME);
        if (defTeam == null) {
            defTeam = scoreboard.addPlayerTeam(DefenderConfig.TEAM_NAME);
            defTeam.setColor(ChatFormatting.GREEN);
        }
        return defTeam;
    }

    @Unique
    private int millager$spawnSquad(ServerLevel level, BlockPos pos, boolean cavalry) {
        List<MillagerEntityPool.Entry> pool = cavalry
                ? MillagerEntityPool.CAVALRY
                : MillagerEntityPool.INFANTRY;
        int count = DefenderConfig.SQUAD_MIN_SIZE + this.random.nextInt(
                DefenderConfig.SQUAD_MAX_SIZE - DefenderConfig.SQUAD_MIN_SIZE + 1);
        DifficultyInstance difficulty = level.getCurrentDifficultyAt(pos);
        Scoreboard scoreboard = level.getScoreboard();
        UUID squadId = UUID.randomUUID();

        int totalHp = 0;
        int spawnedCount = 0;
        for (int i = 0; i < count; i++) {
            BlockPos memberPos = millager$findSquadMemberSpawnPos(level, pos, cavalry);
            if (memberPos == null) continue;

            MillagerEntityPool.Entry entry = MillagerEntityPool.weightedPick(pool, this.random);
            if (entry == null) break;

            AbstractMillager entity = entry.type().create(level, EntitySpawnReason.EVENT);
            if (entity == null) break;

            entity.setPos(memberPos.getX() + 0.5, memberPos.getY(), memberPos.getZ() + 0.5);
            entity.finalizeSpawn(level, difficulty, EntitySpawnReason.EVENT, null);
            entity.setRaidReinforcementCenter(center);
            entity.setSquad(squadId, spawnedCount == 0);
            entity.setPersistenceRequired();
            if (spawnedCount == 0) {
                entity.equipSquadLeaderBanner();
            }
            level.addFreshEntityWithPassengers(entity);
            spawnedCount++;

            entity.setPatrolTarget(center);

            scoreboard.addPlayerToTeam(entity.getScoreboardName(), millager$getMillagerTeam(scoreboard));

            if (entity instanceof Scouter scouter) {
                scouter.setPendingRaidToot(true);
            }

            totalHp += (int) entry.maxHealth() * difficulty.getDifficulty().getId();
        }
        return totalHp;
    }

    @Unique
    private BlockPos millager$findSquadMemberSpawnPos(ServerLevel level, BlockPos origin, boolean cavalry) {
        if (!cavalry) {
            int x = origin.getX() + this.random.nextInt(5) - 2;
            int z = origin.getZ() + this.random.nextInt(5) - 2;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos candidate = new BlockPos(x, y, z);
            return level.getFluidState(candidate).isEmpty()
                    && level.getBlockState(candidate).isAir()
                    && level.getBlockState(candidate.above()).isAir() ? candidate : origin;
        }
        int radius = DefenderConfig.SQUAD_MEMBER_SPAWN_RADIUS;
        for (int attempt = 0; attempt < DefenderConfig.SPAWN_SEARCH_ATTEMPTS; attempt++) {
            int x = attempt == 0 ? origin.getX() : origin.getX() + this.random.nextInt(radius * 2 + 1) - radius;
            int z = attempt == 0 ? origin.getZ() : origin.getZ() + this.random.nextInt(radius * 2 + 1) - radius;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos candidate = new BlockPos(x, y, z);
            if (!level.getFluidState(candidate).isEmpty()) continue;
            if (!level.getBlockState(candidate.below()).isSolidRender()) continue;
            if (!level.getBlockState(candidate).isAir()) continue;
            if (!level.getBlockState(candidate.above()).isAir()) continue;
            if (millager$hasCavalrySpawnObstruction(level, candidate)) continue;
            return candidate;
        }
        return null;
    }

    @Unique
    private boolean millager$hasCavalrySpawnObstruction(ServerLevel level, BlockPos pos) {
        AABB box = EntityType.HORSE.getDimensions().makeBoundingBox(
                pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D).inflate(
                DefenderConfig.CAVALRY_SPAWN_CLEARANCE, 0.0D, DefenderConfig.CAVALRY_SPAWN_CLEARANCE);
        return !level.noCollision(box);
    }

    @Inject(method = "spawnGroup", at = @At("TAIL"))
    private void millager$afterEnemyWaveSpawned(ServerLevel level, BlockPos pos, CallbackInfo ci) {
        if (!level.getGameRules().get(MillagerGameRules.RAID_DEFENSES.get())) return;
        Set<Raider> group = this.groupRaiderMap.get(this.groupsSpawned);
        if (group == null || group.isEmpty()) return;
        int baseEnemyCount = group.size();
        if (millager$surgePending) {
            millager$spawnSurgeEnemies(level, pos, group);
            millager$surgePending = false;
        }
        if (this.groupsSpawned >= millager$getFinalEnemyWave()) {
            millager$enemyWaveCount = 0;
            millager$weightedEnemyCount = 0.0D;
            millager$trackedEnemyWave = this.groupsSpawned;
            if (millager$defenseStarted) millager$saveState(level);
            return;
        }
        millager$enemyWaveStart = level.getGameTime();
        millager$enemyWaveCount = baseEnemyCount;
        millager$weightedEnemyCount = baseEnemyCount
                + Math.max(0, group.size() - baseEnemyCount) * millager$getSurgeEnemyWeight(level);
        millager$trackedEnemyWave = this.groupsSpawned;
        if (millager$defenseStarted) millager$saveState(level);
    }

    @Unique
    private void millager$spawnSurgeEnemies(ServerLevel level, BlockPos pos, Set<Raider> group) {
        List<Raider> templates = group.stream()
                .filter(raider -> !raider.getType().equals(EntityType.RAVAGER))
                .toList();
        int baseCount = group.size();
        int extraCount = level.getDifficulty().getId() == 3
                ? baseCount
                : (baseCount + 1) / 2;

        for (int i = 0; i < extraCount; i++) {
            Raider extra = null;
            if (!templates.isEmpty()) {
                var created = templates.get(this.random.nextInt(templates.size()))
                        .getType().create(level, EntitySpawnReason.EVENT);
                if (created instanceof Raider raider) extra = raider;
            }
            if (extra == null) extra = EntityType.PILLAGER.create(level, EntitySpawnReason.EVENT);
            if (extra == null) break;
            ((Raid) (Object) this).joinRaid(level, this.groupsSpawned, extra, pos, false);
        }

        if (level.getDifficulty().getId() == 2) {
            Raider ravager = EntityType.RAVAGER.create(level, EntitySpawnReason.EVENT);
            Raider rider = EntityType.PILLAGER.create(level, EntitySpawnReason.EVENT);
            if (ravager != null) {
                Raid raid = ((Raid) (Object) this);
                raid.joinRaid(level, this.groupsSpawned, ravager, pos, false);
                if (rider != null) {
                    raid.joinRaid(level, this.groupsSpawned, rider, pos, false);
                    rider.startRiding(ravager, false, false);
                }
            }
        }
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void millager$onStop(CallbackInfo ci) {
        if (millager$defenseStarted && millager$level != null) {
            MillagerRaidsData.getOrCreate(millager$level)
                    .removeState(millager$raidId, center.asLong());
            millager$defenseStarted = false;
            millager$defenseComplete = false;
        }
        if (millager$villagerBossEvent != null) {
            millager$villagerBossEvent.removeAllPlayers();
            millager$villagerBossEvent.setVisible(false);
            millager$villagerBossEvent = null;
        }
    }
}
