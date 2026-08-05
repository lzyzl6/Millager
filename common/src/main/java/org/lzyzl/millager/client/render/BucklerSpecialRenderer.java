package org.lzyzl.millager.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.client.render.item.BucklerModel;

import java.util.function.Consumer;

public class BucklerSpecialRenderer implements SpecialModelRenderer<DataComponentMap> {
    private static final Material TEXTURE = new Material(TextureAtlas.LOCATION_ITEMS, Identifier.fromNamespaceAndPath("millager", "item/buckler"));

    private final MaterialSet materials;
    private final BucklerModel model;

    public BucklerSpecialRenderer(MaterialSet materials, BucklerModel model) {
        this.materials = materials;
        this.model = model;
    }

    @Override
    public @Nullable DataComponentMap extractArgument(ItemStack stack) {
        return stack.immutableComponents();
    }

    @Override
    public void submit(@Nullable DataComponentMap components, ItemDisplayContext displayContext, @NonNull PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.scale(1.0F, -1.0F, -1.0F);
        submitNodeCollector.submitModelPart(this.model.root(), poseStack, this.model.renderType(TEXTURE.atlasLocation()),
                lightCoords, overlayCoords, this.materials.get(TEXTURE), false, hasFoil, -1, null, outlineColor);
        poseStack.popPose();
    }

    @Override
    public void getExtents(@NonNull Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        poseStack.scale(1.0F, -1.0F, -1.0F);
        this.model.root().getExtentsForGui(poseStack, output);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public @NonNull MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public BucklerSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new BucklerSpecialRenderer(context.materials(), new BucklerModel(context.entityModelSet().bakeLayer(MillagerModelLayers.BUCKLER)));
        }
    }
}
