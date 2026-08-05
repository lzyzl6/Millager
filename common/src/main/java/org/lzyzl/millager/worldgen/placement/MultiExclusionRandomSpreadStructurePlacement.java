package org.lzyzl.millager.worldgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement.ExclusionZone;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import org.lzyzl.millager.worldgen.MillagerStructures;

import java.util.List;

public class MultiExclusionRandomSpreadStructurePlacement extends RandomSpreadStructurePlacement {

    public static final MapCodec<MultiExclusionRandomSpreadStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(0, 4096).fieldOf("spacing").forGetter(MultiExclusionRandomSpreadStructurePlacement::spacing),
            Codec.intRange(0, 4096).fieldOf("separation").forGetter(MultiExclusionRandomSpreadStructurePlacement::separation),
            RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR).forGetter(MultiExclusionRandomSpreadStructurePlacement::spreadType),
            Codec.INT.fieldOf("salt").forGetter(MultiExclusionRandomSpreadStructurePlacement::saltValue),
            ExclusionZone.CODEC.listOf().fieldOf("exclusion_zones").forGetter(placement -> placement.exclusionZones)
    ).apply(instance, MultiExclusionRandomSpreadStructurePlacement::new));

    private final int saltValue;
    private final List<ExclusionZone> exclusionZones;

    public MultiExclusionRandomSpreadStructurePlacement(int spacing, int separation, RandomSpreadType spreadType, int salt, List<ExclusionZone> exclusionZones) {
        super(spacing, separation, spreadType, salt);
        this.saltValue = salt;
        this.exclusionZones = exclusionZones;
    }

    @Override
    public boolean isStructureChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        if (!super.isStructureChunk(state, chunkX, chunkZ)) {
            return false;
        }
        for (ExclusionZone exclusionZone : exclusionZones) {
            if (state.hasStructureChunkInRange(exclusionZone.otherSet(), chunkX, chunkZ, exclusionZone.chunkCount())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public StructurePlacementType<?> type() {
        return MillagerStructures.MULTI_EXCLUSION_RANDOM_SPREAD.get();
    }

    private int saltValue() {
        return saltValue;
    }
}
