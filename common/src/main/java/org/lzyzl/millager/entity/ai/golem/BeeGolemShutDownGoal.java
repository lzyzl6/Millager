package org.lzyzl.millager.entity.ai.golem;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.MillagerSounds;
import org.lzyzl.millager.entity.golem.BeeGolem;

public class BeeGolemShutDownGoal extends Goal {
    private final BeeGolem beeGolem;

    public BeeGolemShutDownGoal(BeeGolem beeGolem) {
        this.beeGolem = beeGolem;
    }

    @Override
    public boolean canUse() {
        return beeGolem.getLifeTicks() >= 2300;
    }

    @Override
    public void tick() {
        if(beeGolem.level() instanceof ServerLevel level) {
            if (beeGolem.getLifeTicks() >= 2400) {

                ItemStack amber = new ItemStack(MillagerItems.golemAmber.get());
                ItemEntity item = new ItemEntity(beeGolem.level(), beeGolem.getX(), beeGolem.getY(), beeGolem.getZ(), amber);
                item.setGlowingTag(true);
                level.addFreshEntity(item);
                level.sendParticles(ParticleTypes.GLOW,
                        beeGolem.getX() , beeGolem.getY(), beeGolem.getZ() ,
                        20,
                        0.5, 0.5, 0.5,
                        0.1);
                level.sendParticles(ParticleTypes.WAX_OFF,
                        beeGolem.getX() , beeGolem.getY(), beeGolem.getZ() ,
                        50,
                        0.5, 0.5, 0.5,
                        0.1);
                level.playSound(null, beeGolem.getX(), beeGolem.getY(), beeGolem.getZ(), MillagerSounds.BEE_GOLEM_GLASS_BREAK, SoundSource.NEUTRAL, 2.0F, 0.5f);
                level.playSound(null, beeGolem.getX(), beeGolem.getY(), beeGolem.getZ(), MillagerSounds.BEE_GOLEM_WAX_BREAK, SoundSource.NEUTRAL, 2.0F, 0.5f);
                beeGolem.discard();
                return;
            }
            double tremble = 0.05 * (beeGolem.getRandom().nextDouble() - 0.5);
            double tremble_ = 0.05 * (beeGolem.getRandom().nextDouble() + 0.5);

            beeGolem.setDeltaMovement(Vec3.ZERO);
            level.sendParticles(new DustParticleOptions(new Vector3f(0.0F, 0.52F, 0.74F), 0.8F),
                    beeGolem.getX() + tremble, beeGolem.getY() + tremble + 0.2, beeGolem.getZ() + tremble, 1, 0, 0, 0, 0.01);
            level.sendParticles(new DustParticleOptions(new Vector3f(0.34F, 0.32F, 0.1F), 0.8F),
                    beeGolem.getX() + tremble_, beeGolem.getY() + tremble_ + 0.2, beeGolem.getZ() + tremble_, 1, 0, 0, 0, 0.01);
        }
    }
}
