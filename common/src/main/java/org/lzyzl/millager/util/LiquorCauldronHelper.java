package org.lzyzl.millager.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Brightness;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.lzyzl.millager.MillagerBlocks;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.advancement.MillagerCriteria;
import org.lzyzl.millager.block.LiquorCauldronBlock;

import java.util.List;

import static net.minecraft.world.level.block.Block.UPDATE_CLIENTS;

public class LiquorCauldronHelper {

    private static final List<Item> liquorIngredients = List.of(Items.WHEAT_SEEDS,Items.WHEAT,Items.POTATO,Items.BEETROOT);

    public static void registerLiquorInteraction() {

        liquorIngredients.forEach(item -> CauldronInteractions.WATER.put(item, LiquorCauldronHelper::startBrewing));

        CauldronInteractions.EMPTY.put(Items.GLASS_BOTTLE, (state, world, pos, player, hand, stack) -> {
            if (state.is(MillagerBlocks.LIQUOR_CAULDRON.get())) {

                if (!world.isClientSide()) {
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(MillagerItems.liquor.get())));
                    int currentLevel = state.getValue(LiquorCauldronBlock.LEVEL);
                    if (currentLevel > 1) {
                        world.setBlock(pos, state.setValue(LiquorCauldronBlock.LEVEL, currentLevel - 1), 3);
                    } else {
                        world.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
                    }
                    world.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                    if (player instanceof ServerPlayer serverPlayer) {
                        MillagerCriteria.OBTAIN_LIQUOR.get().trigger(serverPlayer);
                    }
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
    }

    private static InteractionResult startBrewing(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
        if (!state.hasProperty(LayeredCauldronBlock.LEVEL) || state.getValue(LayeredCauldronBlock.LEVEL) < 3) return InteractionResult.PASS;
        player.swing(hand);
        if (!world.isClientSide()) {

            Item ingredientItem = stack.getItem();
            world.setBlock(pos, MillagerBlocks.BREWING_CAULDRON.get().defaultBlockState(), Block.UPDATE_NEIGHBORS | UPDATE_CLIENTS);
            Display.ItemDisplay display = EntityTypes.ITEM_DISPLAY.create(world, EntitySpawnReason.TRIGGERED);
            if (display != null) {
                display.setItemStack(new ItemStack(stack.getItem()));
                stack.consume(1, player);
                display.setPos(pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5);
                display.addTag("liquor_visual");
                display.setBrightnessOverride(new Brightness(15, 15));
                display.setShadowRadius(0.0f);
                world.addFreshEntity(display);
            }
            world.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (player instanceof ServerPlayer serverPlayer) {
                MillagerCriteria.BREW_LIQUOR.get().trigger(serverPlayer, new ItemStack(ingredientItem));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

}
