package org.lzyzl.millager.behavior.raid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;

public class MillagerRaidsData extends SavedData {

    public static final Codec<MillagerRaidsData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, DefenseState.CODEC)
                            .optionalFieldOf("raids", Map.of())
                            .forGetter(d -> d.raidStates)
            ).apply(instance, MillagerRaidsData::new)
    );
    private static final String FILE_ID = "millager_raids";
    public static final SavedDataType<MillagerRaidsData> TYPE =
            new SavedDataType<>(FILE_ID, MillagerRaidsData::new, CODEC, DataFixTypes.SAVED_DATA_RAIDS);
    private final Map<String, DefenseState> raidStates;

    public MillagerRaidsData() {
        this.raidStates = new HashMap<>();
    }

    private MillagerRaidsData(Map<String, DefenseState> raidStates) {
        this.raidStates = new HashMap<>(raidStates);
    }

    public static MillagerRaidsData getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void setState(int raidId, DefenseState state) {
        raidStates.put("raid_" + raidId, state);
        setDirty();
    }

    public DefenseState getStateOrNull(int raidId, long legacyCenterKey) {
        String raidKey = "raid_" + raidId;
        DefenseState state = raidStates.get(raidKey);
        if (state != null) return state;
        state = raidStates.remove(String.valueOf(legacyCenterKey));
        if (state != null) {
            raidStates.put(raidKey, state);
            setDirty();
        }
        return state;
    }

    public void removeState(int raidId, long legacyCenterKey) {
        boolean removed = raidStates.remove("raid_" + raidId) != null;
        removed |= raidStates.remove(String.valueOf(legacyCenterKey)) != null;
        if (removed) setDirty();
    }

    public record DefenseState(int groupsSpawned, int timer, int maxTimer, int spawns, int deployedDisplay,
                               int previousSquads, long enemyWaveStart, int enemyWaveCount, double weightedEnemyCount,
                               int trackedEnemyWave, int surgesUsed, boolean surgePending, int failedSpawnAttempts) {
        static final Codec<DefenseState> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("g").forGetter(DefenseState::groupsSpawned),
                Codec.INT.fieldOf("t").forGetter(DefenseState::timer),
                Codec.INT.fieldOf("m").forGetter(DefenseState::maxTimer),
                Codec.INT.fieldOf("s").forGetter(DefenseState::spawns),
                Codec.INT.fieldOf("d").forGetter(DefenseState::deployedDisplay),
                Codec.INT.optionalFieldOf("q", 0).forGetter(DefenseState::previousSquads),
                Codec.LONG.optionalFieldOf("ws", 0L).forGetter(DefenseState::enemyWaveStart),
                Codec.INT.optionalFieldOf("wc", 0).forGetter(DefenseState::enemyWaveCount),
                Codec.DOUBLE.optionalFieldOf("we", 0.0D).forGetter(DefenseState::weightedEnemyCount),
                Codec.INT.optionalFieldOf("ww", 0).forGetter(DefenseState::trackedEnemyWave),
                Codec.INT.optionalFieldOf("u", 0).forGetter(DefenseState::surgesUsed),
                Codec.BOOL.optionalFieldOf("p", false).forGetter(DefenseState::surgePending),
                Codec.INT.optionalFieldOf("f", 0).forGetter(DefenseState::failedSpawnAttempts)
        ).apply(i, DefenseState::new));
    }
}
