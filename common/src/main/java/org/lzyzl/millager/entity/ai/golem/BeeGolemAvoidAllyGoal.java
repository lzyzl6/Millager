package org.lzyzl.millager.entity.ai.golem;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import org.lzyzl.millager.entity.golem.BeeGolem;

import java.util.List;

public class BeeGolemAvoidAllyGoal extends TargetGoal {

    private final BeeGolem golem;

    public BeeGolemAvoidAllyGoal(BeeGolem golem, boolean bl) {
        super(golem, bl,true);
        this.golem = golem;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.golem.getTarget();
        if (target == null) return false;
        for (LivingEntity entity : this.golem.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(6.0D))) {
            if (this.golem.isAlliedTo(entity) && target.distanceTo(entity) < 1.5D) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        LivingEntity currentTarget = this.golem.getTarget();
        List<LivingEntity> monsters = this.golem.level().getEntitiesOfClass(LivingEntity.class, this.golem.getBoundingBox().inflate(20.0D),
                e ->  this.golem.hasLineOfSight(e) && e instanceof Enemy && !(e instanceof Creeper) && e.isAlive() && !this.golem.isAlliedTo(e) && this.golem.canAttack(e));
        LivingEntity nextTarget = null;
        double minDist = Double.MAX_VALUE;
        for (LivingEntity monster : monsters) {
            if (monster != currentTarget && monster.isAlive()) {
                boolean safe = true;
                for (LivingEntity entity : this.golem.level().getEntitiesOfClass(LivingEntity.class, monster.getBoundingBox().inflate(5.0D))) {
                    if (this.golem.isAlliedTo(entity) && monster.distanceTo(entity) < 1.5D) {
                        safe = false;
                        break;
                    }
                }
                if (safe) {
                    double dist = this.golem.distanceTo(monster);
                    if (dist < minDist) {
                        minDist = dist;
                        nextTarget = monster;
                    }
                }
            }
        }
        if (nextTarget != null) {
            this.golem.setTarget(nextTarget);
        }
    }

    @Override
    public void tick() {

        for (LivingEntity entity : this.golem.level().getEntitiesOfClass(LivingEntity.class, this.golem.getBoundingBox().inflate(10.0D))) {
            if (this.golem.isAlliedTo(entity) && this.golem.distanceTo(entity) < 5.0D) {
                Vec3 pushVec = this.golem.position().subtract(entity.position()).normalize().scale(0.1D);
                this.golem.setDeltaMovement(this.golem.getDeltaMovement().add(pushVec));
            }
        }
    }
}
