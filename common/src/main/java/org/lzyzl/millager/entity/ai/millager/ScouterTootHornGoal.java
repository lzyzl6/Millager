package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import org.lzyzl.millager.MillagerSounds;
import org.lzyzl.millager.entity.millager.Scouter;

import java.util.List;

public class ScouterTootHornGoal extends Goal {

    private final Scouter scouter;

    public ScouterTootHornGoal(Scouter scouter) {
        this.scouter = scouter;
    }

    @Override
    public boolean canUse() {
        if (this.scouter.isTooting()) return true;
        if (this.scouter.hasTooted()) return false;
        return this.scouter.isPendingRaidToot();
    }

    @Override
    public boolean canContinueToUse() {
        return this.scouter.getActivityTicks() > 0;
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
        this.scouter.getLookControl().setLookAt(
                this.scouter.getX(1),
                this.scouter.getY(1) + 1.0,
                this.scouter.getZ(1)
        );
    }

    @Override
    public void start() {
        this.scouter.setTooting(true);
        this.scouter.startUsingItem(InteractionHand.OFF_HAND);
        this.scouter.setActivityTicks(110);
        this.scouter.playSound(MillagerSounds.REINFORCE_HORN, 16.0F, 0.8F);
    }

    @Override
    public void stop() {
        this.addEffectToAlly(getServerLevel(this.scouter));
        this.scouter.stopUsingItem();
        this.scouter.setTooting(false);
        this.scouter.setTooted(true);
        this.scouter.setPendingRaidToot(false);
        this.scouter.setActivityTicks(0);
    }
    
    private void addEffectToAlly(ServerLevel serverLevel) {
        double centerX = this.scouter.getX();
        double centerY = this.scouter.getY();
        double centerZ = this.scouter.getZ();
        double radius = 32.0D + this.scouter.level().getDifficulty().getId() * 4.0D;

        AABB area = new AABB(centerX - radius, centerY - radius, centerZ - radius,
                centerX + radius, centerY + radius, centerZ + radius);

        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, area, this.scouter::isAlliedTo);

        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 1200,0));
            target.addEffect(new MobEffectInstance(MobEffects.SPEED, 1200,0));
            serverLevel.sendParticles(ParticleTypes.TRIAL_OMEN, target.getX(), target.getY() + 1.5D, target.getZ(), 3, 0.2, 0.2, 0.2, 0.1);
        }
    }

}
