package org.lzyzl.millager;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import org.lzyzl.millager.block.MillagerMenuType;
import org.lzyzl.millager.client.ClientRegistrationContext;
import org.lzyzl.millager.client.MillagerEntityRenderers;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.client.MillagerNeoForgeConfigScreen;
import org.lzyzl.millager.client.gui.screens.TotemInfuserScreen;
import org.lzyzl.millager.client.util.BlockRenderHelper;
import org.lzyzl.millager.config.MillagerConfig;
import org.lzyzl.millager.entity.golem.BeeGolem;
import org.lzyzl.millager.entity.millager.*;
import org.lzyzl.millager.entity.projectile.RioterProjectile;
import org.lzyzl.millager.registry.NeoForgeRegistryFactory;
import org.lzyzl.millager.util.EnemyAttackHelper;
import org.lzyzl.millager.util.VillagerTradeHelper;

import java.util.function.Supplier;

@Mod(Millager.MOD_ID)
public class MillagerNeoForge {

    public MillagerNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        NeoForgeRegistryFactory.setModBus(modEventBus);
        MillagerConfig.load(FMLPaths.CONFIGDIR.get());
        Millager.init();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            MillagerNeoForgeConfigScreen.register(modContainer);
        }

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::onEntityAttributeCreation);

        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(Millager::commonSetup);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(MillagerItemGroups.MILLAGER_CREATIVE_TAB_KEY)) {
            MillagerItemGroups.addExtraContents(event::accept, event.getParameters());
        }
    }

    private void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(MillagerEntityTypes.Bee_Golem.get(), BeeGolem.createAttributes().build());
        event.put(MillagerEntityTypes.Archers.get(), Archer.createAttributes().build());
        event.put(MillagerEntityTypes.Breachers.get(), Breacher.createAttributes().build());
        event.put(MillagerEntityTypes.Lancers.get(), Lancer.createAttributes().build());
        event.put(MillagerEntityTypes.Doctors.get(), Doctor.createAttributes().build());
        event.put(MillagerEntityTypes.Maulers.get(), Mauler.createAttributes().build());
        event.put(MillagerEntityTypes.Swordmasters.get(), Swordmaster.createAttributes().build());
        event.put(MillagerEntityTypes.Rioters.get(), Rioter.createAttributes().build());
        event.put(MillagerEntityTypes.Scouters.get(), Scouter.createAttributes().build());
    }

    @SubscribeEvent
    public void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        PotionBrewing.Builder builder = event.getBuilder();
        builder.addMix(Potions.AWKWARD, Items.TORCHFLOWER, MillagerItems.ASCENSION.holder());
        builder.addMix(MillagerItems.ASCENSION.holder(), Items.REDSTONE, MillagerItems.LONG_ASCENSION.holder());
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        EnemyAttackHelper.onEntityJoinLevel(event.getEntity());
    }

    @SubscribeEvent
    public void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (event.getExplosion().getDirectSourceEntity() instanceof RioterProjectile rioterProjectile && rioterProjectile.isRioterProjectile()) {
            event.getAffectedEntities().removeIf(entity -> !(entity instanceof LivingEntity) || entity instanceof ArmorStand);
        }
    }

    @SubscribeEvent
    public void onVillagerTrades(VillagerTradesEvent event) {
        VillagerTradeHelper.registerVillagerTrades(
                BuiltInRegistries.VILLAGER_PROFESSION.getResourceKey(event.getType()).orElseThrow(),
                (level, listing) -> event.getTrades().get(level.intValue()).add(listing));
    }

    @EventBusSubscriber(modid = Millager.MOD_ID, value = Dist.CLIENT)
    public static class ClientEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ItemProperties.register(MillagerItems.buckler.get(), ResourceLocation.withDefaultNamespace("blocking"),
                        (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
                ItemProperties.register(MillagerItems.tntOnAStick.get(), ResourceLocation.fromNamespaceAndPath(Millager.MOD_ID, "in_hand"),
                        (stack, level, entity, seed) -> 1.0F);
                ItemProperties.register(MillagerItems.tntOnAStick.get(), ResourceLocation.fromNamespaceAndPath(Millager.MOD_ID, "throwing"),
                        (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            });
        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            MillagerEntityRenderers.registerEntityRenderers(new ClientRegistrationContext() {
                @Override
                public <T extends Entity> void registerEntityRenderer(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
                    event.registerEntityRenderer(type, provider);
                }

                @Override
                public <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T> provider) {
                    event.registerBlockEntityRenderer(type, provider);
                }

                @Override
                public void registerLayerDefinition(ModelLayerLocation location, Supplier<LayerDefinition> supplier) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void registerBlockColor(BlockColor color, Block... blocks) {
                    throw new UnsupportedOperationException();
                }
            });
        }

        @SubscribeEvent
        public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            MillagerModelLayers.registerModelLayers(new ClientRegistrationContext() {
                @Override
                public <T extends Entity> void registerEntityRenderer(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T> provider) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void registerLayerDefinition(ModelLayerLocation location, Supplier<LayerDefinition> supplier) {
                    event.registerLayerDefinition(location, supplier);
                }

                @Override
                public void registerBlockColor(BlockColor color, Block... blocks) {
                    throw new UnsupportedOperationException();
                }
            });
        }

        @SubscribeEvent
        public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
            BlockRenderHelper.registerBlockColors(new ClientRegistrationContext() {
                @Override
                public <T extends Entity> void registerEntityRenderer(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T> provider) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void registerLayerDefinition(ModelLayerLocation location, Supplier<LayerDefinition> supplier) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void registerBlockColor(BlockColor color, Block... blocks) {
                    event.register(color, blocks);
                }
            });
        }

        @SubscribeEvent
        public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
            event.register(MillagerMenuType.TOTEM_INFUSER.get(), TotemInfuserScreen::new);
        }
    }
}
