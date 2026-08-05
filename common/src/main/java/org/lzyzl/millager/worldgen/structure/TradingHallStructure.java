package org.lzyzl.millager.worldgen.structure;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.worldgen.MillagerStructures;

import java.util.Optional;

public class TradingHallStructure extends Structure {

    public static final MapCodec<TradingHallStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(settingsCodec(instance)).apply(instance, TradingHallStructure::new));
    private static final int[] X_CHECK_OFFSETS = {-4, 0, 11, 22, 26};
    private static final int[] Z_CHECK_OFFSETS = {-4, 0, 10, 20, 30, 40, 44};
    private static final int MAX_SURFACE_VARIATION = 5;

    public TradingHallStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public @NonNull Optional<GenerationStub> findGenerationPoint(@NonNull GenerationContext ctx) {
        int x = ctx.chunkPos().getMiddleBlockX();
        int z = ctx.chunkPos().getMiddleBlockZ();
        Rotation rotation = Rotation.getRandom(ctx.random());
        int minSurface = Integer.MAX_VALUE;
        int maxSurface = Integer.MIN_VALUE;
        for (int localX : X_CHECK_OFFSETS) {
            for (int localZ : Z_CHECK_OFFSETS) {
                int offsetX = rotateX(localX, localZ, rotation);
                int offsetZ = rotateZ(localX, localZ, rotation);
                int surface = ctx.chunkGenerator().getBaseHeight(x + offsetX, z + offsetZ, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ctx.heightAccessor(), ctx.randomState());
                int floor = ctx.chunkGenerator().getBaseHeight(x + offsetX, z + offsetZ, Heightmap.Types.OCEAN_FLOOR_WG, ctx.heightAccessor(), ctx.randomState());
                if (surface > floor) {
                    return Optional.empty();
                }
                minSurface = Math.min(minSurface, floor);
                maxSurface = Math.max(maxSurface, floor);
            }
        }
        if (maxSurface - minSurface > MAX_SURFACE_VARIATION) {
            return Optional.empty();
        }
        int y = maxSurface;
        return onTopOfChunkCenter(ctx, Heightmap.Types.WORLD_SURFACE_WG, builder ->
                TradingHallPiece.addPiece(ctx.structureTemplateManager(), new BlockPos(x, y, z), rotation, builder));
    }

    private static int rotateX(int x, int z, Rotation rotation) {
        return switch (rotation) {
            case NONE -> x;
            case CLOCKWISE_90 -> -z;
            case CLOCKWISE_180 -> -x;
            case COUNTERCLOCKWISE_90 -> z;
        };
    }

    private static int rotateZ(int x, int z, Rotation rotation) {
        return switch (rotation) {
            case NONE -> z;
            case CLOCKWISE_90 -> x;
            case CLOCKWISE_180 -> -z;
            case COUNTERCLOCKWISE_90 -> -x;
        };
    }

    @Override
    public @NonNull StructureType<?> type() {
        return MillagerStructures.TRADING_HALL.get();
    }
}
