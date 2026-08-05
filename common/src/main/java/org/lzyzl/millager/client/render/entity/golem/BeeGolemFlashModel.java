package org.lzyzl.millager.client.render.entity.golem;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class BeeGolemFlashModel extends EntityModel<BeeGolemRenderState> {

    public BeeGolemFlashModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        meshdefinition.getRoot().addOrReplaceChild("light", CubeListBuilder.create().texOffs(1, 3).addBox(0.0F, -1.0F, -1.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 22.0F, 5.0F));
        return LayerDefinition.create(meshdefinition, 24, 24);
    }

    @Override
    public void setupAnim(BeeGolemRenderState state) {
        super.setupAnim(state);
        if (!state.isOnGround) {
            float f1 = Mth.cos(state.ageInTicks * 0.12F);
            this.root.xRot = 0.05F + f1 * (float) Math.PI * 0.015F;
            this.root.y = this.root.y - f1 * 0.3F;
        }
    }
}
