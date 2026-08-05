package org.lzyzl.millager.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.client.render.item.BucklerModel;

import java.util.function.Consumer;

public class BucklerSpecialRenderer implements SpecialModelRenderer<DataComponentMap> {
    private static final SpriteId TEXTURE = new SpriteId(TextureAtlas.LOCATION_ITEMS, Identifier.fromNamespaceAndPath("millager", "item/buckler"));

    private final SpriteGetter sprites;
    private final BucklerModel model;

    public BucklerSpecialRenderer(SpriteGetter sprites, BucklerModel model) {
        this.sprites = sprites;
        this.model = model;
    }

    @Override
    public @Nullable DataComponentMap extractArgument(ItemStack stack) {
        return stack.immutableComponents();
    }

    @Override
    public void submit(@Nullable DataComponentMap components, @NonNull PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        submitNodeCollector.submitModel(this.model, Unit.INSTANCE, poseStack, lightCoords, overlayCoords, -1, TEXTURE, this.sprites, outlineColor, null);
        if (hasFoil) {
            submitNodeCollector.order(1).submitModel(this.model, Unit.INSTANCE, poseStack, RenderTypes.entityGlint(), lightCoords, overlayCoords, -1, this.sprites.get(TEXTURE), 0, null);
        }
    }

    @Override
    public void getExtents(@NonNull Consumer<Vector3fc> output) {
        this.model.root().getExtentsForGui(new PoseStack(), output);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<DataComponentMap> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public @NonNull MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public BucklerSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new BucklerSpecialRenderer(context.sprites(), new BucklerModel(context.entityModelSet().bakeLayer(MillagerModelLayers.BUCKLER)));
        }
    }
}
