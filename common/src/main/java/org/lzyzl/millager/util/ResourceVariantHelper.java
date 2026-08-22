package org.lzyzl.millager.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.lzyzl.millager.registry.DeferredHolder;
import org.lzyzl.millager.registry.DeferredRegister;

import static org.lzyzl.millager.Millager.MOD_ID;

public class ResourceVariantHelper {

    private static final DeferredRegister<PaintingVariant> PAINTING_VARIANTS =
            DeferredRegister.create(Registries.PAINTING_VARIANT, MOD_ID);

    private static final DeferredRegister<BannerPattern> BANNER_PATTERNS =
            DeferredRegister.create(Registries.BANNER_PATTERN, MOD_ID);

    public static final ResourceKey<PaintingVariant> COCKTAIL_TUTORIAL = createVariantKey("how_to_make_a_molotov_cocktail");

    public static final ResourceKey<BannerPattern> VILLAGER = createPatternKey("villager");
    public static final ResourceKey<BannerPattern> VILLAGE_VILLAGER = createPatternKey("village_villager");

    public static final TagKey<BannerPattern> PATTERN_ITEM_VILLAGER = TagKey.create(
            Registries.BANNER_PATTERN, ResourceLocationHelper.create(MOD_ID, "pattern_item/villager"));

    public static final DeferredHolder<PaintingVariant, PaintingVariant> COCKTAIL_TUTORIAL_VARIANT =
            PAINTING_VARIANTS.register("how_to_make_a_molotov_cocktail", () -> new PaintingVariant(64, 64));

    public static final DeferredHolder<BannerPattern, BannerPattern> VILLAGER_PATTERN =
            BANNER_PATTERNS.register("villager", () -> new BannerPattern("mlv"));

    public static final DeferredHolder<BannerPattern, BannerPattern> VILLAGE_VILLAGER_PATTERN =
            BANNER_PATTERNS.register("village_villager", () -> new BannerPattern("village_villager"));

    public static ResourceKey<BannerPattern> createPatternKey(String string) {
        return ResourceKey.create(Registries.BANNER_PATTERN, ResourceLocationHelper.create(MOD_ID, string));
    }

    public static ResourceKey<PaintingVariant> createVariantKey(String string) {
        return ResourceKey.create(Registries.PAINTING_VARIANT, ResourceLocationHelper.create(MOD_ID, string));
    }

    public static void initialize() {
        PAINTING_VARIANTS.register();
        BANNER_PATTERNS.register();
    }

}
