package org.lzyzl.millager.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.lzyzl.millager.Millager;
import org.lzyzl.millager.MillagerBlocks;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.block.MillagerMenuType;
import org.lzyzl.millager.client.gui.screens.TotemInfuserScreen;
import org.lzyzl.millager.client.util.BlockRenderHelper;

import java.util.function.Supplier;

public class MillagerFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientRegistrationContext ctx = new ClientRegistrationContext() {
            @Override
            public <T extends Entity> void registerEntityRenderer(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
                EntityRendererRegistry.register(type, provider);
            }

            @Override
            @SuppressWarnings({"unchecked", "rawtypes"})
            public <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T> provider) {
                BlockEntityRenderers.register((BlockEntityType) type, (BlockEntityRendererProvider) provider);
            }

            @Override
            public void registerLayerDefinition(ModelLayerLocation location, Supplier<LayerDefinition> supplier) {
                EntityModelLayerRegistry.registerModelLayer(location, supplier::get);
            }

            @Override
            public void registerBlockColor(BlockColor color, Block... blocks) {
                ColorProviderRegistry.BLOCK.register(color, blocks);
            }
        };

        MillagerEntityRenderers.registerEntityRenderers(ctx);
        MillagerModelLayers.registerModelLayers(ctx);
        BlockRenderHelper.registerBlockColors(ctx);
        ItemProperties.register(MillagerItems.buckler.get(), ResourceLocation.withDefaultNamespace("blocking"),
                (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
        ItemProperties.register(MillagerItems.tntOnAStick.get(), ResourceLocation.fromNamespaceAndPath(Millager.MOD_ID, "in_hand"),
                (stack, level, entity, seed) -> 1.0F);
        ItemProperties.register(MillagerItems.tntOnAStick.get(), ResourceLocation.fromNamespaceAndPath(Millager.MOD_ID, "throwing"),
                (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(),
                MillagerBlocks.SAND_TABLE.get(), MillagerBlocks.TIMED_FIRE.get(),
                MillagerBlocks.VILLAGER_HEAD.get(), MillagerBlocks.VILLAGER_WALL_HEAD.get(),
                MillagerBlocks.ILLAGER_HEAD.get(), MillagerBlocks.ILLAGER_WALL_HEAD.get(),
                MillagerBlocks.ROSE.get(), MillagerBlocks.POTTED_ROSE.get());
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.translucent(),
                MillagerBlocks.LIQUOR_CAULDRON.get(), MillagerBlocks.BREWING_CAULDRON.get());

        MenuScreens.register(MillagerMenuType.TOTEM_INFUSER.get(), TotemInfuserScreen::new);
    }
}
