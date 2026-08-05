package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import org.lzyzl.millager.entity.millager.Scholar;

import java.util.EnumSet;

import static org.lzyzl.millager.Millager.MOD_ID;

public class ScholarReadBookGoal extends Goal {
    private final Scholar scholar;
    private int readTimer;
    private final int MAX_READ_TIME;

    private static final Identifier SLOW_READING_ID = Identifier.fromNamespaceAndPath(MOD_ID, "reading_slowness");

    public ScholarReadBookGoal(Scholar scholar, int durationInTicks) {
        this.scholar = scholar;
        this.MAX_READ_TIME = durationInTicks;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.scholar.getTarget() != null) return false;
        if (this.scholar.hurtTime > 0) return false;
        return this.scholar.getRandom().nextInt(10) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.scholar.hurtTime > 0) return false;
        return this.readTimer < MAX_READ_TIME;
    }

    @Override
    public void start() {
        this.readTimer = 0;
        this.scholar.setReading(true);
        AttributeInstance speedAttr = this.scholar.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(SLOW_READING_ID);
            speedAttr.addTransientModifier(new AttributeModifier(
                    SLOW_READING_ID, 
                    -0.5,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            ));
        }
    }

    @Override
    public void tick() {
        this.readTimer++;
        this.scholar.getLookControl().setLookAt(
            this.scholar.getX(), 
            this.scholar.getY() + this.scholar.getEyeHeight() - 0.5, 
            this.scholar.getZ()
        );
    }

    @Override
    public void stop() {
        this.scholar.setReading(false);
        AttributeInstance speedAttr = this.scholar.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(SLOW_READING_ID);
        }
        if (this.readTimer < MAX_READ_TIME) {
            if (this.scholar.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                        this.scholar.getX(),
                        this.scholar.getEyeY() + 0.3,
                        this.scholar.getZ(),
                        3,
                        0.2, 0.2, 0.2,
                        0.0
                );
            }
        }
    }
}