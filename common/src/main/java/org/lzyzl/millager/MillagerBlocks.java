package org.lzyzl.millager;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.lzyzl.millager.registry.DeferredBlock;
import org.lzyzl.millager.registry.DeferredRegister;
import org.lzyzl.millager.block.*;
import org.lzyzl.millager.block.entity.BrewingCauldronBlockEntity;
import org.lzyzl.millager.block.entity.HeadBlockEntity;
import org.lzyzl.millager.block.entity.TimedFireBlockEntity;
import org.lzyzl.millager.block.entity.TotemInfuserBlockEntity;

import java.util.Set;
import java.util.function.Supplier;

import static org.lzyzl.millager.Millager.MOD_ID;

public class MillagerBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MOD_ID);

    public static final DeferredBlock<FakeIronBlock> FAKE_IRON_BLOCK = BLOCKS.registerBlock(
            "fake_iron_block", FakeIronBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));

    public static final DeferredBlock<FakePumpkinBlock> FAKE_CARVED_PUMPKIN = BLOCKS.registerBlock(
            "fake_carved_pumpkin", FakePumpkinBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CARVED_PUMPKIN));

    public static final DeferredBlock<LiquorCauldronBlock> LIQUOR_CAULDRON = BLOCKS.registerBlock(
            "liquor_cauldron", LiquorCauldronBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON));

    public static final DeferredBlock<BrewingCauldronBlock> BREWING_CAULDRON = BLOCKS.registerBlock(
            "brewing_cauldron", BrewingCauldronBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON));

    public static final DeferredBlock<TotemInfuserBlock> TOTEM_INFUSER = BLOCKS.registerBlock(
            "totem_infuser", TotemInfuserBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK).lightLevel(state -> 12));

    public static final DeferredBlock<SandTableBlock> SAND_TABLE = BLOCKS.registerBlock(
            "sand_table", SandTableBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_WOOD).noOcclusion());

    public static final DeferredBlock<TimedFireBlock> TIMED_FIRE = BLOCKS.registerBlock(
            "timed_fire", TimedFireBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.FIRE).replaceable().noCollision().instabreak()
                    .lightLevel(s -> 15).sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<FlowerBlock> ROSE = BLOCKS.registerBlock(
            "rose", props -> new FlowerBlock(MobEffects.RESISTANCE, 140, props),
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().instabreak()
                    .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<PottedRoseBlock> POTTED_ROSE = BLOCKS.registerBlock(
            "potted_rose", props -> new PottedRoseBlock(ROSE.get(), props),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT));

    public static final DeferredBlock<IllagerHeadBlock> ILLAGER_HEAD = BLOCKS.registerBlock(
            "illager_head", IllagerHeadBlock::new,
            () -> BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.valueOf("ILLAGER"))
                    .strength(1.0F).pushReaction(PushReaction.DESTROY).noOcclusion());

    public static final DeferredBlock<IllagerWallHeadBlock> ILLAGER_WALL_HEAD = BLOCKS.registerBlock(
            "illager_wall_head",
            IllagerWallHeadBlock::new,
            () -> wallVariant(ILLAGER_HEAD.get()).strength(1.0F).pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<VillagerHeadBlock> VILLAGER_HEAD = BLOCKS.registerBlock(
            "villager_head", VillagerHeadBlock::new,
            () -> BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.valueOf("VILLAGER"))
                    .strength(1.0F).pushReaction(PushReaction.DESTROY).noOcclusion());

    public static final DeferredBlock<VillagerWallHeadBlock> VILLAGER_WALL_HEAD = BLOCKS.registerBlock(
            "villager_wall_head",
            VillagerWallHeadBlock::new,
            () -> wallVariant(VILLAGER_HEAD.get()).strength(1.0F).pushReaction(PushReaction.DESTROY));

    public static final Supplier<BlockEntityType<HeadBlockEntity>> HEAD_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("head_block_entity",
                    () -> new BlockEntityType<>(HeadBlockEntity::new,
                            Set.of(VILLAGER_HEAD.get(), ILLAGER_HEAD.get(),
                                    VILLAGER_WALL_HEAD.get(), ILLAGER_WALL_HEAD.get())));

    public static final Supplier<BlockEntityType<TotemInfuserBlockEntity>> TOTEM_INFUSER_ENTITY =
            BLOCK_ENTITY_TYPES.register("totem_infuser_entity",
                    () -> new BlockEntityType<>(TotemInfuserBlockEntity::new, Set.of(TOTEM_INFUSER.get())));

    public static final Supplier<BlockEntityType<BrewingCauldronBlockEntity>> BREWING_CAULDRON_ENTITY =
            BLOCK_ENTITY_TYPES.register("brewing_cauldron_entity",
                    () -> new BlockEntityType<>(BrewingCauldronBlockEntity::new, Set.of(BREWING_CAULDRON.get())));

    public static final Supplier<BlockEntityType<TimedFireBlockEntity>> TIMED_FIRE_ENTITY =
            BLOCK_ENTITY_TYPES.register("timed_fire_entity",
                    () -> new BlockEntityType<>(TimedFireBlockEntity::new, Set.of(TIMED_FIRE.get())));

    private static BlockBehaviour.Properties wallVariant(Block block) {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.of().overrideLootTable(block.getLootTable());
        return props.overrideDescription(block.getDescriptionId());
    }

    public static void initialize() {
        BLOCKS.register();
        BLOCK_ENTITY_TYPES.register();
    }
}
