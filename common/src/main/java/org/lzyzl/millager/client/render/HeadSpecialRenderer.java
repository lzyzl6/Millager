package org.lzyzl.millager.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.SkullBlock;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.block.AbstractHeadBlock;
import org.lzyzl.millager.client.render.block.HeadBlockRenderer;

import java.util.Optional;
import java.util.function.Consumer;

public class HeadSpecialRenderer implements NoDataSpecialModelRenderer {
    private final SkullModelBase model;
    private final float animation;
    private final RenderType renderType;

    public HeadSpecialRenderer(SkullModelBase skullModelBase, float f, RenderType renderType) {
        this.model = skullModelBase;
        this.animation = f;
        this.renderType = renderType;
    }

    @Override
    public void submit(@NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        SkullBlockRenderer.submitSkull(this.animation, poseStack, submitNodeCollector, lightCoords, this.model, this.renderType, outlineColor, null);
    }

    public void getExtents(@NonNull Consumer<Vector3fc> consumer) {
        PoseStack poseStack = new PoseStack();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        SkullModelBase.State state = new SkullModelBase.State();
        state.animationPos = this.animation;
        state.yRot = 180.0F;
        this.model.setupAnim(state);
        this.model.root().getExtentsForGui(poseStack, consumer);
    }

    public record Unbaked(SkullBlock.Type kind, Optional<Identifier> textureOverride, float animation) implements NoDataSpecialModelRenderer.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        AbstractHeadBlock.Type.CODEC.fieldOf("kind").forGetter(Unbaked::kind),
                        Identifier.CODEC.optionalFieldOf("texture").forGetter(Unbaked::textureOverride),
                        Codec.FLOAT.optionalFieldOf("animation", 0.0F).forGetter(Unbaked::animation)
                ).apply(instance, Unbaked::new)
        );

        public @NonNull MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        public @Nullable HeadSpecialRenderer bake(BakingContext bakingContext) {
            SkullModelBase skullModelBase = HeadBlockRenderer.createModel(bakingContext.entityModelSet(), this.kind);
            if (skullModelBase == null) {
                return null;
            } else {
                RenderType renderType = HeadBlockRenderer.getSkullRenderType(this.kind);
                return new HeadSpecialRenderer(skullModelBase, this.animation, renderType);
            }
        }
    }
}