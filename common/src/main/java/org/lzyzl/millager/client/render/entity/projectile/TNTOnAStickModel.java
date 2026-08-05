package org.lzyzl.millager.client.render.entity.projectile;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.entity.projectile.TNTOnAStick;

public class TNTOnAStickModel extends EntityModel<TNTOnAStick> {

    private final ModelPart main;

    public TNTOnAStickModel(ModelPart root) {
        this.main = root.getChild("main");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition core = main.addOrReplaceChild("core", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 0.0F));
        core.addOrReplaceChild("arm", CubeListBuilder.create().texOffs(28, 35).addBox(-2.0F, 8.0F, -2.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(TNTOnAStick entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.onGround()) {
            this.main.yRot = entity.getViewYRot(limbSwing) * ((float)Math.PI / 180f);
            this.main.xRot = entity.getRotationProgress(limbSwing) * ((float)Math.PI / 180f);
        } else {
            this.main.yRot = entity.getLastYRot() * ((float)Math.PI / 180f);
            this.main.xRot = entity.getRotationProgress(limbSwing) * ((float)Math.PI / 180f);
        }
    }

    @Override
    public void renderToBuffer(@NonNull PoseStack poseStack, @NonNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
