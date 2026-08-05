package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.MillagerSounds;
import org.lzyzl.millager.entity.millager.Lancer;

import java.util.EnumSet;
import java.util.List;

public class LancerHealAuraGoal extends Goal {

    private final Lancer lancer;

    private static final int COOLDOWN_TICKS = 1240;

    private int castTicks;

    public LancerHealAuraGoal(Lancer lancer) {
        this.lancer = lancer;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if(this.lancer.isHealing()) return true;
        if(this.lancer.hasEffect(MobEffects.REGENERATION)) return false;
        if (this.lancer.level().getGameTime() < this.lancer.getNextHealTime()) {
            return false;
        }
        boolean isCrisis = this.lancer.getHealth() <= 8.0F;
        boolean noTarget = this.lancer.getTarget() == null;

        if (isCrisis || noTarget) {
            return this.hasInjuredAllyNearby();
        }
        return false;
    }

    private boolean hasInjuredAllyNearby() {
        List<LivingEntity> allies = this.lancer.level().getEntitiesOfClass(LivingEntity.class,
                this.lancer.getBoundingBox().inflate(16.0D, 3.0D, 16.0D),
                entity -> {
                    boolean isAlly = this.lancer.isAlliedTo(entity);
                    boolean isWarHorse = entity instanceof Horse horse &&
                            horse.getControllingPassenger() != null &&
                            this.lancer.isAlliedTo(horse.getControllingPassenger());

                    return (isAlly || isWarHorse) && entity.getHealth() < entity.getMaxHealth() * 0.8;
                }
        );
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
        this.lancer.playSound(MillagerSounds.LANCER_CAST_SPELL,4,1);
    }

    @Override
    public void stop() {
        this.lancer.setCastTicks(0);
        this.lancer.setHealing(false);
        this.lancer.playSound(MillagerSounds.LANCER_CAST_SPELL,4,1);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (!(this.lancer.level() instanceof ServerLevel serverLevel)) return;

        Result result = getResult(serverLevel);

        serverLevel.sendParticles(ParticleTypes.ENCHANT,
                this.lancer.getX() + result.offsetX(),
                this.lancer.getY() + result.currentHeight(),
                this.lancer.getZ() + result.offsetZ(),
                5, 0.1, 0.1, 0.1, 0.0);


        if (this.lancer.getCastTicks() <= 1) {
            int cooldownReduceAmplifier = Math.min(this.releaseHealAura(serverLevel), 16);
            this.lancer.setNextHealTime(this.lancer.level().getGameTime() + COOLDOWN_TICKS - cooldownReduceAmplifier * 40L);
        }
    }

    private @NonNull Result getResult(ServerLevel serverLevel) {
        long gameTime = serverLevel.getGameTime();
        float progress = (float) (this.castTicks - this.lancer.getCastTicks()) / this.castTicks;

        double rotationSpeed = 0.2;
        double angle = gameTime * rotationSpeed;
        double radius = 0.5 + progress * 1.2;

        double startHeight = 0.5;
        double maxHeightGain = 2.0;

        double currentHeight = startHeight + Math.sin(progress * (Math.PI / 2.0)) * maxHeightGain;

        double offsetX = Math.cos(angle) * radius;
        double offsetZ = Math.sin(angle) * radius;
        return new Result(currentHeight, offsetX, offsetZ);
    }

    private record Result(double currentHeight, double offsetX, double offsetZ) {
    }

    private int releaseHealAura(ServerLevel serverLevel) {
        double centerX = this.lancer.getX();
        double centerY = this.lancer.getY();
        double centerZ = this.lancer.getZ();
        double radius = 10.0D + this.lancer.level().getDifficulty().getId() * 2.0D;

        for (int i = 0; i < 360; i += 5) {
            double angle = Math.toRadians(i);
            double x = centerX + Math.cos(angle) * radius;
            double z = centerZ + Math.sin(angle) * radius;
            serverLevel.sendParticles(ParticleTypes.GLOW, x, centerY + 0.5D, z, 10, 0.1, 0.1, 0.1, 0.1);
        }

        AABB area = new AABB(centerX - radius, centerY - 16, centerZ - radius,
                centerX + radius, centerY + 16, centerZ + radius);

        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, area, this.lancer::isAlliedTo);
        int level = this.lancer.getEffect(MobEffects.REGENERATION) instanceof MobEffectInstance regeneration ? regeneration.getAmplifier() + 1 : 1;

        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300 - Math.min(150, level * 50), Math.min(255, level)));
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + 1.5D, target.getZ(), 30, 0.2, 0.2, 0.2, 0.05);
        }
        return level;
    }
}