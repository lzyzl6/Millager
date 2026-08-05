package org.lzyzl.millager.util;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.level.block.entity.BannerPattern;

import static org.lzyzl.millager.Millager.MOD_ID;

public class ResourceVariantHelper {

    public static final ResourceKey<PaintingVariant> COCKTAIL_TUTORIAL = createVariantKey("how_to_make_a_molotov_cocktail");

    public static final ResourceKey<BannerPattern> VILLAGER = createPatternKey("villager");
    public static final ResourceKey<BannerPattern> VILLAGE_VILLAGER = createPatternKey("village_villager");

    public static final TagKey<BannerPattern> PATTERN_ITEM_VILLAGER = TagKey.create(
            Registries.BANNER_PATTERN, Identifier.fromNamespaceAndPath(MOD_ID, "pattern_item/villager"));

    public static ResourceKey<BannerPattern> createPatternKey(String string) {
        return ResourceKey.create(Registries.BANNER_PATTERN, Identifier.fromNamespaceAndPath(MOD_ID, string));
    }

    public static ResourceKey<PaintingVariant> createVariantKey(String string) {
        return ResourceKey.create(Registries.PAINTING_VARIANT, Identifier.fromNamespaceAndPath(MOD_ID, string));
    }
}
