package org.lzyzl.millager.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.lzyzl.millager.Millager;
import org.lzyzl.millager.block.MillagerMenuType;
import org.lzyzl.millager.client.gui.screens.TotemInfuserScreen;
import org.lzyzl.millager.client.render.HeadSpecialRenderer;
import org.lzyzl.millager.client.render.BucklerSpecialRenderer;
import org.lzyzl.millager.client.util.BlockRenderHelper;

import java.util.List;
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
            public <T extends BlockEntity, S extends BlockEntityRenderState> void registerBlockEntityRenderer(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T, S> provider) {
                BlockEntityRenderers.register(type, provider);
            }

            @Override
            public void registerLayerDefinition(ModelLayerLocation location, Supplier<LayerDefinition> supplier) {
                ModelLayerRegistry.registerModelLayer(location, supplier::get);
            }

            @Override
            public void registerBlockColor(List<BlockTintSource> tintSources, Block... blocks) {
                BlockColorRegistry.register(tintSources, blocks);
            }
        };

        MillagerEntityRenderers.registerEntityRenderers(ctx);
        MillagerModelLayers.registerModelLayers(ctx);
        BlockRenderHelper.registerBlockColors(ctx);

        MenuScreens.register(MillagerMenuType.TOTEM_INFUSER.get(), TotemInfuserScreen::new);

        SpecialModelRenderers.ID_MAPPER.put(
                Identifier.fromNamespaceAndPath(Millager.MOD_ID, "head"),
                HeadSpecialRenderer.Unbaked.MAP_CODEC
        );
        SpecialModelRenderers.ID_MAPPER.put(
                Identifier.fromNamespaceAndPath(Millager.MOD_ID, "buckler"),
                BucklerSpecialRenderer.Unbaked.MAP_CODEC
        );
    }
}
