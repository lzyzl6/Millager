package org.lzyzl.millager.compat.goety;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.MillagerGameRules;
import org.lzyzl.millager.behavior.MiscConfig;
import org.lzyzl.millager.behavior.raid.DefenderConfig;
import org.lzyzl.millager.behavior.raid.GoetyRaidsData;
import org.lzyzl.millager.behavior.raid.RaidReinforcementSpawner;
import org.lzyzl.millager.entity.golem.BeeGolem;
import org.lzyzl.millager.entity.millager.AbstractMillager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class GoetyReinforcementController {

    private static final String TEAM_NAME = "millager_goety_raid";
    private static final int PARTICIPANT_SCAN_INTERVAL = 20;
    private static final int TARGET_REFRESH_INTERVAL = 100;
    private static final Map<ServerLevel, Map<UUID, ServerBossEvent>> BOSS_EVENTS = new WeakHashMap<>();

    private GoetyReinforcementController() {
    }

    public static boolean startRaid(ServerLevel level, UUID playerId, BlockPos center) {
        if (!MiscConfig.ENABLE_GOETY_RAIDS
                || !level.getGameRules().getRule(MillagerGameRules.RAID_DEFENSES).get()) return false;
        GoetyRaidsData data = GoetyRaidsData.getOrCreate(level);
        if (data.getState(playerId) != null) return true;
        int maxWaves = RaidReinforcementSpawner.getMaxWaves(level);
        if (maxWaves == 0) return false;
        int validBeds = RaidReinforcementSpawner.countValidBeds(level, center);
        int maxTimer = RaidReinforcementSpawner.computeMaxTimer(validBeds, 0);
        data.setState(playerId, new GoetyRaidsData.ActiveRaidState(center.asLong(), maxWaves, maxTimer, maxTimer,
                0, 0, 0, 0, validBeds, DefenderConfig.BED_CACHE_INTERVAL, 0, 0, true, false, false));
        return true;
    }

    public static void tickServer(MinecraftServer server) {
        if (GoetyCompat.isUnavailable()) return;
        for (ServerLevel level : server.getAllLevels()) tickLevel(server, level);
    }

    private static void tickLevel(MinecraftServer server, ServerLevel level) {
        GoetyRaidsData data = GoetyRaidsData.getOrCreate(level);
        for (UUID playerId : data.activePlayers()) {
            GoetyRaidsData.ActiveRaidState state = data.getState(playerId);
            if (state == null) continue;
            BlockPos center = BlockPos.of(state.center());
            if (!MiscConfig.ENABLE_GOETY_RAIDS) {
                finishRaid(level, data, playerId, center);
                continue;
            }
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            boolean ownerNearby = player != null && player.level() == level && GoetyCompat.isRaidOwnerPresent(player, playerId)
                    && player.distanceToSqr(center.getX() + 0.5D, player.getY(), center.getZ() + 0.5D) <= 16384.0D;
            ServerBossEvent bossEvent = getBossEvent(level, playerId);
            syncBossEvent(bossEvent, ownerNearby ? player : null);
            boolean enabled = level.getGameRules().getRule(MillagerGameRules.RAID_DEFENSES).get();
            bossEvent.setVisible(enabled && ownerNearby);
            updateBossEvent(bossEvent, state);
            if (state.resultDisplay() > 0) {
                if (!enabled || !ownerNearby) continue;
                int resultDisplay = state.resultDisplay() - 1;
                if (resultDisplay == 0) {
                    finishRaid(level, data, playerId, center);
                } else {
                    GoetyRaidsData.ActiveRaidState next = new GoetyRaidsData.ActiveRaidState(state.center(),
                            state.maxWaves(), state.timer(), state.maxTimer(), state.waves(), state.deployedDisplay(),
                            state.previousSquads(), state.lastWaveHp(), state.validBeds(), state.bedCacheTimer(),
                            state.failedSpawnAttempts(), resultDisplay, state.playerVictory(), state.complete(),
                            state.goodwillCleared());
                    data.setState(playerId, next);
                    updateBossEvent(bossEvent, next);
                }
                continue;
            }

            List<LivingEntity> targets = GoetyCompat.getOwnedRaidingServants(level, playerId, center);
            if (targets.isEmpty()) {
                releaseReinforcements(level, center, playerId);
                GoetyRaidsData.ActiveRaidState next = new GoetyRaidsData.ActiveRaidState(state.center(),
                        state.maxWaves(), state.timer(), state.maxTimer(), state.waves(), state.deployedDisplay(),
                        state.previousSquads(), state.lastWaveHp(), state.validBeds(), state.bedCacheTimer(),
                        state.failedSpawnAttempts(), DefenderConfig.DEPLOYED_DISPLAY_TICKS,
                        isVillageConquered(level, center), state.complete(), state.goodwillCleared());
                data.setState(playerId, next);
                updateBossEvent(bossEvent, next);
                continue;
            }
            if (!enabled || !ownerNearby) continue;
            if (!state.goodwillCleared()) {
                GoetyCompat.clearGoodwillAndNotify(player);
                state = state.withGoodwillCleared();
                data.setState(playerId, state);
            }
            if (GoetyCompat.isRaidTarget(player, playerId)) targets.add(0, player);
            long targetTick = level.getGameTime() + playerId.hashCode();
            if (Math.floorMod(targetTick, PARTICIPANT_SCAN_INTERVAL) == 0) {
                recruitNeutralMillagers(level, center, playerId, targets);
            }
            if (Math.floorMod(targetTick, TARGET_REFRESH_INTERVAL) == 0) {
                retargetReinforcements(level, center, playerId, targets);
            }
            if (state.complete()) {
                if (state.deployedDisplay() > 0
                        && state.failedSpawnAttempts() < DefenderConfig.MAX_CONSECUTIVE_SPAWN_FAILURES) {
                    GoetyRaidsData.ActiveRaidState next = new GoetyRaidsData.ActiveRaidState(state.center(),
                            state.maxWaves(), state.timer(), state.maxTimer(), state.waves(),
                            state.deployedDisplay() - 1, state.previousSquads(), state.lastWaveHp(), state.validBeds(),
                            state.bedCacheTimer(), state.failedSpawnAttempts(), 0, state.playerVictory(), true,
                            state.goodwillCleared());
                    data.setState(playerId, next);
                    updateBossEvent(bossEvent, next);
                }
                continue;
            }

            int validBeds = state.validBeds();
            int bedCacheTimer = state.bedCacheTimer();
            if (bedCacheTimer > 0) {
                bedCacheTimer--;
            } else {
                validBeds = RaidReinforcementSpawner.countValidBeds(level, center);
                bedCacheTimer = DefenderConfig.BED_CACHE_INTERVAL;
            }
            int deployedDisplay = Math.max(0, state.deployedDisplay() - 1);
            int timer = state.timer();
            if (deployedDisplay == 0 && timer > 0) {
                timer = Math.max(0, timer - getHostileTickDecrement(level));
            }
            if (deployedDisplay > 0 || timer > 0) {
                data.setState(playerId, new GoetyRaidsData.ActiveRaidState(state.center(), state.maxWaves(), timer,
                        state.maxTimer(), state.waves(), deployedDisplay, state.previousSquads(), state.lastWaveHp(),
                        validBeds, bedCacheTimer, state.failedSpawnAttempts(), 0, state.playerVictory(), false,
                        state.goodwillCleared()));
                continue;
            }

            Scoreboard scoreboard = level.getScoreboard();
            PlayerTeam team = scoreboard.getPlayerTeam(TEAM_NAME);
            if (team == null) {
                team = scoreboard.addPlayerTeam(TEAM_NAME);
                team.setColor(ChatFormatting.RED);
            }
            int[] targetIndex = {0};
            RaidReinforcementSpawner.WaveResult result = RaidReinforcementSpawner.spawnWave(level, center,
                    level.getRandom(), state.waves() + 1, state.previousSquads(), team, entity -> {
                        entity.setGoetyRaidOwner(playerId);
                        entity.setTarget(findAttackTarget(entity, targets, playerId, targetIndex[0]++));
                    });
            if (result.squadsSpawned() == 0) {
                int failures = state.failedSpawnAttempts() + 1;
                boolean complete = failures >= DefenderConfig.MAX_CONSECUTIVE_SPAWN_FAILURES;
                GoetyRaidsData.ActiveRaidState next = new GoetyRaidsData.ActiveRaidState(state.center(),
                        state.maxWaves(), DefenderConfig.SPAWN_FAILURE_RETRY_TICKS, state.maxTimer(), state.waves(),
                        complete ? DefenderConfig.DEPLOYED_DISPLAY_TICKS : 0, state.previousSquads(),
                        state.lastWaveHp(), validBeds, bedCacheTimer, failures, 0, true, complete,
                        state.goodwillCleared());
                data.setState(playerId, next);
                if (complete) player.sendSystemMessage(Component.translatable("raid.millager.goety.unavailable"));
                updateBossEvent(bossEvent, next);
                continue;
            }

            int waves = state.waves() + 1;
            boolean complete = waves >= state.maxWaves();
            int maxTimer = RaidReinforcementSpawner.computeMaxTimer(validBeds, result.totalHp());
            GoetyRaidsData.ActiveRaidState next = new GoetyRaidsData.ActiveRaidState(state.center(), state.maxWaves(),
                    complete ? 0 : maxTimer, maxTimer, waves, DefenderConfig.DEPLOYED_DISPLAY_TICKS,
                    result.squadsSpawned(), result.totalHp(), validBeds, bedCacheTimer, 0, 0, true, complete,
                    state.goodwillCleared());
            data.setState(playerId, next);
            updateBossEvent(bossEvent, next);
        }
    }

    private static void retargetReinforcements(ServerLevel level, BlockPos center, UUID playerId,
                                                List<LivingEntity> targets) {
        int targetIndex = 0;
        for (AbstractMillager entity : level.getEntitiesOfClass(AbstractMillager.class,
                new AABB(center).inflate(192.0D), candidate -> playerId.equals(candidate.getGoetyRaidOwner()))) {
            LivingEntity target = entity.getTarget();
            if (target != null && canAttack(entity, target, playerId)) continue;
            entity.setTarget(findAttackTarget(entity, targets, playerId, targetIndex++));
        }
    }

    private static void recruitNeutralMillagers(ServerLevel level, BlockPos center, UUID playerId,
                                                 List<LivingEntity> targets) {
        int targetIndex = 0;
        for (AbstractMillager entity : level.getEntitiesOfClass(AbstractMillager.class,
                new AABB(center).inflate(128.0D), candidate -> candidate.getGoetyRaidOwner() == null
                        && candidate.getRaidReinforcementCenter() == null
                        && (candidate.getTarget() == null || targets.contains(candidate.getTarget())))) {
            LivingEntity target = findAttackTarget(entity, targets, playerId, targetIndex++);
            if (target == null) continue;
            entity.setGoetyRaidOwner(playerId);
            entity.setTarget(target);
        }
    }

    private static @Nullable LivingEntity findAttackTarget(AbstractMillager entity, List<LivingEntity> targets,
                                                            UUID playerId, int startIndex) {
        for (int i = 0; i < targets.size(); i++) {
            LivingEntity target = targets.get(Math.floorMod(startIndex + i, targets.size()));
            if (canAttack(entity, target, playerId) && entity.getNavigation().createPath(target, 0) != null) return target;
        }
        return null;
    }

    private static boolean canAttack(AbstractMillager entity, LivingEntity target, UUID playerId) {
        return GoetyCompat.isRaidTarget(target, playerId) && !target.isRemoved() && entity.canAttack(target);
    }

    private static boolean isVillageConquered(ServerLevel level, BlockPos center) {
        return !level.isVillage(center) || level.getEntitiesOfClass(Villager.class,
                new AABB(center).inflate(256.0D), villager -> villager.isAlive() && !villager.isBaby()).isEmpty();
    }

    private static int getHostileTickDecrement(ServerLevel level) {
        return switch (Math.max(1, level.getDifficulty().getId())) {
            case 1 -> DefenderConfig.TICK_DECREMENT_HARD;
            case 3 -> DefenderConfig.TICK_DECREMENT_EASY;
            default -> DefenderConfig.TICK_DECREMENT_NORMAL;
        };
    }

    private static void finishRaid(ServerLevel level, GoetyRaidsData data, UUID playerId, BlockPos center) {
        data.removeState(playerId);
        releaseReinforcements(level, center, playerId);
        Map<UUID, ServerBossEvent> levelEvents = BOSS_EVENTS.get(level);
        if (levelEvents != null) {
            ServerBossEvent bossEvent = levelEvents.remove(playerId);
            if (bossEvent != null) {
                bossEvent.removeAllPlayers();
                bossEvent.setVisible(false);
            }
            if (levelEvents.isEmpty()) BOSS_EVENTS.remove(level);
        }
    }

    private static void releaseReinforcements(ServerLevel level, BlockPos center, UUID playerId) {
        for (AbstractMillager entity : level.getEntitiesOfClass(AbstractMillager.class,
                new AABB(center).inflate(192.0D), candidate -> playerId.equals(candidate.getGoetyRaidOwner()))) {
            entity.setGoetyRaidOwner(null);
            entity.setTarget(null);
        }
        for (BeeGolem entity : level.getEntitiesOfClass(BeeGolem.class,
                new AABB(center).inflate(192.0D), candidate -> playerId.equals(candidate.getGoetyRaidOwner()))) {
            entity.setGoetyRaidOwner(null);
            entity.setTarget(null);
        }
    }

    private static ServerBossEvent getBossEvent(ServerLevel level, UUID playerId) {
        return BOSS_EVENTS.computeIfAbsent(level, ignored -> new HashMap<>()).computeIfAbsent(playerId, ignored -> {
            ServerBossEvent event = new ServerBossEvent(Component.translatable("raid.millager.goety.reinforcements"),
                    BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);
            event.setProgress(0.0F);
            return event;
        });
    }

    private static void syncBossEvent(ServerBossEvent bossEvent, @Nullable ServerPlayer player) {
        for (ServerPlayer current : List.copyOf(bossEvent.getPlayers())) {
            if (current != player) bossEvent.removePlayer(current);
        }
        if (player != null && !bossEvent.getPlayers().contains(player)) bossEvent.addPlayer(player);
    }

    private static void updateBossEvent(ServerBossEvent bossEvent, GoetyRaidsData.ActiveRaidState state) {
        if (state.resultDisplay() > 0) {
            bossEvent.setProgress(state.playerVictory() ? 0.0F : 1.0F);
            bossEvent.setName(Component.translatable(state.playerVictory()
                    ? "raid.millager.goety.defeated" : "raid.millager.goety.defended"));
        } else if (state.complete() && state.failedSpawnAttempts() >= DefenderConfig.MAX_CONSECUTIVE_SPAWN_FAILURES) {
            bossEvent.setProgress(1.0F);
            bossEvent.setName(Component.translatable("raid.millager.goety.unavailable"));
        } else if (state.deployedDisplay() > 0) {
            bossEvent.setProgress(1.0F);
            bossEvent.setName(Component.translatable("raid.millager.goety.deployed.squads", state.previousSquads()));
        } else if (state.complete()) {
            bossEvent.setProgress(1.0F);
            bossEvent.setName(Component.translatable("raid.millager.goety.complete"));
        } else {
            float progress = state.maxTimer() > 0 ? 1.0F - (float) state.timer() / state.maxTimer() : 0.0F;
            bossEvent.setProgress(Math.max(0.0F, Math.min(1.0F, progress)));
            bossEvent.setName(Component.translatable("raid.millager.goety.reinforcements"));
        }
    }
}
