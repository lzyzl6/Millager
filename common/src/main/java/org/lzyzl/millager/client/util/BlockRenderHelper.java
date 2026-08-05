package org.lzyzl.millager.client.util;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.lzyzl.millager.MillagerBlocks;
import org.lzyzl.millager.block.entity.BrewingCauldronBlockEntity;
import org.lzyzl.millager.client.ClientRegistrationContext;

public class BlockRenderHelper {

    public static void registerBlockColors(ClientRegistrationContext ctx) {
        ctx.registerBlockColor((state, world, pos, tintIndex) -> 5578058, MillagerBlocks.LIQUOR_CAULDRON.get());

        ctx.registerBlockColor((state, world, pos, tintIndex) -> {
            if (tintIndex == 0 && world != null && pos != null) {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof BrewingCauldronBlockEntity brewingBe) {
                    float progress = brewingBe.getBrewingProgress();
                    return BrewingCauldronBlockEntity.getInterpolatedColor(progress);
                }
            }
            return 4159204;
        }, MillagerBlocks.BREWING_CAULDRON.get());

        ctx.registerBlockColor((state, world, pos, tintIndex) -> 14589720, MillagerBlocks.TIMED_FIRE.get());
    }
}
