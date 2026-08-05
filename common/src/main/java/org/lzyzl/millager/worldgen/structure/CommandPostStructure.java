package org.lzyzl.millager.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.worldgen.MillagerStructures;

import java.util.Optional;

public class CommandPostStructure extends Structure {

    public static final Codec<CommandPostStructure> CODEC = RecordCodecBuilder.create((instance) -> instance.group(settingsCodec(instance), Codec.BOOL.fieldOf("ruined").forGetter((commandPostStructure) -> commandPostStructure.ruined)).apply(instance, CommandPostStructure::new));
    private static final int[][] SURFACE_CHECK_OFFSETS = {
            {-16, -16}, {-8, -16}, {0, -16}, {8, -16}, {16, -16},
            {-16, -8}, {-8, -8}, {0, -8}, {8, -8}, {16, -8},
            {-16, 0}, {-8, 0}, {0, 0}, {8, 0}, {16, 0},
            {-16, 8}, {-8, 8}, {0, 8}, {8, 8}, {16, 8},
            {-16, 16}, {-8, 16}, {0, 16}, {8, 16}, {16, 16}
    };
    private static final int MAX_SURFACE_VARIATION = 3;
    public final boolean ruined;

    public CommandPostStructure(StructureSettings settings, boolean bl) {
        super(settings);
        this.ruined = bl;
    }

    @Override
    public @NonNull Optional<GenerationStub> findGenerationPoint(@NonNull GenerationContext ctx) {
        int x = ctx.chunkPos().getMiddleBlockX();
        int z = ctx.chunkPos().getMiddleBlockZ();

        int minSurface = Integer.MAX_VALUE;
        int maxSurface = Integer.MIN_VALUE;
        for (int[] offset : SURFACE_CHECK_OFFSETS) {
            int cx = x + offset[0], cz = z + offset[1];
            int surface = ctx.chunkGenerator().getBaseHeight(
                    cx, cz, Heightmap.Types.WORLD_SURFACE_WG, ctx.heightAccessor(), ctx.randomState());
            int floor = ctx.chunkGenerator().getBaseHeight(
                    cx, cz, Heightmap.Types.OCEAN_FLOOR_WG, ctx.heightAccessor(), ctx.randomState());
            if (surface > floor) {
                return Optional.empty();
            }
            minSurface = Math.min(minSurface, surface);
            maxSurface = Math.max(maxSurface, surface);
        }
        if (maxSurface - minSurface > MAX_SURFACE_VARIATION) {
            return Optional.empty();
        }

        return onTopOfChunkCenter(ctx, Heightmap.Types.WORLD_SURFACE_WG, builder -> {
            int embedDepth = ctx.random().nextIntBetweenInclusive(2, this.ruined ? 4 : 3);
            int y = ctx.chunkGenerator().getBaseHeight(
                    x, z, Heightmap.Types.WORLD_SURFACE_WG,
                    ctx.heightAccessor(), ctx.randomState())
                    - embedDepth;
            Rotation rotation = Rotation.getRandom(ctx.random());
            Holder<Biome> biome = ctx.biomeSource().getNoiseBiome(
                    x >> 2, y >> 2, z >> 2, ctx.randomState().sampler());
            CommandPostPiece.addPiece(ctx.structureTemplateManager(),
                    new BlockPos(x, y, z), rotation, builder, ctx.random(), this.ruined, biome, embedDepth);
        });
    }

    @Override
    public @NonNull StructureType<?> type() {
        return MillagerStructures.COMMAND_POST.get();
    }
}
