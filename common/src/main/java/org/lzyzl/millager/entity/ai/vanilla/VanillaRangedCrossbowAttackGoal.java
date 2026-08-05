package org.lzyzl.millager.entity.ai.vanilla;

import net.minecraft.core.component.DataComponents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import org.lzyzl.millager.entity.millager.Scouter;

import java.util.EnumSet;

public class VanillaRangedCrossbowAttackGoal<T extends Scouter & CrossbowAttackMob> extends Goal {
    public static final UniformInt PATHFINDING_DELAY_RANGE = TimeUtil.rangeOfSeconds(1, 2);
    private final T scouter;
    private CrossbowState crossbowState;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private int seeTime;
    private int attackDelay;
    private int updatePathDelay;

    public VanillaRangedCrossbowAttackGoal(T scouter, double speedModifier, float attackRadius) {
        this.crossbowState = CrossbowState.UNCHARGED;
        this.scouter = scouter;
        this.speedModifier = speedModifier;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public boolean canUse() {
        if(this.scouter.isTooting()) return false;
        return this.isValidTarget() && this.isHoldingCrossbow();
    }

    private boolean isHoldingCrossbow() {
        return this.scouter.isHolding(Items.CROSSBOW);
    }

    public boolean canContinueToUse() {
        return this.isValidTarget() && (this.canUse() || !this.scouter.getNavigation().isDone()) && this.isHoldingCrossbow();
    }

    private boolean isValidTarget() {
        return this.scouter.getTarget() != null && this.scouter.getTarget().isAlive();
    }

    public void stop() {
        super.stop();
        this.scouter.setAggressive(false);
        this.scouter.setTarget(null);
        this.seeTime = 0;
        if (this.scouter.isUsingItem()) {
            this.scouter.stopUsingItem();
            this.scouter.setChargingCrossbow(false);
            this.scouter.getUseItem().set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        }
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void tick() {
        LivingEntity target = this.scouter.getTarget();
        if (target != null) {
            boolean hasLineOfSight = this.scouter.getSensing().hasLineOfSight(target);
            boolean wasVisible = this.seeTime > 0;

            if (hasLineOfSight != wasVisible) {
                this.seeTime = 0;
            }

            if (hasLineOfSight) {
                ++this.seeTime;
            } else {
                --this.seeTime;
            }

            double distToTargetSqr = this.scouter.distanceToSqr(target);
            boolean shouldMove = (distToTargetSqr > (double)this.attackRadiusSqr || this.seeTime < 5) && this.attackDelay == 0;

            if (shouldMove) {
                --this.updatePathDelay;
                if (this.updatePathDelay <= 0) {
                    this.scouter.getNavigation().moveTo(target, this.canRun() ? this.speedModifier : this.speedModifier * (double)0.5F);
                    this.updatePathDelay = PATHFINDING_DELAY_RANGE.sample(this.scouter.getRandom());
                }
            } else {
                this.updatePathDelay = 0;
                this.scouter.getNavigation().stop();
            }

            this.scouter.faceTowardsTarget(target);

            if (this.crossbowState == CrossbowState.UNCHARGED) {
                if (!shouldMove) {
                    this.scouter.startUsingItem(InteractionHand.MAIN_HAND);
                    this.crossbowState = CrossbowState.CHARGING;
                    this.scouter.setChargingCrossbow(true);
                }
            } else if (this.crossbowState == CrossbowState.CHARGING) {
                if (!this.scouter.isUsingItem()) {
                    this.crossbowState = CrossbowState.UNCHARGED;
                }

                int useTicks = this.scouter.getTicksUsingItem();
                ItemStack currentItem = this.scouter.getUseItem();
                if (useTicks >= CrossbowItem.getChargeDuration(currentItem, this.scouter)) {
                    this.scouter.releaseUsingItem();
                    this.crossbowState = CrossbowState.CHARGED;
                    this.attackDelay = 20 + this.scouter.getRandom().nextInt(20);
                    this.scouter.setChargingCrossbow(false);
                }
            } else if (this.crossbowState == CrossbowState.CHARGED) {
                --this.attackDelay;
                if (this.attackDelay == 0) {
                    this.crossbowState = CrossbowState.READY_TO_ATTACK;
                }
            } else if (this.crossbowState == CrossbowState.READY_TO_ATTACK && hasLineOfSight) {
                this.scouter.performRangedAttack(target, 1.0F);
                this.crossbowState = CrossbowState.UNCHARGED;
            }
        }
    }

    private boolean canRun() {
        return this.crossbowState == CrossbowState.UNCHARGED;
    }

    enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK
    }
}