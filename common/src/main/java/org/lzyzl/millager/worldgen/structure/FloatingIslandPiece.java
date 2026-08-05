package org.lzyzl.millager.worldgen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.lzyzl.millager.worldgen.MillagerStructures;

import java.util.List;

import static org.lzyzl.millager.Millager.MOD_ID;

public class FloatingIslandPiece extends TemplateStructurePiece {

    private static final Identifier STRUCTURE = Identifier.fromNamespaceAndPath(MOD_ID, "island");

    public FloatingIslandPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(MillagerStructures.FLOATING_ISLAND_PIECE.get(), tag, ctx.structureTemplateManager(), id -> makeSettings(tag.read("rot", Rotation.CODEC).orElseThrow()));
    }

    private static StructurePlaceSettings makeSettings(Rotation rotation) {
        return new StructurePlaceSettings().setMirror(Mirror.NONE).setRotation(rotation).setIgnoreEntities(false);
    }

    private FloatingIslandPiece(StructureTemplateManager manager, BlockPos pos, Rotation rotation) {
        super(MillagerStructures.FLOATING_ISLAND_PIECE.get(), 0, manager, STRUCTURE, STRUCTURE.toString(), makeSettings(rotation), pos);
    }

    public static void addPiece(StructureTemplateManager manager, BlockPos pos, Rotation rotation, StructurePieceAccessor accessor) {
        accessor.addPiece(new FloatingIslandPiece(manager, pos, rotation));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super.addAdditionalSaveData(ctx, tag);
        tag.store("rot", Rotation.CODEC, this.placeSettings.getRotation());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        super.postProcess(level, structureManager, generator, random, box, chunkPos, pivot);
        int minX = Math.max(this.boundingBox.minX(), box.minX());
        int minY = Math.max(this.boundingBox.minY(), box.minY());
        int minZ = Math.max(this.boundingBox.minZ(), box.minZ());
        int maxX = Math.min(this.boundingBox.maxX(), box.maxX());
        int maxY = Math.min(this.boundingBox.maxY(), box.maxY());
        int maxZ = Math.min(this.boundingBox.maxZ(), box.maxZ());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if ((state.is(Blocks.LADDER) || y <= 62)
                            && state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
                        level.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, false), 2);
                    }
                }
            }
        }
        this.restoreCrops(level, box);
    }

    private void restoreCrops(WorldGenLevel level, BoundingBox box) {
        for (Block crop : List.of(Blocks.WHEAT, Blocks.BEETROOTS, Blocks.POTATOES)) {
            for (StructureTemplate.StructureBlockInfo info : this.template.filterBlocks(this.templatePosition, this.placeSettings, crop)) {
                if (box.isInside(info.pos())) {
                    level.setBlock(info.pos(), info.state(), 2);
                }
            }
        }
    }

    @Override
    protected void handleDataMarker(String marker, BlockPos pos, ServerLevelAccessor level, RandomSource random, BoundingBox box) {
    }
}
