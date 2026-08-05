package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.entity.millager.Swordmaster;

public class SwordmasterMeleeAttackGoal extends MeleeAttackGoal {

    private final Swordmaster swordmaster;
    private int comboDelay = -1;
    private LivingEntity targetEntity;

    public SwordmasterMeleeAttackGoal(Swordmaster swordmaster, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(swordmaster, speedModifier, followingTargetEvenIfNotSeen);
        this.swordmaster = swordmaster;
    }

    @Override
    public boolean canUse() {
        if(this.swordmaster.getTarget() instanceof LivingEntity living && living.isUnderWater()) return false;
        return super.canUse();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.comboDelay > 0) {
            this.comboDelay--;
        } else if (this.comboDelay == 0) {
            this.performOffHandAttack();
        }
    }

    @Override
    protected void checkAndPerformAttack(@NonNull LivingEntity target) {
        if (this.canPerformAttack(target) && this.comboDelay < 0) {
            this.targetEntity = target;
            this.resetAttackCooldown();

            this.swordmaster.swing(InteractionHand.MAIN_HAND);
            this.swordmaster.startUsingItem(InteractionHand.MAIN_HAND);
            this.swordmaster.doHurtTarget(target);

            this.comboDelay = 4;
        }
    }

    private void performOffHandAttack() {
        if (this.targetEntity != null && this.targetEntity.isAlive() && this.swordmaster.isWithinMeleeAttackRange(this.targetEntity)) {
            this.targetEntity.invulnerableTime = 0;
            this.targetEntity.hurtTime = 0;

            this.swordmaster.swing(InteractionHand.OFF_HAND);
            this.swordmaster.startUsingItem(InteractionHand.OFF_HAND);
            this.swordmaster.doHurtTarget(this.targetEntity);
        }
        this.comboDelay = -1;
        this.targetEntity = null;
    }

    @Override
    public void stop() {
        super.stop();
        this.comboDelay = -1;
        this.targetEntity = null;
    }
}
