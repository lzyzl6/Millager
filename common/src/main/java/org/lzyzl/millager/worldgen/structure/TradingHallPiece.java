package org.lzyzl.millager.worldgen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.lzyzl.millager.util.HangingEntityPositionFix;
import org.lzyzl.millager.worldgen.MillagerStructures;

import static org.lzyzl.millager.Millager.MOD_ID;

public class TradingHallPiece extends TemplateStructurePiece {

    private static final ResourceLocation STRUCTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "trading_hall");

    public TradingHallPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(MillagerStructures.TRADING_HALL_PIECE.get(), tag, ctx.structureTemplateManager(), id -> makeSettings(tag.contains("rot") ? Rotation.valueOf(tag.getString("rot")) : Rotation.NONE));
    }

    private static StructurePlaceSettings makeSettings(Rotation rotation) {
        return new StructurePlaceSettings().setMirror(Mirror.NONE).setRotation(rotation).setIgnoreEntities(false);
    }

    private TradingHallPiece(StructureTemplateManager manager, BlockPos pos, Rotation rotation) {
        super(MillagerStructures.TRADING_HALL_PIECE.get(), 0, manager, STRUCTURE, STRUCTURE.toString(), makeSettings(rotation), pos);
    }

    public static void addPiece(StructureTemplateManager manager, BlockPos pos, Rotation rotation, StructurePieceAccessor accessor) {
        accessor.addPiece(new TradingHallPiece(manager, pos, rotation));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super.addAdditionalSaveData(ctx, tag);
        tag.putString("rot", this.placeSettings.getRotation().name());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        HangingEntityPositionFix.begin();
        try {
            super.postProcess(level, structureManager, generator, random, box, chunkPos, pivot);
        } finally {
            HangingEntityPositionFix.end();
        }
        for (StructureTemplate.StructureBlockInfo info : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.WHEAT)) {
            if (box.isInside(info.pos())) {
                level.setBlock(info.pos(), info.state(), 2);
            }
        }
        for (StructureTemplate.StructureBlockInfo info : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.WALL_TORCH)) {
            if (box.isInside(info.pos())) {
                level.setBlock(info.pos(), info.state(), 2);
            }
        }
    }

    @Override
    protected void handleDataMarker(String marker, BlockPos pos, ServerLevelAccessor level, RandomSource random, BoundingBox box) {
    }
}
