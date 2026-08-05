package org.lzyzl.millager;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import org.lzyzl.millager.config.MillagerConfig;
import org.lzyzl.millager.entity.golem.BeeGolem;
import org.lzyzl.millager.entity.millager.*;
import org.lzyzl.millager.util.EnemyAttackHelper;
import org.lzyzl.millager.util.VillagerTradeHelper;

public class MillagerFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        MillagerConfig.load(FabricLoader.getInstance().getConfigDir());
        Millager.init();
        Millager.commonSetup();

        registerAttributes();

        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            builder.addMix(Potions.AWKWARD, Items.TORCHFLOWER, MillagerItems.ASCENSION.holder());
            builder.addMix(MillagerItems.ASCENSION.holder(), Items.REDSTONE, MillagerItems.LONG_ASCENSION.holder());
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> EnemyAttackHelper.onEntityJoinLevel(entity));

        registerTrades(VillagerProfession.FARMER);
        registerTrades(VillagerProfession.SHEPHERD);
        registerTrades(MillagerProfessionAndPoi.COMMANDER_KEY);

        ItemGroupEvents.modifyEntriesEvent(MillagerItemGroups.MILLAGER_CREATIVE_TAB_KEY)
                .register(entries -> MillagerItemGroups.addExtraContents(entries::accept, entries.getContext()));
    }

    private static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(MillagerEntityTypes.Bee_Golem.get(), BeeGolem.createAttributes());
        FabricDefaultAttributeRegistry.register(MillagerEntityTypes.Archers.get(), Archer.createAttributes());
        FabricDefaultAttributeRegistry.register(MillagerEntityTypes.Breachers.get(), Breacher.createAttributes());
        FabricDefaultAttributeRegistry.register(MillagerEntityTypes.Lancers.get(), Lancer.createAttributes());
        FabricDefaultAttributeRegistry.register(MillagerEntityTypes.Doctors.get(), Doctor.createAttributes());
        FabricDefaultAttributeRegistry.register(MillagerEntityTypes.Maulers.get(), Mauler.createAttributes());
        FabricDefaultAttributeRegistry.register(MillagerEntityTypes.Swordmasters.get(), Swordmaster.createAttributes());
        FabricDefaultAttributeRegistry.register(MillagerEntityTypes.Rioters.get(), Rioter.createAttributes());
        FabricDefaultAttributeRegistry.register(MillagerEntityTypes.Scouters.get(), Scouter.createAttributes());
    }

    private static void registerTrades(ResourceKey<VillagerProfession> profession) {
        VillagerTradeHelper.registerVillagerTrades(profession, (level, listing) ->
                TradeOfferHelper.registerVillagerOffers(profession, level, factories -> factories.add(listing)));
    }
}
