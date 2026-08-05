package org.lzyzl.millager;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import org.lzyzl.millager.config.MillagerConfig;
import org.lzyzl.millager.entity.golem.BeeGolem;
import org.lzyzl.millager.entity.millager.Archer;
import org.lzyzl.millager.entity.millager.Breacher;
import org.lzyzl.millager.entity.millager.Doctor;
import org.lzyzl.millager.entity.millager.Mauler;
import org.lzyzl.millager.entity.millager.Rioter;
import org.lzyzl.millager.entity.millager.Scouter;
import org.lzyzl.millager.entity.millager.Swordmaster;
import org.lzyzl.millager.util.EnemyAttackHelper;
import org.lzyzl.millager.util.VillagerTradeHelper;

public class MillagerFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        MillagerConfig.load(FabricLoader.getInstance().getConfigDir());
        Millager.init();
        Millager.commonSetup();

        registerAttributes();
        registerVillagerTrades(VillagerTradeHelper.FARMER_KEY);
        registerVillagerTrades(VillagerTradeHelper.SHEPHERD_KEY);
        registerVillagerTrades(MillagerProfessionAndPoi.COMMANDER_KEY);

        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            builder.addMix(Potions.AWKWARD, Items.TORCHFLOWER, MillagerItems.ASCENSION.holder());
            builder.addMix(MillagerItems.ASCENSION.holder(), Items.REDSTONE, MillagerItems.LONG_ASCENSION.holder());
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> EnemyAttackHelper.onEntityJoinLevel(entity));

        ItemGroupEvents.modifyEntriesEvent(MillagerItemGroups.MILLAGER_CREATIVE_TAB_KEY)
                .register(entries -> MillagerItemGroups.addExtraContents(entries::accept, entries.getContext()));
    }

    private static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(MillagerEntityTypes.Bee_Golem.get(), BeeGolem.createAttributes());
        FabricDefaultAttributeRegistry.register(MillagerEntityTypes.Archers.get(), Archer.createAttributes());
        FabricDefaultAttributeRegistry.register(MillagerEntityTypes.Breachers.get(), Breacher.createAttributes());
        FabricDefaultAttributeRegistry.register(MillagerEntityTypes.Doctors.get(), Doctor.createAttributes());
        FabricDefaultAttributeRegistry.register(MillagerEntityTypes.Maulers.get(), Mauler.createAttributes());
        FabricDefaultAttributeRegistry.register(MillagerEntityTypes.Swordmasters.get(), Swordmaster.createAttributes());
        FabricDefaultAttributeRegistry.register(MillagerEntityTypes.Rioters.get(), Rioter.createAttributes());
        FabricDefaultAttributeRegistry.register(MillagerEntityTypes.Scouters.get(), Scouter.createAttributes());
    }

    private static void registerVillagerTrades(ResourceKey<VillagerProfession> professionKey) {
        VillagerProfession profession = BuiltInRegistries.VILLAGER_PROFESSION.get(professionKey.location());
        VillagerTradeHelper.registerVillagerTrades(professionKey,
                (level, listing) -> TradeOfferHelper.registerVillagerOffers(profession, level, offers -> offers.add(listing)));
    }
}
