package org.lzyzl.millager.client.render.entity.millager;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.entity.millager.AbstractMillager;

public class ScouterHornHeldLayer<S extends ScouterRenderState, M extends MillagerModel<S>> extends RenderLayer<S, M> {

    public ScouterHornHeldLayer(RenderLayerParent<S, M> parent) {
        super(parent);
    }

    @Override
    public void submit(@NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, int packedLight, ScouterRenderState state, float limbSwing, float limbSwingAmount) {
        if (state.hornRenderState.isEmpty() || state.armPose == AbstractMillager.MillagerPose.TOOT_HORN) {
            return;
        }
        poseStack.pushPose();
        this.getParentModel().body.translateAndRotate(poseStack);
        poseStack.translate(state.isLeftHanded ? -0.25F : 0.25F, 0.65D, 0.21D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(state.isLeftHanded ? 70.0F : -250.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-20.0F));
        float scaleFactor = 0.5F;
        poseStack.scale(scaleFactor, scaleFactor, scaleFactor);
        state.hornRenderState.submit(
                poseStack,
                submitNodeCollector,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                0
        );
        poseStack.popPose();
    }
}
