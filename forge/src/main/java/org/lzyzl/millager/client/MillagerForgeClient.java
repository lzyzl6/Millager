package org.lzyzl.millager.client;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lzyzl.millager.Millager;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.block.MillagerMenuType;
import org.lzyzl.millager.client.gui.screens.TotemInfuserScreen;
import org.lzyzl.millager.client.util.BlockRenderHelper;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Millager.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class MillagerForgeClient {

    private MillagerForgeClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(MillagerItems.buckler.get(), ResourceLocation.withDefaultNamespace("blocking"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(MillagerItems.tntOnAStick.get(), ResourceLocation.fromNamespaceAndPath(Millager.MOD_ID, "in_hand"),
                    (stack, level, entity, seed) -> 1.0F);
            MenuScreens.register(MillagerMenuType.TOTEM_INFUSER.get(), TotemInfuserScreen::new);
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
            @SuppressWarnings({"unchecked", "rawtypes"})
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
}
