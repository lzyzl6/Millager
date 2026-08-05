package org.lzyzl.millager.client.render.entity.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.entity.projectile.TNTOnAStick;

import static org.lzyzl.millager.Millager.MOD_ID;

public class TNTOnAStickRenderer extends EntityRenderer<TNTOnAStick,TNTOnAStickRenderState> {

    private static final Identifier TOAS_LOCATION = Identifier.fromNamespaceAndPath(MOD_ID,"textures/projectile/tnt_on_a_stick_cude.png");
    private final TNTOnAStickModel model;

    public TNTOnAStickRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new TNTOnAStickModel(context.bakeLayer(MillagerModelLayers.TNT_ON_A_STICK));
    }

    @Override
    public TNTOnAStickRenderState createRenderState() {
        return new TNTOnAStickRenderState();
    }

    @Override
    public void extractRenderState(TNTOnAStick tntOnAStick, TNTOnAStickRenderState tntOnAStickRenderState, float f) {
        tntOnAStickRenderState.yRot = tntOnAStick.onGround() ? tntOnAStick.getLastYRot() : tntOnAStick.getViewYRot(f);
        tntOnAStickRenderState.totalRotation = tntOnAStick.getRotationProgress(f);
        super.extractRenderState(tntOnAStick, tntOnAStickRenderState, f);
    }
    
    public void submit(TNTOnAStickRenderState TOASRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, @NonNull CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.scale(0.4F, 0.4F, 0.4F);
        poseStack.translate(0.0f,-0.5f,0.0f);
        submitNodeCollector.submitModel(this.model, TOASRenderState, poseStack, RenderTypes.entitySolid(TOAS_LOCATION), TOASRenderState.lightCoords, OverlayTexture.NO_OVERLAY, TOASRenderState.outlineColor, null);
        poseStack.popPose();
        super.submit(TOASRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

}
