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

public class StrongRoomStructure extends Structure {

    public static final Codec<StrongRoomStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(settingsCodec(instance)).apply(instance, StrongRoomStructure::new));
    private static final int Y = -48;

    public StrongRoomStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public @NonNull Optional<GenerationStub> findGenerationPoint(@NonNull GenerationContext ctx) {
        int x = ctx.chunkPos().getMiddleBlockX();
        int z = ctx.chunkPos().getMiddleBlockZ();
        return onTopOfChunkCenter(ctx, Heightmap.Types.WORLD_SURFACE_WG, builder ->
                StrongRoomPiece.addPiece(ctx.structureTemplateManager(), new BlockPos(x, Y, z), Rotation.getRandom(ctx.random()), builder));
    }

    @Override
    public @NonNull StructureType<?> type() {
        return MillagerStructures.STRONG_ROOM.get();
    }
}
