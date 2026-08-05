package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import org.lzyzl.millager.MillagerEntityTypes;
import org.lzyzl.millager.MillagerSounds;
import org.lzyzl.millager.entity.golem.BeeGolem;
import org.lzyzl.millager.entity.millager.Doctor;

import java.util.EnumSet;

public class DoctorBeeSummonGoal extends Goal {

    private final Doctor doctor;
    private int spawnedNum;
    private int cooldown;

    public DoctorBeeSummonGoal(Doctor doctor) {
        this.doctor = doctor;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if(this.doctor.getBeeSpawnCooldown() > 0) return false;
        if(this.doctor.isSummoning()) return true;
        return this.doctor.getTarget() != null && this.doctor.getTarget().isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        int MAX_SPAWN_TIMES = 3;
        return this.spawnedNum < MAX_SPAWN_TIMES && this.doctor.getTarget() != null && this.doctor.getTarget().isAlive();
    }

    @Override
    public void start() {
        this.spawnedNum = 0;
        spawnBeeGolem();
        this.doctor.setAggressive(true);
        this.doctor.setSummoning(true);
        this.doctor.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.spawnedNum = 0;
        this.cooldown = 0;
        this.doctor.setSummoning(false);
        this.doctor.setAggressive(false);
        this.doctor.setBeeSpawnCooldown(100);
    }

    @Override
    public void tick() {
        if(this.doctor.getTarget() == null || !this.doctor.getTarget().isAlive()) return;
        this.doctor.getLookControl().setLookAt(this.doctor.getTarget(), 30.0F, 30.0F);
        if(this.cooldown <= 0) {
            spawnBeeGolem();
            this.spawnedNum++;
        }
        this.cooldown--;
    }

    private void spawnBeeGolem() {
        if(this.doctor.level() instanceof ServerLevel level) {
            BeeGolem golem = MillagerEntityTypes.Bee_Golem.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (golem != null) {
                Vec3 vec3 = this.doctor.getHeadLookAngle();
                golem.setSummoned(true);
                golem.setPos(this.doctor.getX() - vec3.x, this.doctor.getEyeY(), this.doctor.getZ() - vec3.z);
                level.addFreshEntity(golem);
                level.sendParticles(ParticleTypes.ENCHANTED_HIT,
                        golem.getX() , golem.getY(), golem.getZ() ,
                        20,
                        0.5, 0.5, 0.5,
                        0.1);
                golem.playSound(MillagerSounds.BEE_GOLEM_BURST_OUT, 1.0F, 1.0F);
                if(this.doctor.getTarget()!=null && this.doctor.getTarget().isAlive()) {
                    golem.setTarget(this.doctor.getTarget());
                }
            }
        }
        this.cooldown= 20 + this.doctor.getRandom().nextInt(31);
    }
}