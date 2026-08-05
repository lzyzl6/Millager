package org.lzyzl.millager.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import org.lzyzl.millager.registry.DeferredHolder;
import org.lzyzl.millager.registry.DeferredRegister;
import org.lzyzl.millager.util.ResourceLocationHelper;
import org.lzyzl.millager.worldgen.placement.MultiExclusionRandomSpreadStructurePlacement;
import org.lzyzl.millager.worldgen.structure.CommandPostPiece;
import org.lzyzl.millager.worldgen.structure.CommandPostStructure;
import org.lzyzl.millager.worldgen.structure.FloatingIslandPiece;
import org.lzyzl.millager.worldgen.structure.FloatingIslandStructure;
import org.lzyzl.millager.worldgen.structure.StrongRoomPiece;
import org.lzyzl.millager.worldgen.structure.StrongRoomStructure;
import org.lzyzl.millager.worldgen.structure.TradingHallPiece;
import org.lzyzl.millager.worldgen.structure.TradingHallStructure;

import static org.lzyzl.millager.Millager.MOD_ID;

public class MillagerStructures {

    public static final ResourceKey<Structure> COMMAND_POST_KEY = ResourceKey.create(
            Registries.STRUCTURE, ResourceLocationHelper.create(MOD_ID, "command_post"));

    public static final ResourceKey<Structure> RUINED_COMMAND_POST_KEY = ResourceKey.create(
            Registries.STRUCTURE, ResourceLocationHelper.create(MOD_ID, "ruined_command_post"));

    public static final ResourceKey<Structure> FLOATING_ISLAND_KEY = ResourceKey.create(
            Registries.STRUCTURE, ResourceLocationHelper.create(MOD_ID, "floating_island"));

    public static final ResourceKey<Structure> STRONG_ROOM_KEY = ResourceKey.create(
            Registries.STRUCTURE, ResourceLocationHelper.create(MOD_ID, "strong_room"));

    public static final ResourceKey<Structure> TRADING_HALL_KEY = ResourceKey.create(
            Registries.STRUCTURE, ResourceLocationHelper.create(MOD_ID, "trading_hall"));

    private static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, MOD_ID);

    private static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, MOD_ID);

    private static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENT_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, MOD_ID);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> COMMAND_POST_PIECE =
            STRUCTURE_PIECE_TYPES.register("command_post", () -> (StructurePieceType.StructureTemplateType) CommandPostPiece::new);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> FLOATING_ISLAND_PIECE =
            STRUCTURE_PIECE_TYPES.register("floating_island", () -> (StructurePieceType.StructureTemplateType) FloatingIslandPiece::new);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> STRONG_ROOM_PIECE =
            STRUCTURE_PIECE_TYPES.register("strong_room", () -> (StructurePieceType.StructureTemplateType) StrongRoomPiece::new);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> TRADING_HALL_PIECE =
            STRUCTURE_PIECE_TYPES.register("trading_hall", () -> (StructurePieceType.StructureTemplateType) TradingHallPiece::new);

    public static final DeferredHolder<StructureType<?>, StructureType<CommandPostStructure>> COMMAND_POST =
            STRUCTURE_TYPES.register("command_post", () -> () -> CommandPostStructure.CODEC);

    public static final DeferredHolder<StructureType<?>, StructureType<FloatingIslandStructure>> FLOATING_ISLAND =
            STRUCTURE_TYPES.register("floating_island", () -> () -> FloatingIslandStructure.CODEC);

    public static final DeferredHolder<StructureType<?>, StructureType<StrongRoomStructure>> STRONG_ROOM =
            STRUCTURE_TYPES.register("strong_room", () -> () -> StrongRoomStructure.CODEC);

    public static final DeferredHolder<StructureType<?>, StructureType<TradingHallStructure>> TRADING_HALL =
            STRUCTURE_TYPES.register("trading_hall", () -> () -> TradingHallStructure.CODEC);

    public static final DeferredHolder<StructurePlacementType<?>, StructurePlacementType<MultiExclusionRandomSpreadStructurePlacement>> MULTI_EXCLUSION_RANDOM_SPREAD =
            STRUCTURE_PLACEMENT_TYPES.register("multi_exclusion_random_spread", () -> () -> MultiExclusionRandomSpreadStructurePlacement.CODEC);

    public static void initialize() {
        STRUCTURE_PIECE_TYPES.register();
        STRUCTURE_TYPES.register();
        STRUCTURE_PLACEMENT_TYPES.register();
    }
}
