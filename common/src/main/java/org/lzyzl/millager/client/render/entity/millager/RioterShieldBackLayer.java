package org.lzyzl.millager.client.render.entity.millager;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.entity.millager.Rioter;

public class RioterShieldBackLayer extends RenderLayer<Rioter, MillagerModel<Rioter>> {

    public RioterShieldBackLayer(RenderLayerParent<Rioter, MillagerModel<Rioter>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Rioter entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.getMillagerPose() != AbstractMillager.MillagerPose.NEUTRAL) return;
        ItemStack shield = entity.getItemBySlot(EquipmentSlot.OFFHAND);
        if (shield.isEmpty()) return;
        poseStack.pushPose();
        this.getParentModel().body.translateAndRotate(poseStack);
        poseStack.translate(0.0D, 0.23D, 0.21D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.scale(1.6F, 1.6F, 1.6F);
        Minecraft.getInstance().getItemRenderer().renderStatic(shield, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, entity.level(), 0);
        poseStack.popPose();
    }
}
