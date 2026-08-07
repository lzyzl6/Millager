package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.phys.AABB;
import org.lzyzl.millager.MillagerSounds;
import org.lzyzl.millager.entity.millager.Lancer;

import java.util.EnumSet;
import java.util.List;

public class LancerHealAuraGoal extends Goal {
    private static final int COOLDOWN_TICKS = 1240;
    private final Lancer lancer;
    private int castTicks;

    public LancerHealAuraGoal(Lancer lancer) {
        this.lancer = lancer;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.lancer.isHealing()) return true;
        if (this.lancer.hasEffect(MobEffects.REGENERATION)) return false;
        if (this.lancer.level().getGameTime() < this.lancer.getNextHealTime()) return false;
        if (this.lancer.getHealth() > 8.0F && this.lancer.getTarget() != null) return false;
        return this.hasInjuredAllyNearby();
    }

    private boolean hasInjuredAllyNearby() {
        List<LivingEntity> allies = this.lancer.level().getEntitiesOfClass(LivingEntity.class,
                this.lancer.getBoundingBox().inflate(16.0D, 3.0D, 16.0D), entity -> {
                    boolean isAlly = this.lancer.isAlliedTo(entity);
                    boolean isWarHorse = entity instanceof Horse horse
                            && horse.getControllingPassenger() != null
                            && this.lancer.isAlliedTo(horse.getControllingPassenger());
                    return (isAlly || isWarHorse) && entity.getHealth() < entity.getMaxHealth() * 0.8F;
                });
        return !allies.isEmpty();
    }

    @Override
    public boolean canContinueToUse() {
        return this.lancer.getCastTicks() > 0;
    }

    @Override
    public void start() {
        this.castTicks = Math.min(120 - this.lancer.level().getDifficulty().getId() * 20, 100);
        this.lancer.setCastTicks(this.castTicks);
        this.lancer.setHealing(true);
        this.lancer.getNavigation().stop();
        this.lancer.playSound(MillagerSounds.LANCER_CAST_SPELL, 4.0F, 1.0F);
    }

    @Override
    public void stop() {
        this.lancer.setCastTicks(0);
        this.lancer.setHealing(false);
        this.lancer.playSound(MillagerSounds.LANCER_CAST_SPELL, 4.0F, 1.0F);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (!(this.lancer.level() instanceof ServerLevel serverLevel)) return;
        float progress = (float) (this.castTicks - this.lancer.getCastTicks()) / this.castTicks;
        double angle = serverLevel.getGameTime() * 0.2D;
        double radius = 0.5D + progress * 1.2D;
        double height = 0.5D + Math.sin(progress * (Math.PI / 2.0D)) * 2.0D;
        serverLevel.sendParticles(ParticleTypes.ENCHANT, this.lancer.getX() + Math.cos(angle) * radius,
                this.lancer.getY() + height, this.lancer.getZ() + Math.sin(angle) * radius,
                5, 0.1D, 0.1D, 0.1D, 0.0D);
        if (this.lancer.getCastTicks() <= 1) {
            int level = this.releaseHealAura(serverLevel);
            this.lancer.setNextHealTime(serverLevel.getGameTime() + COOLDOWN_TICKS - Math.min(level, 16) * 40L);
        }
    }

    private int releaseHealAura(ServerLevel serverLevel) {
        double centerX = this.lancer.getX();
        double centerY = this.lancer.getY();
        double centerZ = this.lancer.getZ();
        double radius = 10.0D + this.lancer.level().getDifficulty().getId() * 2.0D;
        for (int i = 0; i < 360; i += 5) {
            double angle = Math.toRadians(i);
            serverLevel.sendParticles(ParticleTypes.GLOW, centerX + Math.cos(angle) * radius,
                    centerY + 0.5D, centerZ + Math.sin(angle) * radius,
                    10, 0.1D, 0.1D, 0.1D, 0.1D);
        }
        AABB area = new AABB(centerX - radius, centerY - 16.0D, centerZ - radius,
                centerX + radius, centerY + 16.0D, centerZ + radius);
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, area, this.lancer::isAlliedTo);
        int level = this.lancer.getEffect(MobEffects.REGENERATION) instanceof MobEffectInstance regeneration
                ? regeneration.getAmplifier() + 1 : 1;
        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
                    300 - Math.min(150, level * 50), Math.min(255, level)));
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + 1.5D,
                    target.getZ(), 30, 0.2D, 0.2D, 0.2D, 0.05D);
        }
        return level;
    }
}
