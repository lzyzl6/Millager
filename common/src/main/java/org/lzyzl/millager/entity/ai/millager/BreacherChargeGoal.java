package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.Horse;
import org.lzyzl.millager.entity.millager.Breacher;

import java.util.EnumSet;
import java.util.UUID;

public class BreacherChargeGoal extends Goal {

    private static final UUID CHARGE_SPEED_ID = UUID.fromString("3075456e-bb74-47e2-9aef-c26e76431f6a");

    private final Breacher breacher;
    private Horse horse;

    public BreacherChargeGoal(Breacher breacher) {
        this.breacher = breacher;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.breacher.getTarget();
        return this.shouldUseShield(target);
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.breacher.getTarget();
        return this.shouldUseShield(target);
    }

    @Override
    public void start() {
        this.breacher.setCharging(true);
        this.breacher.releaseUsingItem();
        if (this.breacher.getVehicle() instanceof Horse mount && mount.isAlive()) {
            this.horse = mount;
            AttributeInstance speed = mount.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) {
                speed.removeModifier(CHARGE_SPEED_ID);
                speed.addTransientModifier(new AttributeModifier(CHARGE_SPEED_ID, "millager:breacher_charge_speed",
                        0.2D, AttributeModifier.Operation.MULTIPLY_BASE));
            }
        }
        this.breacher.startUsingItem(InteractionHand.OFF_HAND);
    }

    @Override
    public void tick() {
        LivingEntity target = this.breacher.getTarget();
        if (target != null) {
            this.breacher.getNavigation().moveTo(target, this.horse == null ? 1.0D : 1.2D);
            this.breacher.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
    }

    @Override
    public void stop() {
        if (this.horse != null) {
            AttributeInstance speed = this.horse.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) speed.removeModifier(CHARGE_SPEED_ID);
        }
        this.breacher.getNavigation().stop();
        this.horse = null;
        this.breacher.setCharging(false);
        this.breacher.releaseUsingItem();
    }

    private boolean shouldUseShield(LivingEntity target) {
        return target != null && target.isAlive() && this.breacher.canUseShield()
                && (!this.breacher.isWithinMeleeAttackRange(target) || this.breacher.getAxeCooldown() > 0);
    }
}
