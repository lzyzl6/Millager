package org.lzyzl.millager.client.render.entity.millager;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.lzyzl.millager.item.LancerSpearItem;

public final class LancerSpearAnimations {
    private LancerSpearAnimations() {
    }

    public static void thirdPersonHandUse(ModelPart arm, ModelPart head, boolean rightArm, boolean fallFlying,
                                          float swimAmount, LancerSpearItem spear, float useTime) {
        int direction = rightArm ? 1 : -1;
        arm.yRot = -0.1F * direction + head.yRot;
        arm.xRot = -(float) Math.PI / 2.0F + head.xRot + 0.8F;
        if (fallFlying || swimAmount > 0.0F) arm.xRot -= 0.9599311F;
        arm.yRot = Mth.clamp(arm.yRot, -(float) Math.PI / 3.0F, (float) Math.PI / 3.0F);
        arm.xRot = Mth.clamp(arm.xRot, -(float) Math.PI * 2.0F / 3.0F, (float) Math.PI / 6.0F);
        if (useTime <= 0.0F) return;
        UseParams params = UseParams.fromProfile(spear.getKineticProfile(), useTime);
        arm.yRot -= direction * params.swayScaleFast * ((float) Math.PI / 180.0F) * params.swayIntensity;
        arm.zRot -= direction * params.swayScaleSlow * ((float) Math.PI / 360.0F) * params.swayIntensity;
        arm.xRot += ((float) Math.PI / 180.0F) * (-40.0F * params.raiseProgressStart
                + 30.0F * params.raiseProgressMiddle - 20.0F * params.raiseProgressEnd
                + 20.0F * params.lowerProgress + 10.0F * params.raiseBackProgress
                + 0.6F * params.swayScaleSlow * params.swayIntensity);
    }

    public static void thirdPersonUseItem(PoseStack poseStack, LancerSpearItem spear, float useTime,
                                          HumanoidArm arm, float attackTime, float ticksSinceHitFeedback) {
        if (useTime == 0.0F) return;
        LancerSpearItem.KineticProfile profile = spear.getKineticProfile();
        UseParams params = UseParams.fromProfile(profile, useTime);
        float attack = inQuad(progress(attackTime, 0.05F, 0.2F));
        float retract = inOutExpo(progress(attackTime, 0.4F, 1.0F));
        float raised = 1.0F - outBack(1.0F - params.raiseProgress);
        float hitFeedback = hitFeedbackAmount(ticksSinceHitFeedback);
        int direction = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(0.0F, -hitFeedback * 0.4F,
                -profile.forwardMovement() * (raised - params.raiseBackProgress) + hitFeedback);
        rotateAround(poseStack, Axis.XN, 70.0F * (params.raiseProgress - params.raiseBackProgress)
                - 40.0F * (attack - retract), -0.03125F);
        rotateAround(poseStack, Axis.YP, direction * 90.0F
                * (params.raiseProgress - params.swayProgress + 3.0F * retract + attack),
                0.0F);
    }

    public static void thirdPersonAttackItem(PoseStack poseStack, LancerSpearItem spear, float attackTime) {
        float attack = inQuad(progress(attackTime, 0.05F, 0.2F));
        float retract = inOutExpo(progress(attackTime, 0.4F, 1.0F));
        rotateAround(poseStack, Axis.XN, 70.0F * (attack - retract), -0.125F);
        poseStack.translate(0.0F, spear.getKineticProfile().forwardMovement() * (attack - retract), 0.0F);
    }

    private static void rotateAround(PoseStack poseStack, Axis axis, float degrees, float y) {
        poseStack.translate((float) 0.0, y, (float) 0.125);
        poseStack.mulPose(axis.rotationDegrees(degrees));
        poseStack.translate(-(float) 0.0, -y, -(float) 0.125);
    }

    private static float progress(float value, float start, float end) {
        return Mth.clamp((value - start) / (end - start), 0.0F, 1.0F);
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

    private static float hitFeedbackAmount(float ticksSinceHit) {
        return 0.4F * (outQuart(progress(ticksSinceHit, 1.0F, 3.0F))
                - inOutSine(progress(ticksSinceHit, 3.0F, 10.0F)));
    }

    private static float outQuart(float value) {
        float offset = 1.0F - value;
        return 1.0F - offset * offset * offset * offset;
    }

    private static float inOutSine(float value) {
        return -(Mth.cos((float) Math.PI * value) - 1.0F) / 2.0F;
    }

    private static float outCirc(float value) {
        return Mth.sqrt(1.0F - (value - 1.0F) * (value - 1.0F));
    }

    private static float inCirc(float value) {
        return 1.0F - Mth.sqrt(1.0F - value * value);
    }

    private static float outCubic(float value) {
        return 1.0F - (1.0F - value) * (1.0F - value) * (1.0F - value);
    }

    private static float inOutElastic(float value) {
        if (value == 0.0F || value == 1.0F) return value;
        float exponent = value < 0.5F ? 20.0F * value - 10.0F : -20.0F * value + 10.0F;
        float wave = Mth.sin((20.0F * value - 11.125F) * ((float) Math.PI * 2.0F / 4.5F));
        float scale = (float) Math.pow(2.0F, exponent);
        return value < 0.5F ? -scale * wave / 2.0F : scale * wave / 2.0F + 1.0F;
    }

    private record UseParams(float raiseProgress, float raiseProgressStart, float raiseProgressMiddle,
                             float raiseProgressEnd, float swayProgress, float lowerProgress,
                             float raiseBackProgress, float swayIntensity, float swayScaleSlow,
                             float swayScaleFast) {
        private static UseParams fromProfile(LancerSpearItem.KineticProfile profile, float useTime) {
            int delayEnd = profile.delayTicks();
            int swayStart = delayEnd + profile.dismountTicks() - 20;
            int knockbackEnd = delayEnd + profile.knockbackTicks();
            int swayEnd = knockbackEnd - 40;
            int damageEnd = delayEnd + profile.damageTicks();
            float raiseProgress = progress(useTime, 0.0F, delayEnd);
            float raiseProgressStart = progress(raiseProgress, 0.0F, 0.5F);
            float raiseProgressMiddle = progress(raiseProgress, 0.5F, 0.8F);
            float raiseProgressEnd = progress(raiseProgress, 0.8F, 1.0F);
            float swayProgress = progress(useTime, swayStart, swayEnd);
            float lowerProgress = outCubic(inOutElastic(progress(useTime - 20.0F, swayEnd, knockbackEnd)));
            float raiseBackProgress = progress(useTime, damageEnd - 5.0F, damageEnd);
            float swayIntensity = 2.0F * outCirc(swayProgress) - 2.0F * inCirc(raiseBackProgress);
            float swayScaleSlow = Mth.sin(useTime * 19.0F * ((float) Math.PI / 180.0F)) * swayIntensity;
            float swayScaleFast = Mth.sin(useTime * 30.0F * ((float) Math.PI / 180.0F)) * swayIntensity;
            return new UseParams(raiseProgress, raiseProgressStart, raiseProgressMiddle, raiseProgressEnd,
                    swayProgress, lowerProgress, raiseBackProgress, swayIntensity, swayScaleSlow, swayScaleFast);
        }
    }
}
