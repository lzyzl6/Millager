package org.lzyzl.millager.util;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.TooltipDisplay;
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
        banner.set(DataComponents.TOOLTIP_DISPLAY,
                TooltipDisplay.DEFAULT.withHidden(DataComponents.BANNER_PATTERNS, true));
        banner.set(DataComponents.ITEM_NAME, Component.translatable("block.millager.village_banner"));
        banner.set(DataComponents.RARITY, Rarity.UNCOMMON);
        return banner;
    }
}
