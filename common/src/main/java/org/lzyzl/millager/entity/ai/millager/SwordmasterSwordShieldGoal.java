package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.Projectile;
import org.lzyzl.millager.MillagerSounds;
import org.lzyzl.millager.entity.millager.Swordmaster;

import java.util.EnumSet;

public class SwordmasterSwordShieldGoal extends Goal {

    private final Swordmaster swordmaster;
    private final int[] blockThresholds = {15, 12, 9};
    private boolean finished = false;

    private int pathRecalcTimer = 0;
    private static final int PATH_RECALC_INTERVAL = 20; // 1 秒

    private int stuckTicks = 0;
    private static final int MAX_STUCK_TICKS = 60; // 3 秒

    public SwordmasterSwordShieldGoal(Swordmaster swordmaster) {
        this.swordmaster = swordmaster;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.swordmaster.getShieldedCooldown() > 0) return false;
        if(this.swordmaster.isShielding()) return true;
        LivingEntity target = this.swordmaster.getTarget();
        if (target == null || !target.isAlive() || target.isUnderWater()) return false;
        return !this.swordmaster.isWithinMeleeAttackRange(target);
    }

    @Override
    public boolean canContinueToUse() {
        if(this.finished) return false;
        if (this.swordmaster.getShieldedTimes() >= 3) return false;

        LivingEntity target = this.swordmaster.getTarget();
        if (target == null || !target.isAlive()) return false;
        return !this.swordmaster.isWithinMeleeAttackRange(target);
    }

    @Override
    public void start() {
        this.swordmaster.setShielding(true);
        this.finished = false;
        this.pathRecalcTimer = 0;
        this.stuckTicks = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = this.swordmaster.getTarget();
        if (target == null) return;

        this.swordmaster.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (!this.swordmaster.isShielding()) {
            this.swordmaster.setShielding(true);
        }

        if (this.swordmaster.getNavigation().isStuck()) {
            this.stuckTicks++;
            if (this.stuckTicks >= MAX_STUCK_TICKS) {
                this.finished = true;
                return;
            }
        } else {
            this.stuckTicks = 0;
        }

        this.pathRecalcTimer++;
        if (this.swordmaster.getNavigation().isDone() || this.pathRecalcTimer >= PATH_RECALC_INTERVAL) {
            this.pathRecalcTimer = 0;
            this.swordmaster.getNavigation().moveTo(target, 0.95d);
        }
    }

    @Override
    public void stop() {
        this.swordmaster.setShielding(false);
        this.swordmaster.setShieldedTimes(0);
    }

    public boolean attemptBlock(DamageSource source, float amount) {
        if (!this.swordmaster.isShielding()) return false;

        if (source.getDirectEntity() instanceof Projectile || source.getEntity() instanceof LivingEntity) {
            int currentTimes = this.swordmaster.getShieldedTimes();

            if (currentTimes < 3 && amount <= blockThresholds[currentTimes]) {
                int nextTimes = currentTimes + 1;
                this.swordmaster.setShieldedTimes(nextTimes);

                this.swordmaster.level().playSound(null, this.swordmaster.getX(), this.swordmaster.getY(), this.swordmaster.getZ(),
                        MillagerSounds.SWORD_SHIELD_BLOCK, SoundSource.NEUTRAL, 1.0F, 0.8F + this.swordmaster.getRandom().nextFloat() * 0.4F);

                if (source.getDirectEntity() instanceof Projectile projectile) {
                    projectile.setDeltaMovement(projectile.getDeltaMovement().scale(-0.2D));
                }

                if (nextTimes >= 3) {
                    this.swordmaster.setShieldedCooldown(200);
                    this.swordmaster.setShielding(false);
                    this.finished = true;
                }
                return true;
            } else {
                this.breakSwordShield();
                this.finished = true;
                return false;
            }
        }
        return false;
    }

    private void breakSwordShield() {
        this.swordmaster.level().playSound(null, this.swordmaster.getX(), this.swordmaster.getY(), this.swordmaster.getZ(),
                SoundEvents.SHIELD_BREAK, SoundSource.NEUTRAL, 1.0F, 0.8F + this.swordmaster.getRandom().nextFloat() * 0.4F);
        this.swordmaster.setShieldedCooldown(500);
        this.finished = true;
        this.swordmaster.setShielding(false);
        this.swordmaster.getNavigation().stop();
    }
}
