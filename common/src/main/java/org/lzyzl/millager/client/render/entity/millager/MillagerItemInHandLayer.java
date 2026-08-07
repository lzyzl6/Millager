package org.lzyzl.millager.client.render.entity.millager;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.component.DataComponents;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.entity.millager.Archer;
import org.lzyzl.millager.entity.millager.Doctor;
import org.lzyzl.millager.entity.millager.Lancer;
import org.lzyzl.millager.entity.millager.Mauler;
import org.lzyzl.millager.entity.millager.Rioter;
import org.lzyzl.millager.entity.millager.Scouter;
import org.lzyzl.millager.entity.millager.Swordmaster;
import org.lzyzl.millager.item.LancerSpearItem;

public class MillagerItemInHandLayer<T extends AbstractMillager> extends ItemInHandLayer<T, MillagerModel<T>> {

    private final ItemInHandRenderer itemInHandRenderer;

    public MillagerItemInHandLayer(RenderLayerParent<T, MillagerModel<T>> parent, ItemInHandRenderer itemInHandRenderer) {
        super(parent, itemInHandRenderer);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        boolean rightMainArm = entity.getMainArm() == HumanoidArm.RIGHT;
        ItemStack leftHandItem = rightMainArm ? entity.getOffhandItem() : entity.getMainHandItem();
        ItemStack rightHandItem = rightMainArm ? entity.getMainHandItem() : entity.getOffhandItem();
        AbstractMillager.MillagerPose pose = entity.getMillagerPose();

        if (entity instanceof Archer) {
            if (pose == AbstractMillager.MillagerPose.NEUTRAL) {
                if (rightMainArm) leftHandItem = ItemStack.EMPTY;
                else rightHandItem = ItemStack.EMPTY;
            } else if (pose == AbstractMillager.MillagerPose.CRAFTING_ARROW) {
                if (rightMainArm) {
                    rightHandItem = ItemStack.EMPTY;
                    leftHandItem = Items.ARROW.getDefaultInstance();
                } else {
                    leftHandItem = ItemStack.EMPTY;
                    rightHandItem = Items.ARROW.getDefaultInstance();
                }
            }
        } else if (entity instanceof Doctor
                && (pose == AbstractMillager.MillagerPose.SWINGING_ARM || pose == AbstractMillager.MillagerPose.HOLDING_ITEM)) {
            leftHandItem = rightMainArm ? Items.CARVED_PUMPKIN.getDefaultInstance() : Items.IRON_BLOCK.getDefaultInstance();
            rightHandItem = rightMainArm ? Items.IRON_BLOCK.getDefaultInstance() : Items.CARVED_PUMPKIN.getDefaultInstance();
        } else if (entity instanceof Lancer && pose == AbstractMillager.MillagerPose.SPELLCASTING) {
            leftHandItem = ItemStack.EMPTY;
            rightHandItem = ItemStack.EMPTY;
        } else if (entity instanceof Rioter && pose == AbstractMillager.MillagerPose.NEUTRAL) {
            leftHandItem = ItemStack.EMPTY;
            rightHandItem = ItemStack.EMPTY;
        } else if (entity instanceof Scouter && pose != AbstractMillager.MillagerPose.TOOT_HORN) {
            if (rightMainArm) leftHandItem = ItemStack.EMPTY;
            else rightHandItem = ItemStack.EMPTY;
        } else if (entity instanceof Swordmaster) {
            if (pose == AbstractMillager.MillagerPose.NEUTRAL) {
                leftHandItem = ItemStack.EMPTY;
                rightHandItem = ItemStack.EMPTY;
            } else if (pose == AbstractMillager.MillagerPose.REGAINING_SWORD) {
                if (rightMainArm) rightHandItem = ItemStack.EMPTY;
                else leftHandItem = ItemStack.EMPTY;
            }
        } else if (entity instanceof Mauler) {
            if (pose == AbstractMillager.MillagerPose.NEUTRAL) {
                leftHandItem = ItemStack.EMPTY;
                rightHandItem = ItemStack.EMPTY;
            } else if (pose == AbstractMillager.MillagerPose.FIXING) {
                if (rightMainArm) leftHandItem = Items.AMETHYST_BLOCK.getDefaultInstance();
                else rightHandItem = Items.AMETHYST_BLOCK.getDefaultInstance();
            }
        }

        if (!rightHandItem.isEmpty() || !leftHandItem.isEmpty()) {
            poseStack.pushPose();
            if (this.getParentModel().young) {
                poseStack.translate(0.0F, 0.75F, 0.0F);
                poseStack.scale(0.5F, 0.5F, 0.5F);
            }
            this.renderArmWithItem(entity, rightHandItem, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, poseStack, buffer, packedLight);
            this.renderArmWithItem(entity, leftHandItem, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, poseStack, buffer, packedLight);
            poseStack.popPose();
        }
    }

