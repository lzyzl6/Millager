package org.lzyzl.millager.client.render.entity.golem;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;


public class BeeGolemModel extends EntityModel<BeeGolemRenderState> {

    private final ModelPart leftAntenna;
    private final ModelPart rightAntenna;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart frontLeg;
    private final ModelPart midLeg;
    private final ModelPart backLeg;

    public BeeGolemModel(ModelPart root) {
        super(root);
        this.leftAntenna = root.getChild("left_antenna");
        this.rightAntenna = root.getChild("right_antenna");
        ModelPart torso = root.getChild("torso");
        this.leftWing = torso.getChild("left_wing");
        this.rightWing = torso.getChild("right_wing");
        this.frontLeg = root.getChild("front_legs");
        this.midLeg = root.getChild("middle_legs");
        this.backLeg = root.getChild("back_legs");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition torso = partdefinition.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 14).addBox(-2.5F, -2.0F, -3.0F, 5.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 20.0F, 0.0F));

        torso.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(-6, 7).mirror().addBox(-0.5F, 0.0F, -3.0F, 5.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(2.0F, -2.0F, 0.0F));

        torso.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(-6, 7).addBox(-4.5F, 0.0F, -3.0F, 5.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -2.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_antenna", CubeListBuilder.create().texOffs(4, -2).addBox(0.0F, -1.0F, -1.5F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 19.0F, -4.5F));

        partdefinition.addOrReplaceChild("right_antenna", CubeListBuilder.create().texOffs(4, 1).addBox(0.0F, -1.0F, -1.5F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, 19.0F, -4.5F));

        partdefinition.addOrReplaceChild("stinger", CubeListBuilder.create().texOffs(1, 2).addBox(0.0F, -0.5F, -1.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 21.5F, 4.0F));

        partdefinition.addOrReplaceChild("front_legs", CubeListBuilder.create().texOffs(16, 3).addBox(-1.5F, -1.0F, 0.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 23.0F, -2.0F));

        partdefinition.addOrReplaceChild("middle_legs", CubeListBuilder.create().texOffs(12, 6).addBox(-2.5F, -1.0F, 0.0F, 5.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 23.0F, 0.0F));

        partdefinition.addOrReplaceChild("back_legs", CubeListBuilder.create().texOffs(12, 6).addBox(-2.5F, -1.0F, 0.0F, 5.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 23.0F, 2.0F));

        return LayerDefinition.create(meshdefinition, 24, 24);
    }
    @Override
    public void setupAnim(BeeGolemRenderState state) {
        super.setupAnim(state);

        if (!state.isOnGround) {
            float wingFreq = state.ageInTicks * 120.32113F * (float) (Math.PI / 180.0);
            this.rightWing.zRot = Mth.cos(wingFreq) * (float) Math.PI * 0.12F;
            this.leftWing.zRot = -this.rightWing.zRot;

            float f1 = Mth.cos(state.ageInTicks * 0.12F);

            this.root.xRot = 0.05F + f1 * (float) Math.PI * 0.015F;

            this.leftAntenna.xRot = f1 * (float) Math.PI * 0.015F;
            this.rightAntenna.xRot = f1 * (float) Math.PI * 0.015F;

            this.frontLeg.xRot = -f1 * (float) Math.PI * 0.05F + (float) (Math.PI / 12);
            this.midLeg.xRot = (float) (Math.PI / 10);
            this.backLeg.xRot = -f1 * (float) Math.PI * 0.03F + (float) (Math.PI / 8);

            this.root.y = this.root.y - f1 * 0.3F;
        }
    }

}
