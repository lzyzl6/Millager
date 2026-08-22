package org.lzyzl.millager.client.render.entity.millager;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.item.LancerSpearItem;

public class MillagerModel<T extends AbstractMillager> extends HumanoidModel<T> {

    private final ModelPart quiver;
    private final ModelPart back_bottom;
    private final ModelPart back_cover;
    private final ModelPart arms;
    private final ModelPart bags;

    protected MillagerModel(ModelPart root) {
        super(root);
        this.quiver = root.getChild("quiver");
        this.quiver.getChild("arrows");
        this.back_bottom = root.getChild("back_bottom");
        this.back_cover = this.back_bottom.getChild("back_cover");
        this.back_bottom.getChild("back_top");
        root.getChild("hat");
        this.head.getChild("headwear");
        this.head.getChild("nose");
        this.body.getChild("bodywear");
        this.arms = root.getChild("arms");
        this.arms.getChild("mirrored");
        this.bags = root.getChild("bags");
        this.bags.getChild("front_right_bag");
        ModelPart moreBags = this.bags.getChild("more_bags");
        moreBags.getChild("back_right_bag");
        moreBags.getChild("back_left_bag");
        moreBags.getChild("front_left_bag");
        moreBags.getChild("front_middle_bag");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition quiver = root.addOrReplaceChild("quiver", CubeListBuilder.create(), PartPose.offsetAndRotation(0.1642F, 5.2929F, 4.5F, 0.0F, 0.0F, 1.5708F));
        quiver.addOrReplaceChild("quiver_r1", CubeListBuilder.create().texOffs(12, 56).addBox(-1.5F, -4.5F, -1.5F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1213F, 0.2929F, 0.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition arrows = quiver.addOrReplaceChild("arrows", CubeListBuilder.create().texOffs(24, 20).addBox(-0.6005F, -2.9853F, 0.5F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.7574F, -4.2426F, 0.499F, 0.0F, 0.0F, 0.7854F));
        arrows.addOrReplaceChild("arrows_r1", CubeListBuilder.create().texOffs(24, 20).addBox(-2.0F, -1.5F, 0.0F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3995F, -2.2353F, -0.5F, -0.6545F, 0.0F, 0.0F));
        arrows.addOrReplaceChild("arrows_r2", CubeListBuilder.create().texOffs(34, 9).addBox(-2.0F, -1.5F, 0.0F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3995F, -1.4853F, 0.0F, 0.3927F, 0.0F, 0.0F));
        arrows.addOrReplaceChild("arrows_r3", CubeListBuilder.create().texOffs(34, 9).addBox(-2.0F, -1.5F, 0.0F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3995F, -1.4853F, -0.25F, -0.3927F, 0.0F, 0.0F));

        PartDefinition back_bottom = root.addOrReplaceChild("back_bottom", CubeListBuilder.create().texOffs(0, 69).addBox(-4.0F, -21.0F, 3.0F, 8.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 25.0F, 0.0F));
        back_bottom.addOrReplaceChild("back_cover", CubeListBuilder.create().texOffs(15, 46).addBox(-4.0F, -3.0F, 0.0F, 8.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -21.0F, 3.0F));
        back_bottom.addOrReplaceChild("back_top", CubeListBuilder.create().texOffs(58, 54).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -20.0F, 6.0F));

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(46, 63).addBox(-8.0F, -8.0F, -6.0F, 16.0F, 16.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));
        head.addOrReplaceChild("headwear", CubeListBuilder.create().texOffs(48, 35).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.51F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(33, 15).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("bodywear", CubeListBuilder.create().texOffs(0, 19).addBox(-4.0F, -25.0F, -3.0F, 8.0F, 20.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 25.0F, 0.0F));

        PartDefinition arms = root.addOrReplaceChild("arms", CubeListBuilder.create().texOffs(29, 34).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(45, 0).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.95F, -1.05F, -0.7505F, 0.0F, 0.0F));
        arms.addOrReplaceChild("mirrored", CubeListBuilder.create().texOffs(45, 0).mirror().addBox(4.0F, -23.05F, -3.05F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 21.05F, 1.05F));

        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(64, 0).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(64, 17).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(29, 64).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(2.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(29, 64).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 12.0F, 0.0F));

