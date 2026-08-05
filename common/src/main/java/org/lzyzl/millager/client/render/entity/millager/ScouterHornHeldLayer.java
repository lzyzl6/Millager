package org.lzyzl.millager.client.render.entity.millager;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.entity.millager.Scouter;

public class ScouterHornHeldLayer extends RenderLayer<Scouter, MillagerModel<Scouter>> {

    public ScouterHornHeldLayer(RenderLayerParent<Scouter, MillagerModel<Scouter>> parent) {
        super(parent);
    }

    @Override
    public void render(@NonNull PoseStack poseStack, @NonNull MultiBufferSource bufferSource, int packedLight, Scouter entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.getMillagerPose() == AbstractMillager.MillagerPose.TOOT_HORN) return;
        ItemStack horn = entity.getItemBySlot(EquipmentSlot.OFFHAND);
        if (horn.isEmpty()) return;
        poseStack.pushPose();
        this.getParentModel().body.translateAndRotate(poseStack);
        poseStack.translate(entity.getMainArm() == HumanoidArm.LEFT ? -0.27F : 0.27F, 0.65D, 0.21D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getMainArm() == HumanoidArm.LEFT ? 70.0F : -250.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-20.0F));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        Minecraft.getInstance().getItemRenderer().renderStatic(horn, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, entity.level(), 0);
        poseStack.popPose();
    }
}
