package org.lzyzl.millager.worldgen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.util.ResourceLocationHelper;
import org.lzyzl.millager.worldgen.MillagerStructures;

import static org.lzyzl.millager.Millager.MOD_ID;

public class StrongRoomPiece extends TemplateStructurePiece {

    private static final ResourceLocation STRUCTURE = ResourceLocationHelper.create(MOD_ID, "strong_room");

    public StrongRoomPiece(StructureTemplateManager manager, CompoundTag tag) {
        super(MillagerStructures.STRONG_ROOM_PIECE.get(), tag, manager, id -> makeSettings(tag.contains("rot") ? Rotation.valueOf(tag.getString("rot")) : Rotation.NONE));
    }

    private static StructurePlaceSettings makeSettings(Rotation rotation) {
        return new StructurePlaceSettings().setMirror(Mirror.NONE).setRotation(rotation).setIgnoreEntities(false);
    }

    private StrongRoomPiece(StructureTemplateManager manager, BlockPos pos, Rotation rotation) {
        super(MillagerStructures.STRONG_ROOM_PIECE.get(), 0, manager, STRUCTURE, STRUCTURE.toString(), makeSettings(rotation), pos);
    }

    public static void addPiece(StructureTemplateManager manager, BlockPos pos, Rotation rotation, StructurePieceAccessor accessor) {
        accessor.addPiece(new StrongRoomPiece(manager, pos, rotation));
    }

    @Override
    protected void addAdditionalSaveData(@NonNull StructurePieceSerializationContext ctx, @NonNull CompoundTag tag) {
        super.addAdditionalSaveData(ctx, tag);
        tag.putString("rot", this.placeSettings.getRotation().name());
    }

    @Override
    protected void handleDataMarker(@NonNull String marker, @NonNull BlockPos pos, @NonNull ServerLevelAccessor level, @NonNull RandomSource random, @NonNull BoundingBox box) {
    }
}
