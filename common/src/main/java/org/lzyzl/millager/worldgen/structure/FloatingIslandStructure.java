package org.lzyzl.millager.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.worldgen.MillagerStructures;

import java.util.Optional;

public class FloatingIslandStructure extends Structure {

    public static final Codec<FloatingIslandStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(settingsCodec(instance)).apply(instance, FloatingIslandStructure::new));
    private static final int[][] WATER_CHECK_OFFSETS = {{0, 0}, {16, 0}, {-16, 0}, {0, 16}, {0, -16}, {16, 16}, {16, -16}, {-16, 16}, {-16, -16}};

    public FloatingIslandStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public @NonNull Optional<GenerationStub> findGenerationPoint(@NonNull GenerationContext ctx) {
        int x = ctx.chunkPos().getMiddleBlockX();
        int z = ctx.chunkPos().getMiddleBlockZ();
        for (int[] offset : WATER_CHECK_OFFSETS) {
            int surface = ctx.chunkGenerator().getBaseHeight(x + offset[0], z + offset[1], Heightmap.Types.WORLD_SURFACE_WG, ctx.heightAccessor(), ctx.randomState());
            int floor = ctx.chunkGenerator().getBaseHeight(x + offset[0], z + offset[1], Heightmap.Types.OCEAN_FLOOR_WG, ctx.heightAccessor(), ctx.randomState());
            if (surface <= floor) return Optional.empty();
        }
        return onTopOfChunkCenter(ctx, Heightmap.Types.WORLD_SURFACE_WG, builder ->
                FloatingIslandPiece.addPiece(ctx.structureTemplateManager(), new BlockPos(x, 39, z), Rotation.getRandom(ctx.random()), builder));
    }

    @Override
    public @NonNull StructureType<?> type() {
        return MillagerStructures.FLOATING_ISLAND.get();
    }
}
