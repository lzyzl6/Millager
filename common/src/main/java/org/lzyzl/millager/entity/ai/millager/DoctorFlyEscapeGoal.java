package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import org.lzyzl.millager.MillagerSounds;
import org.lzyzl.millager.entity.millager.Doctor;

public class DoctorFlyEscapeGoal extends Goal {

    private final Doctor doctor;

    public DoctorFlyEscapeGoal(Doctor doctor) {
        this.doctor = doctor;
    }

    @Override
    public boolean canUse() {
        return  !this.doctor.hasEffect(MobEffects.LEVITATION) && this.doctor.hurtTime > 0
                && this.doctor.getLastHurtByMob() instanceof LivingEntity living && living.isAlive()
                && this.doctor.getHealth() < this.doctor.getMaxHealth() / 2;
    }

    @Override
    public void start() {
        this.doctor.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 120));
        this.doctor.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 300));
        this.doctor.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20));
        this.doctor.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 50,1));
        this.doctor.playSound(MillagerSounds.DOCTOR_DRINKING_POTION);
        this.doctor.makePoofParticles();
    }
}
