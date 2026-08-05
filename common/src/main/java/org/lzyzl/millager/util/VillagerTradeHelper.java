package org.lzyzl.millager.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.MillagerProfessionAndPoi;

import java.util.function.BiConsumer;

public class VillagerTradeHelper {

    public static final TagKey<Structure> ON_OUTPOST_EXPLORER_MAPS = MiscHelper.createStructureKey("on_outpost_exploder_maps");

    public static final ResourceKey<VillagerProfession> FARMER_KEY =
            ResourceKey.create(Registries.VILLAGER_PROFESSION, ResourceLocationHelper.create("minecraft", "farmer"));
    public static final ResourceKey<VillagerProfession> SHEPHERD_KEY =
            ResourceKey.create(Registries.VILLAGER_PROFESSION, ResourceLocationHelper.create("minecraft", "shepherd"));

    public static void registerVillagerTrades(ResourceKey<VillagerProfession> profession,
                                              BiConsumer<Integer, VillagerTrades.ItemListing> sink) {
        if (profession.equals(FARMER_KEY)) {
            registerLiquorTrades(sink);
        } else if (profession.equals(SHEPHERD_KEY)) {
            registerShepherdTrades(sink);
        } else if (profession.equals(MillagerProfessionAndPoi.COMMANDER_KEY)) {
            registerCommanderTrades(sink);
        }
    }

    private static void registerShepherdTrades(BiConsumer<Integer, VillagerTrades.ItemListing> sink) {
        sink.accept(5, (entity, random) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, random.nextInt(2, 4)),
                VillageBannerHelper.create(),
                12, 30, 0.05f
        ));
    }

    private static void registerLiquorTrades(BiConsumer<Integer, VillagerTrades.ItemListing> sink) {
        sink.accept(1, (entity, random) -> new MerchantOffer(
                new ItemStack(MillagerItems.liquor.get(), random.nextInt(2, 6)),
                new ItemStack(Items.EMERALD, 1),
                16, 2, 0.05f
        ));

        sink.accept(3, (entity, random) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, random.nextInt(1, 4)),
                new ItemStack(MillagerItems.liquor.get(), random.nextInt(2, 5)),
                12, 10, 0.05f
        ));
    }

    private static void registerCommanderTrades(BiConsumer<Integer, VillagerTrades.ItemListing> sink) {
        sink.accept(1, (entity, random) -> new MerchantOffer(
                new ItemStack(Items.GUNPOWDER, random.nextInt(3, 8)),
                new ItemStack(Items.EMERALD, 1),
                16, 2, 0.05f
        ));
        sink.accept(1, (entity, random) -> {
            if (!(entity.level() instanceof ServerLevel serverLevel)) return null;
            return new MerchantOffer(
                    new ItemStack(Items.EMERALD, random.nextInt(3, 6)),
                    MiscHelper.createExplorerMap("pillager_outpost", ON_OUTPOST_EXPLORER_MAPS, MapDecoration.Type.TARGET_POINT, serverLevel, entity),
                    3, 1, 0.2f
            );
        });

        sink.accept(2, (entity, random) -> new MerchantOffer(
                new ItemStack(Items.AMETHYST_SHARD, random.nextInt(3, 7)),
                new ItemStack(Items.EMERALD, 1),
                16, 10, 0.05f
        ));
        sink.accept(2, (entity, random) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 2),
                MiscHelper.getVariantPainting(ResourceVariantHelper.COCKTAIL_TUTORIAL),
                3, 5, 0.05f
        ));

        sink.accept(3, (entity, random) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, random.nextInt(8, 13)),
                new ItemStack(random.nextInt(3) == 0 ? MillagerItems.infantryMusterOrder.get()
                        : random.nextBoolean() ? MillagerItems.cavalryMusterOrder.get()
                        : MillagerItems.randomMusterOrder.get()),
                3, 10, 0.2f
        ));
        sink.accept(3, (entity, random) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 1),
                new ItemStack(MillagerItems.molotovCocktail.get(), random.nextInt(2, 5)),
                12, 10, 0.2f
        ));

        sink.accept(4, (entity, random) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, random.nextInt(2, 6)),
                new ItemStack(MillagerItems.tntOnAStick.get(), random.nextInt(2, 6)),
                12, 15, 0.05f
        ));

        sink.accept(5, (entity, random) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, random.nextInt(7, 13)),
                new ItemStack(Items.TOTEM_OF_UNDYING, 1),
                new ItemStack(MillagerItems.totemOfHealing.get(), 1),
                3, 30, 0.2f
        ));
        sink.accept(5, (entity, random) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, random.nextInt(1, 3)),
                new ItemStack(MillagerItems.golemAmber.get(), 1),
                16, 30, 0.05f
        ));
    }
}
