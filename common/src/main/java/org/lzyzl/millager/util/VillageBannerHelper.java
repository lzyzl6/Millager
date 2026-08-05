package org.lzyzl.millager.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;

public class VillageBannerHelper {

    public static ItemStack create(HolderGetter<BannerPattern> patterns) {
        BannerPatternLayers layers = new BannerPatternLayers.Builder()
                .addIfRegistered(patterns, BannerPatterns.BORDER, DyeColor.BROWN)
                .addIfRegistered(patterns, ResourceVariantHelper.VILLAGE_VILLAGER, DyeColor.WHITE)
                .build();
        ItemStack banner = new ItemStack(Items.GRAY_BANNER);
        banner.set(DataComponents.BANNER_PATTERNS, layers);
        banner.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        banner.set(DataComponents.ITEM_NAME,
                Component.translatable("block.millager.village_banner").withStyle(ChatFormatting.GOLD));
        return banner;
    }
}
