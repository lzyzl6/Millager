package org.lzyzl.millager.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.lzyzl.millager.MillagerGameRules;
import org.lzyzl.millager.MillagerSounds;
import org.lzyzl.millager.advancement.MillagerCriteria;
import org.lzyzl.millager.behavior.raid.DefenderConfig;
import org.lzyzl.millager.behavior.raid.MillagerRaidsData;
import org.lzyzl.millager.behavior.raid.RaidReinforcementSpawner;
import org.lzyzl.millager.entity.millager.RaidHornPlayer;
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

@Mixin(Raid.class)
public class RaidDefenderMixin implements RaidHornPlayer {

    @Override
    @Unique
    public void millager$playReinforcementHorn(ServerLevel level, BlockPos sourcePos) {
        Vec3 source = Vec3.atCenterOf(sourcePos);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(source) > 4096.0D && !this.raidEvent.getPlayers().contains(player)) continue;
            double deltaX = source.x - player.getX();
            double deltaZ = source.z - player.getZ();
            double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            if (distance == 0.0D) continue;
            player.connection.send(new ClientboundSoundPacket(
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(MillagerSounds.REINFORCE_HORN),
                    SoundSource.NEUTRAL,
                    player.getX() + deltaX * 13.0D / distance,
                    player.getY(),
                    player.getZ() + deltaZ * 13.0D / distance,
                    64.0F,
                    0.8F,
                    this.random.nextLong()
            ));
        }
    }

    @Shadow private boolean started;
    @Shadow private int groupsSpawned;
    @Shadow private int badOmenLevel;
    @Shadow private BlockPos center;
    @Final @Shadow private int numGroups;
    @Final @Shadow private Map<Integer, Set<Raider>> groupRaiderMap;
    @Final @Shadow private ServerBossEvent raidEvent;
    @Final @Shadow private RandomSource random;
    @Final @Shadow private ServerLevel level;

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
    @Unique private ServerLevel millager$level = null;

    @Inject(method = "tick", at = @At("HEAD"))
    private void millager$onTick(CallbackInfo ci) {
        millager$defenderTick(this.level);
    }

    @Unique
    private void millager$defenderTick(ServerLevel level) {
        Raid self = (Raid) (Object) this;
        millager$level = level;

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
                MillagerRaidsData.getOrCreate(level).removeState(self.getId(), center.asLong());
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

        if (!level.getGameRules().getRule(MillagerGameRules.RAID_DEFENSES).get()) return;
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
            millager$defenderTickTimer = waveSpawned ? millager$maxTimer : DefenderConfig.SPAWN_FAILURE_RETRY_TICKS;
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
        return RaidReinforcementSpawner.countValidBeds(level, center);
    }

    @Unique
    private void millager$updateVillagerBossBar() {
        if (millager$villagerBossEvent == null) return;
        if (millager$defenseComplete) {
            millager$villagerBossEvent.setProgress(1.0f);
            millager$villagerBossEvent.setName(Component.translatable(millager$failedSpawnAttempts >= DefenderConfig.MAX_CONSECUTIVE_SPAWN_FAILURES
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
        MillagerRaidsData.getOrCreate(level).setState(((Raid) (Object) this).getId(),
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
        return this.numGroups + (this.badOmenLevel > 1 ? 1 : 0);
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
        return RaidReinforcementSpawner.getTickDecrement(level);
    }

    @Unique
    private boolean millager$spawnWave(ServerLevel level) {
        int reinforcementWave = millager$defenderSpawnsCount + 1;
        Scoreboard scoreboard = level.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(DefenderConfig.TEAM_NAME);
        if (team == null) {
            team = scoreboard.addPlayerTeam(DefenderConfig.TEAM_NAME);
            team.setColor(ChatFormatting.GREEN);
        }
        RaidReinforcementSpawner.WaveResult result = RaidReinforcementSpawner.spawnWave(level, center, this.random,
                reinforcementWave, millager$previousSquadCount, team);
        int squadsSpawned = result.squadsSpawned();

        if (squadsSpawned == 0) {
            millager$failedSpawnAttempts++;
            millager$defenderTickTimer = DefenderConfig.SPAWN_FAILURE_RETRY_TICKS;
            if (millager$failedSpawnAttempts >= DefenderConfig.MAX_CONSECUTIVE_SPAWN_FAILURES) {
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
        millager$lastWaveHp = result.totalHp();

        millager$deployedDisplayTimer = DefenderConfig.DEPLOYED_DISPLAY_TICKS;
        if (millager$villagerBossEvent != null) {
            millager$villagerBossEvent.setProgress(1.0f);
            millager$villagerBossEvent.setName(Component.translatable(
                    "raid.millager.deployed.squads", squadsSpawned));
        }
        return true;
    }

    @Unique
    private int millager$computeMaxTimer() {
        return RaidReinforcementSpawner.computeMaxTimer(millager$cachedValidBeds, millager$lastWaveHp);
    }

    @Unique
    private boolean millager$loadState(ServerLevel level) {
        MillagerRaidsData.DefenseState state = MillagerRaidsData.getOrCreate(level)
                .getStateOrNull(((Raid) (Object) this).getId(), center.asLong());
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

    @Inject(method = "spawnGroup", at = @At("TAIL"))
    private void millager$afterEnemyWaveSpawned(BlockPos pos, CallbackInfo ci) {
        if (!this.level.getGameRules().getRule(MillagerGameRules.RAID_DEFENSES).get()) return;
        Set<Raider> group = this.groupRaiderMap.get(this.groupsSpawned);
        if (group == null || group.isEmpty()) return;
        int baseEnemyCount = group.size();
        if (millager$surgePending) {
            millager$spawnSurgeEnemies(this.level, pos, group);
            millager$surgePending = false;
        }
        if (this.groupsSpawned >= millager$getFinalEnemyWave()) {
            millager$enemyWaveCount = 0;
            millager$weightedEnemyCount = 0.0D;
            millager$trackedEnemyWave = this.groupsSpawned;
            if (millager$defenseStarted) millager$saveState(this.level);
            return;
        }
        millager$enemyWaveStart = this.level.getGameTime();
        millager$enemyWaveCount = baseEnemyCount;
        millager$weightedEnemyCount = baseEnemyCount
                + Math.max(0, group.size() - baseEnemyCount) * millager$getSurgeEnemyWeight(this.level);
        millager$trackedEnemyWave = this.groupsSpawned;
        if (millager$defenseStarted) millager$saveState(this.level);
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
                        .getType().create(level);
                if (created instanceof Raider raider) extra = raider;
            }
            if (extra == null) extra = EntityType.PILLAGER.create(level);
            if (extra == null) break;
            ((Raid) (Object) this).joinRaid(this.groupsSpawned, extra, pos, false);
        }

        if (level.getDifficulty().getId() == 2) {
            Raider ravager = EntityType.RAVAGER.create(level);
            Raider rider = EntityType.PILLAGER.create(level);
            if (ravager != null) {
                Raid raid = ((Raid) (Object) this);
                raid.joinRaid(this.groupsSpawned, ravager, pos, false);
                if (rider != null) {
                    raid.joinRaid(this.groupsSpawned, rider, pos, false);
                    rider.startRiding(ravager);
                }
            }
        }
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void millager$onStop(CallbackInfo ci) {
        if (millager$defenseStarted && millager$level != null) {
            MillagerRaidsData.getOrCreate(millager$level)
                    .removeState(((Raid) (Object) this).getId(), center.asLong());
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
