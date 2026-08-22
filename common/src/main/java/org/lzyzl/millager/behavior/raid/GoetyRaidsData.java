package org.lzyzl.millager.behavior.raid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GoetyRaidsData extends SavedData {

    private static final Codec<GoetyRaidsData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, ActiveRaidState.CODEC)
                            .optionalFieldOf("raids", Map.of())
                            .forGetter(data -> data.raidStates)
            ).apply(instance, GoetyRaidsData::new)
    );
    private static final String FILE_ID = "millager_goety_raids";
    private final Map<String, ActiveRaidState> raidStates;

    public GoetyRaidsData() {
        this.raidStates = new HashMap<>();
    }

    private GoetyRaidsData(Map<String, ActiveRaidState> raidStates) {
        this.raidStates = new HashMap<>(raidStates);
    }

    public static GoetyRaidsData getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                GoetyRaidsData::load,
                GoetyRaidsData::new,
                FILE_ID
        );
    }

    public static GoetyRaidsData load(CompoundTag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag).result().orElseGet(GoetyRaidsData::new);
    }

    @Override
    public @NonNull CompoundTag save(@NonNull CompoundTag tag) {
        return (CompoundTag) CODEC.encodeStart(NbtOps.INSTANCE, this).result().orElse(tag);
    }

    public void setState(UUID player, ActiveRaidState state) {
        raidStates.put(player.toString(), state);
        setDirty();
    }

    public Set<UUID> activePlayers() {
        Set<UUID> players = new HashSet<>();
        for (String player : raidStates.keySet()) {
            try {
                players.add(UUID.fromString(player));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return players;
    }

    public @Nullable ActiveRaidState getState(UUID player) {
        return raidStates.get(player.toString());
    }

    public void removeState(UUID player) {
        if (raidStates.remove(player.toString()) != null) setDirty();
    }

    public record ActiveRaidState(long center, int maxWaves, int timer, int maxTimer, int waves,
                                  int deployedDisplay, int previousSquads, int lastWaveHp, int validBeds,
                                  int bedCacheTimer, int failedSpawnAttempts, int resultDisplay, boolean playerVictory,
                                  boolean complete, boolean goodwillCleared) {
        private static final Codec<ActiveRaidState> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.LONG.fieldOf("c").forGetter(ActiveRaidState::center),
                        Codec.INT.optionalFieldOf("g", 3).forGetter(ActiveRaidState::maxWaves),
                        Codec.INT.optionalFieldOf("r", 0).forGetter(ActiveRaidState::timer),
                        Codec.INT.optionalFieldOf("m", 0).forGetter(ActiveRaidState::maxTimer),
                        Codec.INT.optionalFieldOf("w", 0).forGetter(ActiveRaidState::waves),
                        Codec.INT.optionalFieldOf("d", 0).forGetter(ActiveRaidState::deployedDisplay),
                        Codec.INT.optionalFieldOf("q", 0).forGetter(ActiveRaidState::previousSquads),
                        Codec.INT.optionalFieldOf("h", 0).forGetter(ActiveRaidState::lastWaveHp),
                        Codec.INT.optionalFieldOf("b", 0).forGetter(ActiveRaidState::validBeds),
                        Codec.INT.optionalFieldOf("bt", 0).forGetter(ActiveRaidState::bedCacheTimer),
                        Codec.INT.optionalFieldOf("f", 0).forGetter(ActiveRaidState::failedSpawnAttempts),
                        Codec.INT.optionalFieldOf("v", 0).forGetter(ActiveRaidState::resultDisplay),
                        Codec.BOOL.optionalFieldOf("pv", true).forGetter(ActiveRaidState::playerVictory),
                        Codec.BOOL.optionalFieldOf("x", false).forGetter(ActiveRaidState::complete),
                        Codec.BOOL.optionalFieldOf("gc", false).forGetter(ActiveRaidState::goodwillCleared)
                ).apply(instance, ActiveRaidState::new)
        );

        public ActiveRaidState withGoodwillCleared() {
            return new ActiveRaidState(center, maxWaves, timer, maxTimer, waves, deployedDisplay, previousSquads,
                    lastWaveHp, validBeds, bedCacheTimer, failedSpawnAttempts, resultDisplay, playerVictory, complete, true);
        }
    }
}
