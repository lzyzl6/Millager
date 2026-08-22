package org.lzyzl.millager;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.lzyzl.millager.registry.DeferredBlock;
import org.lzyzl.millager.registry.DeferredHolder;
import org.lzyzl.millager.registry.DeferredRegister;
import org.lzyzl.millager.block.BrewingCauldronBlock;
import org.lzyzl.millager.block.FakeIronBlock;
import org.lzyzl.millager.block.FakePumpkinBlock;
import org.lzyzl.millager.block.IllagerHeadBlock;
import org.lzyzl.millager.block.IllagerWallHeadBlock;
import org.lzyzl.millager.block.LiquorCauldronBlock;
import org.lzyzl.millager.block.PottedRoseBlock;
import org.lzyzl.millager.block.RoseBlock;
import org.lzyzl.millager.block.SandTableBlock;
import org.lzyzl.millager.block.TimedFireBlock;
import org.lzyzl.millager.block.TotemInfuserBlock;
import org.lzyzl.millager.block.VillagerHeadBlock;
import org.lzyzl.millager.block.VillagerWallHeadBlock;
import org.lzyzl.millager.block.entity.BrewingCauldronBlockEntity;
import org.lzyzl.millager.block.entity.HeadBlockEntity;
import org.lzyzl.millager.block.entity.TimedFireBlockEntity;
import org.lzyzl.millager.block.entity.TotemInfuserBlockEntity;

import static org.lzyzl.millager.Millager.MOD_ID;

public class MillagerBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.BlockEntityTypes BLOCK_ENTITY_TYPES =
            DeferredRegister.createBlockEntityTypes(MOD_ID);

    public static final DeferredBlock<FakeIronBlock> FAKE_IRON_BLOCK = BLOCKS.registerBlock(
            "fake_iron_block", FakeIronBlock::new, () -> BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK));

    public static final DeferredBlock<FakePumpkinBlock> FAKE_CARVED_PUMPKIN = BLOCKS.registerBlock(
            "fake_carved_pumpkin", FakePumpkinBlock::new, () -> BlockBehaviour.Properties.copy(Blocks.CARVED_PUMPKIN));

    public static final DeferredBlock<LiquorCauldronBlock> LIQUOR_CAULDRON = BLOCKS.registerBlock(
            "liquor_cauldron", LiquorCauldronBlock::new, () -> BlockBehaviour.Properties.copy(Blocks.CAULDRON));

    public static final DeferredBlock<BrewingCauldronBlock> BREWING_CAULDRON = BLOCKS.registerBlock(
            "brewing_cauldron", BrewingCauldronBlock::new, () -> BlockBehaviour.Properties.copy(Blocks.CAULDRON));

    public static final DeferredBlock<TotemInfuserBlock> TOTEM_INFUSER = BLOCKS.registerBlock(
            "totem_infuser", TotemInfuserBlock::new,
            () -> BlockBehaviour.Properties.copy(Blocks.AMETHYST_BLOCK).lightLevel(state -> 12));

    public static final DeferredBlock<SandTableBlock> SAND_TABLE = BLOCKS.registerBlock(
            "sand_table", SandTableBlock::new,
            () -> BlockBehaviour.Properties.copy(Blocks.DARK_OAK_WOOD).noOcclusion());

    public static final DeferredBlock<TimedFireBlock> TIMED_FIRE = BLOCKS.registerBlock(
            "timed_fire", TimedFireBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE).replaceable().noCollission().instabreak()
                    .lightLevel(s -> 15).sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<RoseBlock> ROSE = BLOCKS.registerBlock(
            "rose", props -> new RoseBlock(MobEffects.DAMAGE_RESISTANCE, 140, props),
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak()
                    .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<PottedRoseBlock> POTTED_ROSE = BLOCKS.registerBlock(
            "potted_rose", props -> new PottedRoseBlock(ROSE.get(), props),
            () -> BlockBehaviour.Properties.copy(Blocks.FLOWER_POT).randomTicks());

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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeadBlockEntity>> HEAD_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.registerBlockEntity("head_block_entity", HeadBlockEntity::new,
                    VILLAGER_HEAD, ILLAGER_HEAD, VILLAGER_WALL_HEAD, ILLAGER_WALL_HEAD);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TotemInfuserBlockEntity>> TOTEM_INFUSER_ENTITY =
            BLOCK_ENTITY_TYPES.registerBlockEntity("totem_infuser_entity", TotemInfuserBlockEntity::new, TOTEM_INFUSER);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BrewingCauldronBlockEntity>> BREWING_CAULDRON_ENTITY =
            BLOCK_ENTITY_TYPES.registerBlockEntity("brewing_cauldron_entity", BrewingCauldronBlockEntity::new, BREWING_CAULDRON);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TimedFireBlockEntity>> TIMED_FIRE_ENTITY =
            BLOCK_ENTITY_TYPES.registerBlockEntity("timed_fire_entity", TimedFireBlockEntity::new, TIMED_FIRE);
    private static BlockBehaviour.Properties wallVariant(Block block) {
        return BlockBehaviour.Properties.of().dropsLike(block);
    }

    public static void initialize() {
        BLOCKS.register();
        BLOCK_ENTITY_TYPES.register();
    }
}
