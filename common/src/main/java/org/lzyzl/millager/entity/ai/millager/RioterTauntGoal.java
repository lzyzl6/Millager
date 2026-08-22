package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import org.lzyzl.millager.MillagerSounds;
import org.lzyzl.millager.entity.millager.Rioter;
import org.lzyzl.millager.util.MillagerTargetingHelper;

import java.util.EnumSet;
import java.util.List;

public class RioterTauntGoal extends Goal {

    private final Rioter rioter;

    public RioterTauntGoal(Rioter rioter) {
        this.rioter = rioter;
        this.setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if(this.rioter.isTaunting()) return true;
        if(this.rioter.isThrowing()) return false;
        if(this.rioter.getTauntCooldown() > 0) return false;
        LivingEntity target = this.rioter.getTarget();
        if(target == null || !target.isAlive()) return false;
        return target.distanceToSqr(this.rioter) > 16.0D;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.rioter.getTarget();
        return target != null && target.isAlive() && this.rioter.getActivityTicks() > 0;
    }

    @Override
    public void start() {
        this.rioter.setAggressive(true);
        this.rioter.setTaunting(true);
        this.rioter.setActivityTicks(40 + this.rioter.getRandom().nextInt(40));
        if (this.rioter.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                    this.rioter.getX(), this.rioter.getY() + 1.5D, this.rioter.getZ(), 5, 0.5D, 0.5D, 0.5D, 0.05D);
            this.rioter.playSound(SoundEvents.VILLAGER_NO);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void tick() {
        int ticks = this.rioter.getActivityTicks();
        this.rioter.updateTargetingAndDistance();
        if (this.rioter.level() instanceof ServerLevel serverLevel) {
            if(ticks > 0 && ticks % 16 == 0) {
                serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                        this.rioter.getX(), this.rioter.getY() + 1.5D, this.rioter.getZ(), 5, 0.5D, 0.5D, 0.5D, 0.05D);
                serverLevel.playSound(null, this.rioter, MillagerSounds.RIOTER_TAUNTING, this.rioter.getSoundSource(), 1.0F, 1.0F);

            }
        }
    }

    @Override
    public void stop() {
        this.rioter.setAggressive(false);
        this.rioter.setTaunting(false);
        if (this.rioter.level() instanceof ServerLevel serverLevel) {
            List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, this.rioter.getBoundingBox().inflate(16.0D),
                    e -> e.hasLineOfSight(this.rioter) && e instanceof Enemy && e instanceof Mob && e.isAlive()
                            && MillagerTargetingHelper.canAttack(this.rioter, e));
            if(!targets.isEmpty()) {
                targets.forEach((target) -> {
                    Mob enemy = (Mob) target;
                    LivingEntity enemyTarget = enemy.getTarget();
                    if(enemyTarget == null || MillagerTargetingHelper.isFriendlyToMillager(enemyTarget)) {
                        enemy.setTarget(this.rioter);
                        serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                                enemy.getX(), enemy.getY() + 1.5D, enemy.getZ(), 5, 0.5D, 0.5D, 0.5D, 0.05D);
                    }
                });
            }
        }
        if(this.rioter.getActivityTicks() <= 0) {
            this.rioter.setTauntCooldown(600 + this.rioter.getRandom().nextInt(300));
            this.rioter.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 2));
            this.rioter.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1));
        }
        this.rioter.setActivityTicks(0);
    }
}
