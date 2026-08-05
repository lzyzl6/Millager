package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.lzyzl.millager.entity.millager.Archer;

import java.util.EnumSet;

public class ArcherBowAttackGoal<T extends Archer & RangedAttackMob> extends Goal {
    private final T archer;
    private final double speedModifier;
    private final int attackIntervalMin;
    private final float attackRadiusSqr;

    private int attackTime = 0;
    private int strafeTick = 0;

    public ArcherBowAttackGoal(T archer, double speed, int interval, float radius) {
        this.archer = archer;
        this.speedModifier = speed;
        this.attackIntervalMin = interval;
        this.attackRadiusSqr = radius * radius;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if(this.archer.isAggressive()) return true;
        LivingEntity t = this.archer.getTarget();
        return t != null && t.isAlive() && !t.isRemoved();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity t = this.archer.getTarget();
        return t != null && t.isAlive() && !t.isRemoved();
    }

    @Override
    public void stop() {
        this.archer.stopUsingItem();
        this.archer.setAggressive(false);
        this.archer.setTarget(null);
        this.attackTime = 0;
    }

    @Override
    public void start() {
        this.archer.setAggressive(true);
    }

    @Override
    public void tick() {
        LivingEntity target = this.archer.getTarget();
        if (target == null || !target.isAlive() || target.isRemoved()) return;


        double distSqr = this.archer.distanceToSqr(target);
        boolean inRange = distSqr <= this.attackRadiusSqr;
        boolean hasLOS = this.archer.hasLineOfSight(target);

        double speed = 3.0;
        double predictionTicks = (Math.sqrt(distSqr) / speed) + 3.0;
        Vec3 movement = target.getDeltaMovement();

        double predX = target.getX() + movement.x * predictionTicks;
        double predZ = target.getZ() + movement.z * predictionTicks;
        double predY = target.getEyeY() + (target.onGround() ? 0 : movement.y * predictionTicks);

        this.archer.getLookControl().setLookAt(predX, predY, predZ, 100.0F, 100.0F);

        double ideal = this.attackRadiusSqr * 0.7;
        double tolerance = this.attackRadiusSqr * 0.1;

        if (inRange && hasLOS) {
            this.archer.getNavigation().stop();
            float forward = 0.0F;
            if (distSqr < ideal - tolerance) forward = -0.4F;
            else if (distSqr > ideal + tolerance) forward = 0.4F;

            float sideways = ((this.strafeTick / 20) % 2 == 0) ? 0.4F : -0.4F;
            this.strafeTick++;
            this.archer.getMoveControl().strafe(forward, sideways);
        } else {
            this.archer.getNavigation().moveTo(target, this.speedModifier);
        }

        if (this.archer.isUsingItem()) {
            if (this.archer.getTicksUsingItem() >= 20) {
                this.archer.stopUsingItem();
                this.archer.performRangedAttack(target, 1.0F);
                this.attackTime = this.attackIntervalMin;
            }
        } else if (--this.attackTime <= 0 && inRange && hasLOS) {
            double dx = predX - this.archer.getX();
            double dz = predZ - this.archer.getZ();
            float yaw = (float)(Math.atan2(dz, dx) * (180F / Math.PI)) - 90F;

            this.archer.setYRot(yaw);
            this.archer.yBodyRot = yaw;
            this.archer.yHeadRot = yaw;

            this.archer.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.archer, Items.BOW));
        }
    }

}