package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import org.lzyzl.millager.entity.millager.Rioter;

import java.util.EnumSet;

public class RioterShieldGoal extends Goal {

    private final Rioter rioter;

    public RioterShieldGoal(Rioter rioter) {
        this.rioter = rioter;
        this.setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if(this.rioter.isTaunting() || this.rioter.isThrowing()) return false;
        if(this.rioter.getDisableCooldown() > 0 || this.rioter.getActivityTicks() > 0) return false;
        return this.rioter.getTarget() != null && this.rioter.getTarget().isAlive();
    }

    @Override
    public void tick() {
        this.rioter.updateTargetingAndDistance();
    }

    @Override
    public void start() {
        this.rioter.setAggressive(true);
        this.rioter.startUsingItem(InteractionHand.OFF_HAND);
    }

    @Override
    public void stop() {
        this.rioter.setAggressive(false);
        this.rioter.releaseUsingItem();
    }
}
