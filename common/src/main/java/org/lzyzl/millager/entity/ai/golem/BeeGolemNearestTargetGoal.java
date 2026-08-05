package org.lzyzl.millager.entity.ai.golem;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.entity.golem.BeeGolem;

public class BeeGolemNearestTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {

    private final BeeGolem beeGolem;

    public BeeGolemNearestTargetGoal(BeeGolem golem, Class<T> class_, int i ,boolean bl, boolean bl2, TargetingConditions.@Nullable Selector selector) {
        super(golem,class_, i, bl, bl2, selector);
        this.beeGolem = golem;
    }

    @Override
    public boolean canUse() {
        if(beeGolem.isAttacking()) return false;
        return super.canUse();
    }
}