    @Override
    protected void renderArmWithItem(LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext,
                                     HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (stack.isEmpty()) return;
        poseStack.pushPose();
        this.getParentModel().translateToHand(arm, poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        boolean leftHand = arm == HumanoidArm.LEFT;
        poseStack.translate(leftHand ? -0.0625F : 0.0625F, 0.125F, -0.625F);
        if (entity instanceof Lancer && stack.getItem() instanceof LancerSpearItem spear) {
            if (isAttackingWith(entity, arm)) applySpearAttackTransform(poseStack, spear, entity.getAttackAnim(0.0F));
            if (entity.isUsingItem() && entity.getUsedItemHand() == (leftHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND)) {
                applySpearUseTransform(poseStack, spear, entity.getTicksUsingItem(), arm, entity.getAttackAnim(0.0F));
            }
        }
        ItemStack renderStack = stack;
        if (stack.getItem() instanceof LancerSpearItem) {
            renderStack = stack.copy();
            renderStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(1));
        }
        this.itemInHandRenderer.renderItem(entity, renderStack, displayContext, leftHand, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    private static boolean isAttackingWith(LivingEntity entity, HumanoidArm arm) {
        if (entity.getAttackAnim(0.0F) <= 0.0F) return false;
        boolean mainHand = entity.swingingArm == InteractionHand.MAIN_HAND;
        return arm == (mainHand ? entity.getMainArm() : entity.getMainArm().getOpposite());
    }

    private static void applySpearAttackTransform(PoseStack poseStack, LancerSpearItem spear, float attackTime) {
        float attack = inQuad(progress(attackTime, 0.05F, 0.2F));
        float retract = inOutExpo(progress(attackTime, 0.4F, 1.0F));
        rotateAround(poseStack, Axis.XN, 70.0F * (attack - retract), 0.0F, -0.125F, 0.125F);
        poseStack.translate(0.0F, spear.getKineticProfile().forwardMovement() * (attack - retract), 0.0F);
    }

    private static void applySpearUseTransform(PoseStack poseStack, LancerSpearItem spear, int useTicks,
                                               HumanoidArm arm, float attackTime) {
        LancerSpearItem.KineticProfile profile = spear.getKineticProfile();
        float raiseProgress = progress(useTicks, 0.0F, profile.delayTicks());
        float swayProgress = progress(useTicks, profile.delayTicks() + profile.dismountTicks() - 20.0F,
                profile.delayTicks() + profile.dismountTicks());
        float raiseBackProgress = progress(useTicks, spear.getDamageUseDuration() - 5.0F, spear.getDamageUseDuration());
        float attack = inQuad(progress(attackTime, 0.05F, 0.2F));
        float retract = inOutExpo(progress(attackTime, 0.4F, 1.0F));
        float raiseProgressModified = 1.0F - outBack(1.0F - raiseProgress);
        int invert = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(0.0F, 0.0F, -profile.forwardMovement() * (raiseProgressModified - raiseBackProgress));
        rotateAround(poseStack, Axis.XN, 70.0F * (raiseProgress - raiseBackProgress) - 40.0F * (attack - retract),
                0.0F, -0.03125F, 0.125F);
        rotateAround(poseStack, Axis.YP, invert * 90.0F * (raiseProgress - swayProgress + 3.0F * retract + attack),
                0.0F, 0.0F, 0.125F);
    }

    private static void rotateAround(PoseStack poseStack, Axis axis, float degrees, float x, float y, float z) {
        poseStack.translate(x, y, z);
        poseStack.mulPose(axis.rotationDegrees(degrees));
        poseStack.translate(-x, -y, -z);
    }

    private static float progress(float time, float start, float end) {
        return Math.max(0.0F, Math.min(1.0F, (time - start) / (end - start)));
    }

    private static float inQuad(float value) {
        return value * value;
    }

    private static float inOutExpo(float value) {
        if (value == 0.0F || value == 1.0F) return value;
        return value < 0.5F ? (float) Math.pow(2.0F, 20.0F * value - 10.0F) / 2.0F
                : (2.0F - (float) Math.pow(2.0F, -20.0F * value + 10.0F)) / 2.0F;
    }

    private static float outBack(float value) {
        float overshoot = 1.70158F;
        float offset = value - 1.0F;
        return 1.0F + (overshoot + 1.0F) * offset * offset * offset + overshoot * offset * offset;
    }
}
