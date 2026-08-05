package org.lzyzl.millager.client.render.entity.golem;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import org.lzyzl.millager.entity.golem.BeeGolem;

public class BeeGolemFlashModel extends EntityModel<BeeGolem> {

    private final ModelPart root;

    public BeeGolemFlashModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("light", CubeListBuilder.create().texOffs(1, 3).addBox(0.0F, -1.0F, -1.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 22.0F, 5.0F));
        return LayerDefinition.create(mesh, 24, 24);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public void setupAnim(BeeGolem entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.resetPose();
        if (!entity.onGround()) {
            float f1 = Mth.cos(ageInTicks * 0.12F);
            this.root.xRot = 0.05F + f1 * (float) Math.PI * 0.015F;
            this.root.y = this.root.y - f1 * 0.3F;
        }
    }
}
