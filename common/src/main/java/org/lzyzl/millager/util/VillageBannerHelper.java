package org.lzyzl.millager.util;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class VillageBannerHelper {

    public static boolean isVillageBanner(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof BannerItem)) return false;
        CompoundTag blockEntityData = BlockItem.getBlockEntityData(itemStack);
        if (blockEntityData == null) return false;
        ListTag patterns = blockEntityData.getList("Patterns", Tag.TAG_COMPOUND);
        String villageVillager = ResourceVariantHelper.VILLAGE_VILLAGER_PATTERN.get().getHashname();
        return patterns.stream().anyMatch(pattern -> villageVillager.equals(((CompoundTag) pattern).getString("Pattern")));
    }

    public static ItemStack create() {
        ItemStack banner = new ItemStack(Items.GRAY_BANNER);
        ListTag patterns = new BannerPattern.Builder()
                .addPattern(BannerPatterns.BORDER, DyeColor.BROWN)
                .addPattern(ResourceVariantHelper.VILLAGE_VILLAGER, DyeColor.WHITE)
                .toListTag();
        CompoundTag blockEntityData = new CompoundTag();
        blockEntityData.put("Patterns", patterns);
        BlockItem.setBlockEntityData(banner, BlockEntityType.BANNER, blockEntityData);
        banner.hideTooltipPart(ItemStack.TooltipPart.ADDITIONAL);
        banner.setHoverName(Component.translatable("block.millager.village_banner").withStyle(ChatFormatting.GOLD));
        return banner;
    }

    public static ItemStack createShield() {
        ItemStack shield = new ItemStack(Items.SHIELD);
        ListTag patterns = new BannerPattern.Builder()
                .addPattern(BannerPatterns.BORDER, DyeColor.BROWN)
                .addPattern(ResourceVariantHelper.VILLAGE_VILLAGER, DyeColor.WHITE)
                .toListTag();
        CompoundTag blockEntityData = new CompoundTag();
        blockEntityData.putInt("Base", DyeColor.GRAY.getId());
        blockEntityData.put("Patterns", patterns);
        BlockItem.setBlockEntityData(shield, BlockEntityType.BANNER, blockEntityData);
        return shield;
    }
}
