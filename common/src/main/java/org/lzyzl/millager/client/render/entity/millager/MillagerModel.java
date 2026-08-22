package org.lzyzl.millager.client.render.entity.millager;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.effects.SpearAnimations;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Ease;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.entity.millager.AbstractMillager;

public class MillagerModel<S extends MillagerRenderState> extends HumanoidModel<S> implements ArmedModel<S>, HeadedModel {
    public static final MeshTransformer BODY_TRANSFORMER = MeshTransformer.scaling(0.95F);
    final ModelPart body;
    private final ModelPart quiver;
    private final ModelPart back_cover;
    private final ModelPart head;
    private final ModelPart arms;
    private final ModelPart left_arm;
    private final ModelPart right_arm;
    private final ModelPart left_leg;
    private final ModelPart right_leg;

    protected MillagerModel(ModelPart root) {
        super(root);
        this.quiver = root.getChild("quiver");
        this.quiver.getChild("arrows");
        ModelPart backBottom = root.getChild("back_bottom");
        this.back_cover = backBottom.getChild("back_cover");
        backBottom.getChild("back_top");
        this.head = root.getChild("head");
        this.head.getChild("hat");
        this.head.getChild("headwear");
        this.head.getChild("nose");
        this.body = root.getChild("body");
        this.body.getChild("bodywear");
        this.arms = root.getChild("arms");
        this.arms.getChild("mirrored");
        this.left_arm = root.getChild("left_arm");
        this.right_arm = root.getChild("right_arm");
        this.left_leg = root.getChild("left_leg");
        this.right_leg = root.getChild("right_leg");
        ModelPart bags = root.getChild("bags");
        bags.getChild("front_right_bag");
        ModelPart moreBags = bags.getChild("more_bags");
        moreBags.getChild("back_right_bag");
        moreBags.getChild("back_left_bag");
        moreBags.getChild("front_left_bag");
        moreBags.getChild("front_middle_bag");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition quiver = partdefinition.addOrReplaceChild("quiver", CubeListBuilder.create(), PartPose.offsetAndRotation(0.1642F, 5.2929F, 4.5F, 0.0F, 0.0F, 1.5708F));
        quiver.addOrReplaceChild("quiver_r1", CubeListBuilder.create().texOffs(12, 56).addBox(-1.5F, -4.5F, -1.5F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1213F, 0.2929F, 0.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition arrows = quiver.addOrReplaceChild("arrows", CubeListBuilder.create().texOffs(24, 20).addBox(-0.6005F, -2.9853F, 0.5F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.7574F, -4.2426F, 0.499F, 0.0F, 0.0F, 0.7854F));
        arrows.addOrReplaceChild("arrows_r1", CubeListBuilder.create().texOffs(24, 20).addBox(-2.0F, -1.5F, 0.0F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3995F, -2.2353F, -0.5F, -0.6545F, 0.0F, 0.0F));
        arrows.addOrReplaceChild("arrows_r2", CubeListBuilder.create().texOffs(34, 9).addBox(-2.0F, -1.5F, 0.0F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3995F, -1.4853F, 0.0F, 0.3927F, 0.0F, 0.0F));
        arrows.addOrReplaceChild("arrows_r3", CubeListBuilder.create().texOffs(34, 9).addBox(-2.0F, -1.5F, 0.0F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3995F, -1.4853F, -0.25F, -0.3927F, 0.0F, 0.0F));

        PartDefinition back_bottom = partdefinition.addOrReplaceChild("back_bottom", CubeListBuilder.create().texOffs(0, 69).addBox(-4.0F, -21.0F, 3.0F, 8.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 25.0F, 0.0F));
        back_bottom.addOrReplaceChild("back_cover", CubeListBuilder.create().texOffs(15, 46).addBox(-4.0F, -3.0F, 0.0F, 8.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -21.0F, 3.0F));
        back_bottom.addOrReplaceChild("back_top", CubeListBuilder.create().texOffs(58, 54).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -20.0F, 6.0F));

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(46, 63).addBox(-8.0F, -8.0F, -6.0F, 16.0F, 16.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));
        head.addOrReplaceChild("headwear", CubeListBuilder.create().texOffs(48, 35).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.51F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 0.0F));

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(33, 15).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("bodywear", CubeListBuilder.create().texOffs(0, 19).addBox(-4.0F, -25.0F, -3.0F, 8.0F, 20.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 25.0F, 0.0F));

