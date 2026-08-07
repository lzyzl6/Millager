package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import org.lzyzl.millager.entity.millager.AbstractMillager;

public class MillagerHurtByTargetGoal extends HurtByTargetGoal {

    private final AbstractMillager millager;

    public MillagerHurtByTargetGoal(AbstractMillager millager, Class<?>... classes) {
        super(millager, classes);
        this.millager = millager;
    }

    @Override
    public boolean canUse() {
        LivingEntity attacker = this.mob.getLastHurtByMob();
        if (attacker instanceof Player player) {
            if (!this.millager.shouldRetaliateAgainst(player)) return false;
            this.targetMob = attacker;
            return true;
        }
        return super.canUse();
    }
}