        PartDefinition bags = root.addOrReplaceChild("bags", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        bags.addOrReplaceChild("front_right_bag", CubeListBuilder.create().texOffs(1, 62).addBox(-4.0F, -14.0F, -5.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition more_bags = bags.addOrReplaceChild("more_bags", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        more_bags.addOrReplaceChild("back_right_bag", CubeListBuilder.create().texOffs(1, 48).addBox(-4.0F, -5.0F, -3.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -9.0F, 6.0F));
        more_bags.addOrReplaceChild("back_left_bag", CubeListBuilder.create().texOffs(1, 55).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -12.0F, 4.0F));
        more_bags.addOrReplaceChild("front_left_bag", CubeListBuilder.create().texOffs(56, 15).addBox(2.0F, -14.0F, -5.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        more_bags.addOrReplaceChild("front_middle_bag", CubeListBuilder.create().texOffs(34, 2).addBox(-1.0F, -14.0F, -5.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 80, 80);
    }

    @Override
    public void setupAnim(@NonNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.quiver.resetPose();
        this.back_cover.resetPose();
        this.head.resetPose();
        this.body.resetPose();
        this.arms.resetPose();
        this.leftArm.resetPose();
        this.rightArm.resetPose();
        this.leftLeg.resetPose();
        this.rightLeg.resetPose();
        this.arms.visible = true;
        this.leftArm.visible = true;
        this.rightArm.visible = true;
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);

        if (this.riding) {
            this.rightArm.xRot = (-(float)Math.PI / 5F);
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.0F;
            this.leftArm.xRot = (-(float)Math.PI / 5F);
            this.leftArm.yRot = 0.0F;
            this.leftArm.zRot = 0.0F;
            this.rightLeg.xRot = -1.4137167F;
            this.rightLeg.yRot = ((float)Math.PI / 10F);
            this.rightLeg.zRot = 0.07853982F;
            this.leftLeg.xRot = -1.4137167F;
            this.leftLeg.yRot = (-(float)Math.PI / 10F);
            this.leftLeg.zRot = -0.07853982F;
        }  else {
            this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F;
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.0F;
            this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
            this.leftArm.yRot = 0.0F;
            this.leftArm.zRot = 0.0F;
            this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * 0.5F;
            this.rightLeg.yRot = 0.0F;
            this.rightLeg.zRot = 0.0F;
            this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount * 0.5F;
            this.leftLeg.yRot = 0.0F;
            this.leftLeg.zRot = 0.0F;
        }

        AbstractMillager.MillagerPose pose = entity.getMillagerPose();
        boolean leftHanded = entity.getMainArm() == HumanoidArm.LEFT;

        switch (pose) {
            case SPELLCASTING -> {
                this.rightArm.z = 0.0F;
                this.rightArm.x = -5.0F;
                this.leftArm.z = 0.0F;
                this.leftArm.x = 5.0F;
                this.rightArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
                this.leftArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
                this.rightArm.zRot = (float) (Math.PI * 3.0D / 4.0D);
                this.leftArm.zRot = (float) (-Math.PI * 3.0D / 4.0D);
                this.rightArm.yRot = 0.0F;
                this.leftArm.yRot = 0.0F;
            }
            case BOW_AND_ARROW -> {
                if (leftHanded) {
                    this.rightArm.yRot = -0.1F + this.head.yRot - 0.4F;
                    this.leftArm.yRot = 0.1F + this.head.yRot;
                } else {
                    this.rightArm.yRot = -0.1F + this.head.yRot;
                    this.leftArm.yRot = 0.1F + this.head.yRot + 0.4F;
                }
                this.rightArm.xRot = (-(float) Math.PI / 2F) + this.head.xRot;
                this.leftArm.xRot = (-(float) Math.PI / 2F) + this.head.xRot;
            }
            case NEUTRAL -> {
                this.rightArm.xRot = 0.0F;
                this.leftArm.xRot = 0.0F;
            }
            case CRAFTING_ARROW -> {
                float time = ageInTicks * 0.4F;
                float swing = Mth.sin(time) * 0.15F;
                this.head.xRot = 0.7F + swing * 0.2F;
                this.head.yRot = 0.0F;
                ModelPart mainArm = leftHanded ? this.leftArm : this.rightArm;
                ModelPart offArm = leftHanded ? this.rightArm : this.leftArm;
                mainArm.xRot = -0.9F + swing;
                mainArm.yRot = leftHanded ? 0.5F : -0.5F;
                offArm.xRot = -0.9F - swing;
                offArm.yRot = leftHanded ? -0.5F : 0.5F;
                this.quiver.visible = true;
            }
            case CROSSBOW_CHARGE ->
                AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, entity, !leftHanded);
            case CROSSBOW_HOLD ->
                AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, !leftHanded);
            case HOLDING_ITEM -> {
                this.rightArm.xRot = this.rightArm.xRot * 0.5F - ((float) Math.PI / 10F);
                this.leftArm.xRot = this.leftArm.xRot * 0.5F - ((float) Math.PI / 10F);
            }
            case SPEAR -> {
                ModelPart spearArm = leftHanded ? this.leftArm : this.rightArm;
                ItemStack spearStack = entity.isUsingItem() ? entity.getUseItem() : entity.getMainHandItem();
                if (!(spearStack.getItem() instanceof LancerSpearItem spear)) break;
                float partialTick = Mth.clamp(ageInTicks - entity.tickCount, 0.0F, 1.0F);
                float useTime = entity.isUsingItem() ? entity.getTicksUsingItem() + partialTick : 0.0F;
                LancerSpearAnimations.thirdPersonHandUse(spearArm, this.head, !leftHanded,
                        entity.isFallFlying(), this.swimAmount, spear, useTime);
            }
            case SWINGING_ARM -> {
                float progress = this.attackTime;
                float remaining = 1.0F - progress;
                float eased = 1.0F - remaining * remaining * remaining * remaining;
                float swing = Mth.sin(eased * (float) Math.PI);
                float headOffset = Mth.sin(progress * (float) Math.PI) * -(this.head.xRot - 0.7F) * 0.75F;

                this.rightArm.xRot = this.rightArm.xRot * 0.5F - ((float) Math.PI / 10F);
                this.rightArm.yRot = 0.0F;
                this.leftArm.xRot = this.leftArm.xRot * 0.5F - ((float) Math.PI / 10F);
                this.leftArm.yRot = 0.0F;

                ModelPart attackArm = entity.swingingArm == InteractionHand.MAIN_HAND
                        ? (leftHanded ? this.leftArm : this.rightArm)
                        : (leftHanded ? this.rightArm : this.leftArm);
                attackArm.xRot -= swing * 1.2F + headOffset;
                attackArm.yRot += this.body.yRot * 2.0F;
                attackArm.zRot += Mth.sin(progress * (float) Math.PI) * -0.4F;
            }
            case REGAINING_SWORD -> {
                this.head.xRot = 0.1F;
                this.head.yRot = 0.0F;
                ModelPart mainArm = leftHanded ? this.leftArm : this.rightArm;
                ModelPart offArm = leftHanded ? this.rightArm : this.leftArm;
                mainArm.xRot = 0.0F;
                offArm.xRot = -1.5708F;
                offArm.yRot = leftHanded ? -0.55F : 0.55F;
            }
            case ATTACKING ->
                AnimationUtils.swingWeaponDown(this.rightArm, this.leftArm, entity, this.attackTime, ageInTicks);
            case SWORD_SHIELDING -> {
                float breathe = Mth.cos(ageInTicks * 0.08F) * 0.03F;
                this.head.xRot = 0.25F + breathe;
                this.body.xRot = 0.15F;
                this.rightArm.xRot = -1.4F;
                this.rightArm.yRot = -0.7F;
                this.rightArm.zRot = 0.15F;
                this.leftArm.xRot = -1.25F;
                this.leftArm.yRot = 0.8F;
                this.leftArm.zRot = -0.15F;
            }
            case APPROACHING -> {
                float breathe = Mth.cos(ageInTicks * 0.09F) * 0.05F;
                this.rightArm.xRot = -0.5F + breathe;
                this.leftArm.xRot = -0.5F - breathe;
                this.rightArm.zRot = 0.35F;
                this.leftArm.zRot = -0.35F;
                this.rightArm.yRot = -0.4F;
                this.leftArm.yRot = 0.4F;
            }
            case FIXING -> {
                this.head.xRot = 0.2F + Mth.cos(ageInTicks * 0.08F) * 0.05F;
                float hammerSwing = Mth.sin(ageInTicks * 0.6F);
                hammerSwing = hammerSwing > 0 ? hammerSwing * 0.2F : hammerSwing * 0.08F;
                ModelPart mainArm = leftHanded ? this.leftArm : this.rightArm;
                ModelPart offArm = leftHanded ? this.rightArm : this.leftArm;
                mainArm.xRot = -1.1F + hammerSwing;
                mainArm.yRot = leftHanded ? 0.5F : -0.5F;
                offArm.xRot = -1.0F + hammerSwing * 0.2F;
                offArm.yRot = leftHanded ? -0.5F : 0.5F;
            }
            case SHIELD -> {
                ModelPart shieldArm = leftHanded ? this.rightArm : this.leftArm;
                shieldArm.xRot = shieldArm.xRot * 0.5F - 0.9424779F + Mth.clamp(this.head.xRot, -1.396F, 0.436F);
                shieldArm.yRot = (leftHanded ? -30.0F : 30.0F) * (float) (Math.PI / 180.0) + Mth.clamp(this.head.yRot, -0.523F, 0.523F);
            }
            case THROWING -> {
                ModelPart throwArm = leftHanded ? this.leftArm : this.rightArm;
                throwArm.xRot = throwArm.xRot * 0.5F - (float) Math.PI;
            }
            case TAUNTING -> {
                ModelPart mainArm = leftHanded ? this.leftArm : this.rightArm;
                ModelPart offArm = leftHanded ? this.rightArm : this.leftArm;
                this.head.zRot = 0.25F * Mth.sin(0.4F * ageInTicks);
                this.head.yRot = 0.3F * Mth.sin(0.4F * ageInTicks);
                this.head.xRot = 0.3F;
                offArm.xRot = -0.9F;
                offArm.yRot = leftHanded ? -0.9F : 0.9F;
                offArm.zRot = 0.5F;
                float wave = Mth.sin(ageInTicks * 0.3925F);
                float swingValue = (float) Math.atan(wave * 2.0F) * 0.6F;
                mainArm.yRot = (leftHanded ? 0.6F : -0.6F) + (leftHanded ? -swingValue : swingValue);
                mainArm.xRot = -0.95F;
                mainArm.zRot = leftHanded ? -0.1F : 0.1F;
            }
            case TOOT_HORN -> {
                ModelPart hornArm = leftHanded ? this.rightArm : this.leftArm;
                hornArm.xRot = Mth.clamp(this.head.xRot, -1.2F, 1.2F) - 1.4835298F;
                hornArm.yRot = this.head.yRot + (leftHanded ? -0.523F : 0.523F);
            }
            case SUMMONING -> {
                ModelPart mainArm = leftHanded ? this.leftArm : this.rightArm;
                ModelPart offArm = leftHanded ? this.rightArm : this.leftArm;
                mainArm.xRot = this.head.xRot - ((float) Math.PI / 2F);
                offArm.xRot = 1.2F;
                offArm.yRot = leftHanded ? -0.5F : 0.5F;
                offArm.zRot = leftHanded ? -0.5F : 0.5F;
                float openProgress = Math.min(1.0f, ageInTicks * 0.02f % 100);
                this.back_cover.xRot = openProgress * ((float) Math.PI / 3F);
            }
            case SHIELD_BASH -> {
                ModelPart shieldArm = leftHanded ? this.rightArm : this.leftArm;
                float direction = leftHanded ? -1.0F : 1.0F;
                float swing = Mth.sin(this.attackTime * (float) Math.PI);
                shieldArm.xRot = -0.9F;
                shieldArm.yRot = direction * (0.9F - swing * 1.8F);
                shieldArm.zRot = direction * (0.25F + swing * 0.35F);
                this.body.yRot = direction * swing * 0.15F;
            }
            default -> {
            }
        }

        this.hat.copyFrom(this.head);
        this.hat.xRot -= (float) Math.PI / 2F;
        this.arms.visible = pose == AbstractMillager.MillagerPose.NEUTRAL;
        this.leftArm.visible = pose != AbstractMillager.MillagerPose.NEUTRAL;
        this.rightArm.visible = pose != AbstractMillager.MillagerPose.NEUTRAL;
    }

    @Override
    public @NonNull ModelPart getArm(@NonNull HumanoidArm humanoidArm) {
        return humanoidArm == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
    }

    @Override
    protected @NonNull Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.hat,
                this.arms, this.quiver, this.back_bottom, this.bags);
    }
}
