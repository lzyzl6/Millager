package org.lzyzl.millager.entity.ai.golem;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import org.lzyzl.millager.entity.golem.BeeGolem;

public class BeeGolemWanderGoal extends Goal {

    private final BeeGolem golem;

    public BeeGolemWanderGoal(BeeGolem g) {
        this.golem = g;
    }

    @Override
    public boolean canUse() {
        return !golem.isAttacking() && golem.getTarget() == null && golem.getLifeTicks() < 2300 && golem.getNavigation().isDone();
    }

    @Override
    public void start() {
        RandomSource r = golem.getRandom();

        double x = golem.getX() + (r.nextDouble() - 0.5) * 10;
        double y = golem.getY() + (r.nextDouble() - 0.5) * 6;
        double z = golem.getZ() + (r.nextDouble() - 0.5) * 10;

        golem.getNavigation().moveTo(x, y, z, 0.6);
    }

    @Override
    public boolean canContinueToUse() {
        return !golem.getNavigation().isDone();
    }
}
