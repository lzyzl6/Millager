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
    private float partialTick;

    public MillagerItemInHandLayer(RenderLayerParent<T, MillagerModel<T>> parent, ItemInHandRenderer itemInHandRenderer) {
        super(parent, itemInHandRenderer);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        this.partialTick = partialTick;
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
        } else if (entity instanceof Lancer && (pose == AbstractMillager.MillagerPose.SPELLCASTING
                || pose == AbstractMillager.MillagerPose.NEUTRAL)) {
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
        if (entity instanceof Lancer lancer && stack.getItem() instanceof LancerSpearItem spear) {
            float attackTime = entity.getAttackAnim(this.partialTick);
            if (isAttackingWith(entity, arm)) {
                LancerSpearAnimations.thirdPersonAttackItem(poseStack, spear, attackTime);
            }
            InteractionHand hand = arm == entity.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            if (entity.isUsingItem() && entity.getUsedItemHand() == hand) {
                LancerSpearAnimations.thirdPersonUseItem(poseStack, spear,
                        entity.getTicksUsingItem() + this.partialTick, arm, attackTime,
                        lancer.getTicksSinceSpearHit(this.partialTick));
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

    private boolean isAttackingWith(LivingEntity entity, HumanoidArm arm) {
        if (entity.getAttackAnim(this.partialTick) <= 0.0F) return false;
        boolean mainHand = entity.swingingArm == InteractionHand.MAIN_HAND;
        return arm == (mainHand ? entity.getMainArm() : entity.getMainArm().getOpposite());
    }

}
