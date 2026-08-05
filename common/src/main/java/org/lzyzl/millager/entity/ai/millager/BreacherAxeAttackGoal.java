package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.entity.millager.Breacher;

public class BreacherAxeAttackGoal extends MeleeAttackGoal {
    private final Breacher breacher;

    public BreacherAxeAttackGoal(Breacher breacher) {
        super(breacher, 1.0D, true);
        this.breacher = breacher;
    }

    @Override
    public boolean canUse() {
        if (this.breacher.getAxeCooldown() > 0) {
            return false;
        }
        LivingEntity target = this.breacher.getTarget();
        if (this.breacher.isPassenger() && (target == null || !this.breacher.isWithinMeleeAttackRange(target))) {
            return false;
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (!super.canContinueToUse() || this.breacher.getAxeCooldown() > 0) {
            return false;
        }
        LivingEntity target = this.breacher.getTarget();
        return !this.breacher.isPassenger() || target != null && this.breacher.isWithinMeleeAttackRange(target);
    }

    @Override
    public void start() {
        super.start();
        this.breacher.releaseUsingItem();
    }

    @Override
    protected void checkAndPerformAttack(@NonNull LivingEntity target) {
        boolean canPerformAttack = this.canPerformAttack(target);
        super.checkAndPerformAttack(target);
        if (canPerformAttack) {
            this.breacher.setAxeCooldown(40);
        }
    }
}
