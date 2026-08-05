package org.lzyzl.millager.entity.ai.golem;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.IronGolem;
import org.lzyzl.millager.entity.millager.Doctor;
import org.lzyzl.millager.entity.golem.IronGolemAccessor;

import java.util.EnumSet;
import java.util.List;

public class IronGolemFollowDoctorGoal extends Goal {

    private final IronGolem golem;
    private Doctor owner;
    private int timeToRecalcPath;

    private final double speedModifier;
    private final float stopDistance;  // 停止移动的距离（保持间距）
    private final float startDistance; // 开始跟随的最小距离

    public IronGolemFollowDoctorGoal(IronGolem golem, double speedModifier, float startDistance, float stopDistance) {
        this.golem = golem;
        this.speedModifier = speedModifier;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if(!((IronGolemAccessor)golem).millager$isDoctorCreated()) return false;

        if (this.golem.getTarget() != null) {
            return false;
        }

        List<Doctor> list = this.golem.level().getEntitiesOfClass(
                Doctor.class,
                this.golem.getBoundingBox().inflate(16.0D)
        );

        if (list.isEmpty()) {
            return false;
        }

        for (Doctor doctor : list) {

            if (!doctor.isInvisible()) {
                double distSq = this.golem.distanceToSqr(doctor);
                if (distSq >= (double)(startDistance * startDistance)) {
                    this.owner = doctor;
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.golem.getTarget() != null || this.owner == null || !this.owner.isAlive()) {
            return false;
        }

        double distSq = this.golem.distanceToSqr(this.owner);
        return distSq >= (double)(stopDistance * stopDistance) && distSq <= 256.0D;
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        this.owner = null;
        this.golem.getNavigation().stop();
    }

    @Override
    public void tick() {

        if(this.owner.getTarget() != null && this.owner.getTarget().isAlive())
            this.golem.getLookControl().setLookAt(this.owner.getTarget(), 10.0F, (float)this.golem.getMaxHeadXRot());
        else if(this.golem.getRandom().nextInt(10) == 0)
            this.golem.getLookControl().setLookAt(this.owner, 10.0F, (float)this.golem.getMaxHeadXRot());

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;

            double distSq = this.golem.distanceToSqr(this.owner);

            if (distSq > (double)(stopDistance * stopDistance)) {
                this.golem.getNavigation().moveTo(this.owner, this.speedModifier);
            } else {
                this.golem.getNavigation().stop();
            }
        }
    }
}
