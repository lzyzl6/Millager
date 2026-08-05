package org.lzyzl.millager.entity.ai.golem;

import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.lzyzl.millager.entity.golem.BeeGolem;

import java.util.Optional;

public class BeeGolemAttackGoal extends Goal {

    private final BeeGolem golem;
    private LivingEntity target;
    private int pathUpdateDelay;

    public BeeGolemAttackGoal(BeeGolem g) {
        this.golem = g;
    }

    @Override
    public boolean canUse() {
        LivingEntity potentialTarget = golem.getTarget();
        if (potentialTarget == null || !potentialTarget.isAlive()) return false;
        if (potentialTarget instanceof Player player && player.getStringUUID().equals(golem.getOwnerUUID())) {
            return false;
        }
        return golem.getLifeTicks() < 2300;
    }

    @Override
    public void start() {
        super.start();
        golem.setAggressive(true);
        golem.setAttacking(true);
        this.target = golem.getTarget();
        this.pathUpdateDelay = 0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null && !this.target.isUnderWater() && this.target.isAlive() && golem.getLifeTicks() < 2300;
    }

    @Override
    public void stop() {
        super.stop();
        golem.setAttacking(false);
        golem.setAggressive(false);
        golem.getNavigation().stop();
        this.target = null;
    }

    @Override
    public void tick() {
        if (target == null || !target.isAlive()) return;

        golem.getLookControl().setLookAt(target, 30F, 30F);

        double widthSqr = target.getBbWidth() * target.getBbWidth();
        double explodeDist = Math.max(widthSqr * 1.21, 4.0);

        if (golem.distanceToSqr(target) < explodeDist) {
            explode(golem);
        } else {
            if (--this.pathUpdateDelay <= 0) {
                this.pathUpdateDelay = 10;
                golem.getNavigation().moveTo(target, 1.6);
            }
        }
    }

    private void explode(BeeGolem beeGolem) {
        if (!beeGolem.level().isClientSide() && !beeGolem.isRemoved()) {
            beeGolem.level().explode(beeGolem,
                    beeGolem.damageSources().explosion(beeGolem.getOwner(), beeGolem),
                    null,
                    beeGolem.getX(), beeGolem.getY(), beeGolem.getZ(),
                    beeGolem.isSummoned() ? 1.5F : 2.5F, false,
                    beeGolem.isSummoned() ? Level.ExplosionInteraction.NONE : Level.ExplosionInteraction.MOB);
            beeGolem.discard();

        }
    }
}