package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.goal.Goal;
import org.lzyzl.millager.MillagerSounds;
import org.lzyzl.millager.entity.millager.Mauler;

public class MaulerFixArmorGoal extends Goal {

    private final Mauler mauler;

    public MaulerFixArmorGoal(Mauler mauler) {
        this.mauler = mauler;
    }

    @Override
    public boolean canUse() {
        if(this.mauler.isFixing()) return true;
        return this.mauler.getTarget() == null && this.mauler.getArmorValue() < 20f && this.mauler.getFixCooldown() <= 0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.mauler.getTarget() == null && this.mauler.getFixingTicks() > 0;
    }

    @Override
    public void start() {
        this.mauler.setFixing(true);
        this.mauler.setFixingTicks(160 + this.mauler.getRandom().nextInt(80));
        this.mauler.getNavigation().stop();
    }

    @Override
    public void tick() {
        int ticks = this.mauler.getFixingTicks();
        if(ticks % 20 == 0 && this.mauler.getRandom().nextBoolean())
            this.mauler.playSound(MillagerSounds.MAULER_FIXING);
    }

    @Override
    public void stop() {
        if(this.mauler.getFixingTicks() > 0) {
            this.mauler.playSound(SoundEvents.VILLAGER_NO);
            if(this.mauler.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.mauler.getX(), this.mauler.getY(), this.mauler.getZ(), 20,
                        0.5, 1, 0.5,
                        0.1);
            }
        } else {
            if(this.mauler.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.WAX_OFF, this.mauler.getX(), this.mauler.getY(), this.mauler.getZ(), 60,
                        0.5, 1, 0.5,
                        0.1);
            }
            this.mauler.playSound(SoundEvents.VILLAGER_YES);
            float num = 2 + this.mauler.getRandom().nextInt(2);
            AttributeInstance armor = this.mauler.getAttribute(Attributes.ARMOR);
            if (armor != null) armor.setBaseValue(Math.min(this.mauler.getArmorValue() + num, 20F));
            this.mauler.setFixCooldown(320 + this.mauler.getRandom().nextInt(160));
        }
        this.mauler.setFixing(false);
        this.mauler.setFixingTicks(0);
    }
}
