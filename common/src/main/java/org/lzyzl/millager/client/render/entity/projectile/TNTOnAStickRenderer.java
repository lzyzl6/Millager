package org.lzyzl.millager.client.render.entity.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.Millager;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.entity.projectile.TNTOnAStick;
import org.lzyzl.millager.util.ResourceLocationHelper;

public class TNTOnAStickRenderer extends EntityRenderer<TNTOnAStick> {

    private final TNTOnAStickModel model;

    public TNTOnAStickRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new TNTOnAStickModel(context.bakeLayer(MillagerModelLayers.TNT_ON_A_STICK));
    }

    @Override
    public void render(@NonNull TNTOnAStick entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.4F, 0.4F, 0.4F);
        poseStack.translate(0.0F, -0.5F, 0.0F);
        this.model.setupAnim(entity, partialTick, 0.0F, 0.0F, 0.0F, 0.0F);
        this.model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutout(this.getTextureLocation(entity))), packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public @NonNull ResourceLocation getTextureLocation(@NonNull TNTOnAStick entity) {
        return ResourceLocationHelper.create(Millager.MOD_ID, "textures/projectile/tnt_on_a_stick_cude.png");
    }
}
