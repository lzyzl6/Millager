package org.lzyzl.millager.worldgen.structure;
import net.minecraft.Util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.worldgen.MillagerStructures;

import static org.lzyzl.millager.Millager.MOD_ID;

public class CommandPostPiece extends TemplateStructurePiece {

    private static final TagKey<Biome> IS_DESERT =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", "is_desert"));

    private static final TagKey<Biome> FORGE_IS_DESERT =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("forge", "is_desert"));

    private static final TagKey<Biome> HAS_DESERT_PYRAMID =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("minecraft", "has_structure/desert_pyramid"));

    private static final ResourceLocation[] STRUCTURES_NORMAL = new ResourceLocation[]{
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "command_post/classic")
    };
    private static final ResourceLocation[] STRUCTURES_DESERT = new ResourceLocation[]{
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "command_post/sand")
    };
    private static final ResourceLocation[] STRUCTURES_RUINED = new ResourceLocation[]{
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "command_post/ruined_classic")
    };
    private static final ResourceLocation[] STRUCTURES_DESERT_RUINED = new ResourceLocation[]{
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "command_post/ruined_sand")
    };
    public final boolean ruined;
    public final int embedDepth;

    public CommandPostPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(MillagerStructures.COMMAND_POST_PIECE.get(), tag, ctx.structureTemplateManager(),
                id -> makeSettings(tag.contains("rot") ? Rotation.valueOf(tag.getString("rot")) : Rotation.NONE));
        this.ruined = tag.getBoolean("ruined");
        this.embedDepth = tag.contains("embed_depth") ? tag.getInt("embed_depth") : 1;
    }

    private static StructurePlaceSettings makeSettings(Rotation rotation) {
        return new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(rotation)
                .setIgnoreEntities(false);
    }

    public CommandPostPiece(StructureTemplateManager manager, ResourceLocation identifier, BlockPos pos, Rotation rotation, boolean bl, int i) {
        super(MillagerStructures.COMMAND_POST_PIECE.get(), 0, manager,
                identifier, identifier.toString(), makeSettings(rotation), pos);
        this.ruined = bl;
        this.embedDepth = i;
    }

    public static void addPiece(StructureTemplateManager manager, BlockPos pos, Rotation rotation,
                                StructurePieceAccessor accessor, RandomSource random, boolean bl, Holder<Biome> biome, int i) {
        ResourceLocation id;
        if (biome.is(HAS_DESERT_PYRAMID) || biome.is(IS_DESERT) || biome.is(FORGE_IS_DESERT))
            id = Util.getRandom(bl ? STRUCTURES_DESERT_RUINED : STRUCTURES_DESERT, random);
        else id = Util.getRandom(bl ? STRUCTURES_RUINED : STRUCTURES_NORMAL, random);
        CommandPostPiece piece = new CommandPostPiece(manager, id, pos, rotation, bl, i);
        accessor.addPiece(piece);
    }

    @Override
    protected void addAdditionalSaveData(@NonNull StructurePieceSerializationContext ctx, @NonNull CompoundTag tag) {
        super.addAdditionalSaveData(ctx, tag);
        tag.putBoolean("ruined", this.ruined);
        tag.putInt("embed_depth", this.embedDepth);
        tag.putString("rot", this.placeSettings.getRotation().name());
    }

    @Override
    public void postProcess(@NonNull WorldGenLevel level, @NonNull StructureManager structureManager,
                            @NonNull ChunkGenerator generator, @NonNull RandomSource random,
                            @NonNull BoundingBox box, @NonNull ChunkPos chunkPos, @NonNull BlockPos pivot) {
        int x0 = this.boundingBox.minX(), x1 = this.boundingBox.maxX();
        int z0 = this.boundingBox.minZ(), z1 = this.boundingBox.maxZ();
        int minY = Math.min(
                Math.min(level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x0, z0),
                        level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x1, z0)),
                Math.min(level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x0, z1),
                        level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x1, z1)));
        this.templatePosition = new BlockPos(this.templatePosition.getX(), minY - this.embedDepth, this.templatePosition.getZ());
        super.postProcess(level, structureManager, generator, random, box, chunkPos, pivot);
    }

    @Override
    protected void handleDataMarker(@NonNull String marker, @NonNull BlockPos pos,
                                    @NonNull ServerLevelAccessor level, @NonNull RandomSource random,
                                    @NonNull BoundingBox box) {

    }
}
