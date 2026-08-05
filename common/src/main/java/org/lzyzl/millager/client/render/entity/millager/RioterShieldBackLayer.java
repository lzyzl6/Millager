package org.lzyzl.millager.client.render.entity.millager;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.entity.millager.AbstractMillager;

public class RioterShieldBackLayer<S extends RioterRenderState, M extends MillagerModel<S>> extends RenderLayer<S, M> {

    public RioterShieldBackLayer(RenderLayerParent<S, M> parent) {
        super(parent);
    }

    @Override
    public void submit(@NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, int packedLight, RioterRenderState state, float limbSwing, float limbSwingAmount) {
        if (state.shieldRenderState.isEmpty() || state.armPose != AbstractMillager.MillagerPose.NEUTRAL) {
            return;
        }

        poseStack.pushPose();

        this.getParentModel().body.translateAndRotate(poseStack);

        poseStack.translate(0.0D, 0.3D, 0.21D);

        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

        float scaleFactor = 1.6F;
        poseStack.scale(scaleFactor, scaleFactor, scaleFactor);

        state.shieldRenderState.submit(
                poseStack,
                submitNodeCollector,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                0
        );

        poseStack.popPose();
    }
}
