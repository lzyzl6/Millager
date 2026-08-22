package org.lzyzl.millager.entity.ai.vanilla;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.item.LancerSpearItem;

import java.util.EnumSet;

public class LancerSpearUseGoal<T extends AbstractMillager> extends Goal {
    private static final double MAX_FLEEING_TIME = reducedTickDelay(100);
    private final T mob;
    private final double speedModifierWhenCharging;
    private final double speedModifierWhenRepositioning;
    private final float approachDistanceSq;
    private final float targetInRangeRadiusSq;
    @Nullable
    private SpearUseState state;

    public LancerSpearUseGoal(T mob, double speedModifierWhenCharging, double speedModifierWhenRepositioning,
                              float approachDistance, float targetInRangeRadius) {
        this.mob = mob;
        this.speedModifierWhenCharging = speedModifierWhenCharging;
        this.speedModifierWhenRepositioning = speedModifierWhenRepositioning;
        this.approachDistanceSq = approachDistance * approachDistance;
        this.targetInRangeRadiusSq = targetInRangeRadius * targetInRangeRadius;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() != null && isLancerSpear(this.mob.getMainHandItem()) && !this.mob.isUsingItem();
    }

    @Override
    public boolean canContinueToUse() {
        return this.state != null && !this.state.done && this.mob.getTarget() != null
                && isLancerSpear(this.mob.getMainHandItem());
    }

    @Override
    public void start() {
        this.mob.setAggressive(true);
        this.state = new SpearUseState();
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
        this.mob.setAggressive(false);
        this.state = null;
        this.mob.stopUsingItem();
    }

    @Override
    public void tick() {
        if (this.state == null) return;
        LivingEntity target = this.mob.getTarget();
        if (target == null) return;
        double distanceSq = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        float speed = 1.0F;
        int passengerOffset = this.mob.isPassenger() ? 2 : 0;
        this.mob.lookAt(target, 30.0F, 30.0F);
        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (this.state.notEngagedYet()) {
            if (distanceSq > this.approachDistanceSq) {
                this.mob.getNavigation().moveTo(target, speed * this.speedModifierWhenRepositioning);
                return;
            }
            if (!(this.mob.getMainHandItem().getItem() instanceof LancerSpearItem spear)) return;
            this.state.startEngagement(reducedTickDelay(spear.getDamageUseDuration()));
            this.mob.startUsingItem(InteractionHand.MAIN_HAND);
        }
        if (this.state.tickAndCheckEngagement()) {
            this.mob.stopUsingItem();
            double distance = Math.sqrt(distanceSq);
            this.state.awayPos = LandRandomPos.getPosAway(this.mob,
                    Math.max(0, (int) (9 + passengerOffset - distance)),
                    Math.max(1, (int) (11 + passengerOffset - distance)), target.position());
            this.state.fleeingTime = 1;
        }
        if (!this.state.tickAndCheckFleeing()) {
            if (this.state.awayPos != null) {
                this.mob.getNavigation().moveTo(this.state.awayPos.x, this.state.awayPos.y, this.state.awayPos.z,
                        speed * this.speedModifierWhenRepositioning);
                if (this.mob.getNavigation().isDone()) {
                    if (this.state.fleeingTime > 0) {
                        this.state.done = true;
                    } else {
                        this.state.awayPos = null;
                    }
                }
            } else {
                this.mob.getNavigation().moveTo(target, speed * this.speedModifierWhenCharging);
                if (distanceSq < this.targetInRangeRadiusSq || this.mob.getNavigation().isDone()) {
                    double distance = Math.sqrt(distanceSq);
                    this.state.awayPos = LandRandomPos.getPosAway(this.mob,
                            Math.max(0, (int) (6 + passengerOffset - distance)),
                            Math.max(1, (int) (7 + passengerOffset - distance)), target.position());
                }
            }
        }
    }

    public static boolean isLancerSpear(ItemStack stack) {
        return stack.getItem() instanceof LancerSpearItem;
    }

    private static class SpearUseState {
        private int engageTime = -1;
        private int fleeingTime = -1;
        @Nullable
        private Vec3 awayPos;
        private boolean done;

        private boolean notEngagedYet() {
            return this.engageTime < 0;
        }

        private void startEngagement(int ticks) {
            this.engageTime = ticks;
        }

        private boolean tickAndCheckEngagement() {
            if (this.engageTime > 0) {
                this.engageTime--;
                if (this.engageTime == 0) {
                    return true;
                }
            }
            return false;
        }

        private boolean tickAndCheckFleeing() {
            if (this.fleeingTime > 0) {
                this.fleeingTime++;
                if (this.fleeingTime > MAX_FLEEING_TIME) {
                    this.done = true;
                    return true;
                }
            }
            return false;
        }
    }
}