        PartDefinition arms = partdefinition.addOrReplaceChild("arms", CubeListBuilder.create().texOffs(29, 34).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(45, 0).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.95F, -1.05F, -0.7505F, 0.0F, 0.0F));
        arms.addOrReplaceChild("mirrored", CubeListBuilder.create().texOffs(45, 0).mirror().addBox(4.0F, -23.05F, -3.05F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 21.05F, 1.05F));

        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(64, 0).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(5.0F, 2.0F, 0.0F));
        partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(64, 17).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(29, 64).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(2.0F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(29, 64).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 12.0F, 0.0F));

        PartDefinition bags = partdefinition.addOrReplaceChild("bags", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        bags.addOrReplaceChild("front_right_bag", CubeListBuilder.create().texOffs(1, 62).addBox(-4.0F, -14.0F, -5.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition more_bags = bags.addOrReplaceChild("more_bags", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        more_bags.addOrReplaceChild("back_right_bag", CubeListBuilder.create().texOffs(1, 48).addBox(-4.0F, -5.0F, -3.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -9.0F, 6.0F));
        more_bags.addOrReplaceChild("back_left_bag", CubeListBuilder.create().texOffs(1, 55).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -12.0F, 4.0F));
        more_bags.addOrReplaceChild("front_left_bag", CubeListBuilder.create().texOffs(56, 15).addBox(2.0F, -14.0F, -5.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        more_bags.addOrReplaceChild("front_middle_bag", CubeListBuilder.create().texOffs(34, 2).addBox(-1.0F, -14.0F, -5.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 80, 80).apply(BODY_TRANSFORMER);
    }

    @Override
    public void setupAnim(S state) {
        this.quiver.resetPose();
        this.back_cover.resetPose();
        this.head.resetPose();
        this.body.resetPose();
        this.arms.resetPose();
        this.left_arm.resetPose();
        this.right_arm.resetPose();
        this.left_leg.resetPose();
        this.right_leg.resetPose();

        this.head.yRot = state.yRot * (float) (Math.PI / 180.0);
        this.head.xRot = state.xRot * (float) (Math.PI / 180.0);

        if (state.isRiding) {
            this.right_arm.xRot = (float) (-Math.PI / 5);
            this.right_arm.yRot = 0.0F;
            this.right_arm.zRot = 0.0F;
            this.left_arm.xRot = (float) (-Math.PI / 5);
            this.left_arm.yRot = 0.0F;
            this.left_arm.zRot = 0.0F;
            this.right_leg.xRot = -1.4137167F;
            this.right_leg.yRot = (float) (Math.PI / 10);
            this.right_leg.zRot = 0.07853982F;
            this.left_leg.xRot = -1.4137167F;
            this.left_leg.yRot = (float) (-Math.PI / 10);
            this.left_leg.zRot = -0.07853982F;
        } else {
            float f = state.walkAnimationSpeed;
            float g = state.walkAnimationPos;
            this.right_arm.xRot = Mth.cos(g * 0.6662F + (float) Math.PI) * 2.0F * f * 0.5F;
            this.right_arm.yRot = 0.0F;
            this.right_arm.zRot = 0.0F;
            this.left_arm.xRot = Mth.cos(g * 0.6662F) * 2.0F * f * 0.5F;
            this.left_arm.yRot = 0.0F;
            this.left_arm.zRot = 0.0F;
            this.right_leg.xRot = Mth.cos(g * 0.6662F) * 1.4F * f * 0.5F;
            this.right_leg.yRot = 0.0F;
            this.right_leg.zRot = 0.0F;
            this.left_leg.xRot = Mth.cos(g * 0.6662F + (float) Math.PI) * 1.4F * f * 0.5F;
            this.left_leg.yRot = 0.0F;
            this.left_leg.zRot = 0.0F;
        }

        AbstractMillager.MillagerPose pose = state.armPose;

        switch (pose) {
            case BOW_AND_ARROW -> {
                if (state.isLeftHanded) {
                    this.right_arm.yRot = -0.1F + this.head.yRot - 0.4F;
                    this.left_arm.yRot = 0.1F + this.head.yRot;
                } else {
                    this.right_arm.yRot = -0.1F + this.head.yRot;
                    this.left_arm.yRot = 0.1F + this.head.yRot + 0.4F;
                }
                this.right_arm.xRot = (-(float) Math.PI / 2F) + this.head.xRot;
                this.left_arm.xRot = (-(float) Math.PI / 2F) + this.head.xRot;
            }
            case NEUTRAL -> {
                this.right_arm.xRot = 0F;
                this.left_arm.xRot = 0F;
            }
            case CRAFTING_ARROW -> {
                float time = state.ageInTicks * 0.4F;
                float swing = Mth.sin(time) * 0.15F;

                this.head.xRot = 0.7F;
                this.head.yRot = 0.0F;
                this.head.xRot += swing * 0.2F;

                ModelPart mainArm = state.isLeftHanded ? this.left_arm : this.right_arm;
                ModelPart offArm = state.isLeftHanded ? this.right_arm : this.left_arm;

                mainArm.xRot = -0.9F + swing;
                mainArm.yRot = state.isLeftHanded ? 0.5F : -0.5F;
                mainArm.zRot = 0.0F;

                offArm.xRot = -0.9F - swing;
                offArm.yRot = state.isLeftHanded ? -0.5F : 0.5F;
                offArm.zRot = 0.0F;
            }
            case SPEAR -> {
                if (state.isLeftHanded) {
                    SpearAnimations.thirdPersonHandUse(this.left_arm, this.head, true, state.getUseItemStackForArm(HumanoidArm.LEFT), state);
                } else {
                    SpearAnimations.thirdPersonHandUse(this.right_arm, this.head, true, state.getUseItemStackForArm(HumanoidArm.RIGHT), state);
                }
            }
            case SPELLCASTING -> {
                this.right_arm.z = 0.0F;
                this.right_arm.x = -5.0F;
                this.left_arm.z = 0.0F;
                this.left_arm.x = 5.0F;
                this.right_arm.xRot = Mth.cos(state.ageInTicks * 0.6662F) * 0.25F;
                this.left_arm.xRot = Mth.cos(state.ageInTicks * 0.6662F) * 0.25F;
                this.right_arm.zRot = (float) (Math.PI * 3.0 / 4.0);
                this.left_arm.zRot = (float) (-Math.PI * 3.0 / 4.0);
                this.right_arm.yRot = 0.0F;
                this.left_arm.yRot = 0.0F;
            }
            case HOLDING_ITEM -> {
                this.right_arm.xRot = this.right_arm.xRot * 0.5F - ((float) Math.PI / 10F);
                this.right_arm.yRot = 0.0F;

                this.left_arm.xRot = this.left_arm.xRot * 0.5F - ((float) Math.PI / 10F);
                this.left_arm.yRot = 0.0F;
            }
            case SWINGING_ARM -> {
                float f = state.attackTime;
                float h = Ease.outQuart(f);
                float i = Mth.sin(h * (float) Math.PI);
                float j = Mth.sin(f * (float) Math.PI) * -(this.head.xRot - 0.7F) * 0.75F;

                this.right_arm.xRot = this.right_arm.xRot * 0.5F - ((float) Math.PI / 10F);
                this.right_arm.yRot = 0.0F;

                this.left_arm.xRot = this.left_arm.xRot * 0.5F - ((float) Math.PI / 10F);
                this.left_arm.yRot = 0.0F;

                ModelPart modelPart = this.getArm(state.attackArm);

                modelPart.xRot -= i * 1.2F + j;
                modelPart.yRot += this.body.yRot * 2.0F;
                modelPart.zRot += Mth.sin((f * (float) Math.PI)) * -0.4F;
            }
            case SUMMONING -> {
                boolean isRightMain = !state.isLeftHanded;
                ModelPart mainArm = isRightMain ? this.right_arm : this.left_arm;
                ModelPart offArm = isRightMain ? this.left_arm : this.right_arm;

                mainArm.xRot = this.head.xRot - ((float) Math.PI / 2F);
                mainArm.yRot = this.head.yRot;
                mainArm.zRot = 0.0F;

                offArm.xRot = 1.2F;
                offArm.yRot = isRightMain ? -0.5F : 0.5F;
                offArm.zRot = isRightMain ? -0.5F : 0.5F;

                float speed = 0.02f;

                float openProgress = Math.min(1.0f, state.ageInTicks * speed % 100);

                this.back_cover.xRot = openProgress * ((float) Math.PI / 3F);
            }
            case SWORD_SHIELDING -> {
                float breathing = Mth.cos(state.ageInTicks * 0.08F) * 0.03F;
                this.head.xRot = 0.25F + breathing;
                this.body.xRot = 0.15F;

                this.right_arm.xRot = -1.4F;
                this.right_arm.yRot = -0.7F;
                this.right_arm.zRot = 0.15F;

                this.left_arm.xRot = -1.25F;
                this.left_arm.yRot = 0.8F;
                this.left_arm.zRot = -0.15F;
            }
            case REGAINING_SWORD -> {
                this.head.xRot = 0.1F;
                this.head.yRot = 0.0F;

                boolean isLeft = state.isLeftHanded;
                ModelPart mainArm = isLeft ? this.left_arm : this.right_arm;
                ModelPart offArm = isLeft ? this.right_arm : this.left_arm;

                mainArm.xRot = 0.0F;
                mainArm.yRot = 0.0F;
                mainArm.zRot = 0.0F;

                offArm.xRot = -1.5708F;
                offArm.yRot = isLeft ? -0.55F : 0.55F;
                offArm.zRot = 0.0F;
            }
            case APPROACHING -> {
                float breathe = Mth.cos(state.ageInTicks * 0.09F) * 0.05F;

                this.right_arm.xRot = -0.5F + breathe;
                this.left_arm.xRot = -0.5F - breathe;

                this.right_arm.zRot = 0.35F;
                this.left_arm.zRot = -0.35F;

                this.right_arm.yRot = -0.4F;
                this.left_arm.yRot = 0.4F;
            }
            case BASHING -> poseShieldBash(state);
            case ATTACKING ->
                    AnimationUtils.swingWeaponDown(this.right_arm, this.left_arm, state.attackArm, state.attackTime, state.ageInTicks);
            case FIXING -> {
                float headBreathe = Mth.cos(state.ageInTicks * 0.08F) * 0.05F;
                this.head.xRot = 0.2F + headBreathe;
                this.head.yRot = 0.0F;

                float repairTimer = state.ageInTicks * 0.6F;
                float swing = Mth.sin(repairTimer);
                float hammerSwing = swing > 0 ? swing * 0.2F : swing * 0.08F;

                ModelPart mainArm = state.isLeftHanded ? this.left_arm : this.right_arm;
                ModelPart offArm = state.isLeftHanded ? this.right_arm : this.left_arm;

                mainArm.xRot = -1.1F + hammerSwing;
                mainArm.yRot = state.isLeftHanded ? 0.5F : -0.5F;
                mainArm.zRot = 0.0F;

                offArm.xRot = -1.0F + (hammerSwing * 0.2F);
                offArm.yRot = state.isLeftHanded ? -0.5F : 0.5F;
                offArm.zRot = 0.0F;

                this.body.xRot = 0.1F;
            }
            case SHIELD -> {
                ModelPart arm = state.isLeftHanded ? this.right_arm : this.left_arm;
                poseBlockingArm(arm, state.isLeftHanded);
            }
            case THROWING -> {
                ModelPart arm = state.isLeftHanded ? this.left_arm : this.right_arm;
                arm.xRot = arm.xRot * 0.5F - (float) Math.PI;
                arm.yRot = 0.0F;
            }
            case TAUNTING -> {
                ModelPart mainArm = state.isLeftHanded ? this.left_arm : this.right_arm;
                ModelPart offArm = !state.isLeftHanded ? this.left_arm : this.right_arm;
                float time = state.ageInTicks;

                this.head.zRot = 0.25F * Mth.sin(0.4F * time);
                this.head.yRot = 0.3F * Mth.sin(0.4F * time);
                this.head.xRot = 0.3F;

                offArm.xRot = -0.9F;
                offArm.yRot = state.isLeftHanded ? -0.9F : 0.9F;
                offArm.zRot = 0.5F;

                float wave = Mth.sin(time * 0.3925F);

                float swingValue = (float) Math.atan(wave * 2.0F) * 0.6F;

                mainArm.yRot = (state.isLeftHanded ? 0.6F : -0.6F) + (state.isLeftHanded ? -swingValue : swingValue);
                mainArm.xRot = -0.95F;

                mainArm.zRot = state.isLeftHanded ? -0.1F : 0.1F;
            }
            case TOOT_HORN -> {
                ModelPart arm = state.isLeftHanded ? this.right_arm : this.left_arm;
                arm.xRot = Mth.clamp(this.head.xRot, -1.2F, 1.2F) - 1.4835298F;
                arm.yRot = this.head.yRot + (state.isLeftHanded ? -((float) Math.PI / 6F) : ((float) Math.PI / 6F));
            }
            case CROSSBOW_CHARGE ->
                    AnimationUtils.animateCrossbowCharge(this.right_arm, this.left_arm, state.maxCrossbowChargeDuration, state.ticksUsingItem, !state.isLeftHanded);
            case CROSSBOW_HOLD ->
                    AnimationUtils.animateCrossbowHold(this.right_arm, this.left_arm, this.head, !state.isLeftHanded);
        }

        if (state.isLeftHanded) this.quiver.zRot = -0.2F;

        boolean bl = pose == AbstractMillager.MillagerPose.NEUTRAL;
        this.arms.visible = bl;
        this.left_arm.visible = !bl;
        this.right_arm.visible = !bl;
    }

    @Override
    public @NonNull ModelPart getArm(@NonNull HumanoidArm humanoidArm) {
        return humanoidArm == HumanoidArm.LEFT ? this.left_arm : this.right_arm;
    }

    @Override
    public @NonNull ModelPart getHead() {
        return this.head;
    }

    private void poseBlockingArm(ModelPart modelPart, boolean bl) {
        modelPart.xRot = modelPart.xRot * 0.5F - 0.9424779F + Mth.clamp(this.head.xRot, (float) (-Math.PI * 4.0 / 9.0), 0.43633232F);
        modelPart.yRot = (bl ? -30.0F : 30.0F) * (float) (Math.PI / 180.0) + Mth.clamp(this.head.yRot, (float) (-Math.PI / 6), (float) (Math.PI / 6));
    }

    private void poseShieldBash(S state) {
        ModelPart shieldArm = state.isLeftHanded ? this.right_arm : this.left_arm;
        float direction = state.isLeftHanded ? -1.0F : 1.0F;
        float swing = Mth.sin(state.attackTime * (float) Math.PI);
        shieldArm.xRot = -0.9F;
        shieldArm.yRot = direction * (0.9F - swing * 1.8F);
        shieldArm.zRot = direction * (0.25F + swing * 0.35F);
        this.body.yRot = direction * swing * 0.15F;
    }

    @Override
    public void translateToHand(S state, @NonNull HumanoidArm humanoidArm, @NonNull PoseStack poseStack) {
        this.root.translateAndRotate(poseStack);
        this.getArm(humanoidArm).translateAndRotate(poseStack);
    }
}
