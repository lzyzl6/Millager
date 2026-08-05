package org.lzyzl.millager.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.MillagerProfessionAndPoi;

import java.util.Optional;
import java.util.function.BiConsumer;

public class VillagerTradeHelper {

    public static final TagKey<Structure> ON_OUTPOST_EXPLORER_MAPS = MiscHelper.createStructureKey("on_outpost_exploder_maps");

    /**
     * 加载器无关：由各端的村民交易注册钩子调用,sink 负责把 (等级, 交易) 写入对应加载器的交易表。
     * NeoForge: VillagerTradesEvent -> sink = (level, listing) -> event.getTrades().get(level).add(listing)
     * Fabric:   TradeOfferHelper.registerVillagerOffers(profession, level, f -> f.add(listing))
     */
    public static void registerVillagerTrades(ResourceKey<VillagerProfession> profession,
                                              BiConsumer<Integer, VillagerTrades.ItemListing> sink) {
        if (profession == VillagerProfession.FARMER) {
            registerLiquorTrades(sink);
        } else if (profession == VillagerProfession.SHEPHERD) {
            registerShepherdTrades(sink);
        } else if (profession == MillagerProfessionAndPoi.COMMANDER_KEY) {
            registerCommanderTrades(sink);
        }
    }

    private static void registerShepherdTrades(BiConsumer<Integer, VillagerTrades.ItemListing> sink) {
        sink.accept(5, (level, entity, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, random.nextInt(2, 4)),
                VillageBannerHelper.create(level.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)),
                12, 30, 0.05f
        ));
    }

    private static void registerLiquorTrades(BiConsumer<Integer, VillagerTrades.ItemListing> sink) {
        sink.accept(1, (level, entity, random) -> new MerchantOffer(
                new ItemCost(MillagerItems.liquor.get(), random.nextInt(2, 6)),
                new ItemStack(Items.EMERALD, 1),
                16, 2, 0.05f
        ));

        sink.accept(3, (level, entity, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, random.nextInt(1, 4)),
                new ItemStack(MillagerItems.liquor.get(), random.nextInt(2, 5)),
                12, 10, 0.05f
        ));
    }

    private static void registerCommanderTrades(BiConsumer<Integer, VillagerTrades.ItemListing> sink) {
        // Level 1
        sink.accept(1, (level, entity, random) -> new MerchantOffer(
                new ItemCost(Items.GUNPOWDER, random.nextInt(3, 8)),
                new ItemStack(Items.EMERALD, 1),
                16, 2, 0.05f
        ));
        sink.accept(1, (level, entity, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, random.nextInt(3, 6)),
                MiscHelper.createExplorerMap("pillager_outpost", ON_OUTPOST_EXPLORER_MAPS, MapDecorationTypes.TARGET_POINT, level, entity),
                3, 1, 0.2f
        ));

        // Level 2
        sink.accept(2, (level, entity, random) -> new MerchantOffer(
                new ItemCost(Items.AMETHYST_SHARD, random.nextInt(3, 7)),
                new ItemStack(Items.EMERALD, 1),
                16, 10, 0.05f
        ));
        sink.accept(2, (level, entity, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, 2),
                MiscHelper.getVariantPainting(level, ResourceVariantHelper.COCKTAIL_TUTORIAL),
                3, 5, 0.05f
        ));

        // Level 3
        sink.accept(3, (level, entity, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, random.nextInt(8, 13)),
                new ItemStack(random.nextInt(3) == 0 ? MillagerItems.infantryMusterOrder.get()
                        : random.nextBoolean() ? MillagerItems.cavalryMusterOrder.get()
                        : MillagerItems.randomMusterOrder.get()),
                3, 10, 0.2f
        ));
        sink.accept(3, (level, entity, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, 1),
                new ItemStack(MillagerItems.molotovCocktail.get(), random.nextInt(2, 5)),
                12, 10, 0.2f
        ));

        // Level 4
        sink.accept(4, (level, entity, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, random.nextInt(13, 17)),
                new ItemStack(Items.TRIAL_KEY, 1),
                3, 15, 0.2f
        ));
        sink.accept(4, (level, entity, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, random.nextInt(2, 6)),
                new ItemStack(MillagerItems.tntOnAStick.get(), random.nextInt(2, 6)),
                12, 15, 0.05f
        ));

        // Level 5
        sink.accept(5, (level, entity, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, random.nextInt(7, 13)),
                Optional.of(new ItemCost(Items.TOTEM_OF_UNDYING, 1)),
                new ItemStack(MillagerItems.totemOfHealing.get(), 1),
                3, 30, 0.2f
        ));
        sink.accept(5, (level, entity, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, random.nextInt(1, 3)),
                new ItemStack(MillagerItems.golemAmber.get(), 1),
                16, 30, 0.05f
        ));
    }
}
