package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.lzyzl.millager.MillagerSounds;
import org.lzyzl.millager.entity.millager.Mauler;

import java.util.EnumSet;
import java.util.List;

public class MaulerMaceSmashGoal extends Goal {

    private final Mauler mauler;
    private LivingEntity target;
    private int attackTick;
    private boolean hasJumped;
    private boolean hasSmashed;

    public MaulerMaceSmashGoal(Mauler mauler) {
        this.mauler = mauler;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        this.target = this.mauler.getTarget();
        if (this.target == null || !this.target.isAlive()) return false;
        double distance = this.mauler.distanceToSqr(this.target);
        return distance >= 16.0D && distance < 144.0D && this.mauler.onGround();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.hasSmashed && this.attackTick < 100 && this.target != null && this.target.isAlive();
    }

    @Override
    public void start() {
        this.attackTick = 0;
        this.hasJumped = false;
        this.hasSmashed = false;
        this.mauler.setAggressive(true);
    }

    @Override
    public void stop() {
        this.mauler.setAggressive(false);
    }

    @Override
    public void tick() {
        if (this.target == null) return;
        this.attackTick++;
        this.mauler.getLookControl().setLookAt(this.target, 60.0F, 60.0F);
        this.mauler.yBodyRot = this.mauler.yHeadRot;

        if (this.attackTick < 10) {
            this.mauler.getNavigation().moveTo(this.target, 1.0D);
        } else if (this.attackTick == 10 && !this.hasJumped) {
            this.hasJumped = true;
            Vec3 direction = this.target.position().subtract(this.mauler.position());
            Vec3 horizontal = new Vec3(direction.x, 0, direction.z).normalize().scale(0.8);
            this.mauler.setDeltaMovement(horizontal.x, 0.9, horizontal.z);
        }

        if (this.hasJumped && !this.hasSmashed && this.attackTick > 15) {
            boolean falling = this.mauler.getDeltaMovement().y < 0;
            boolean highEnough = this.mauler.fallDistance > 1.5F;
            boolean hitTarget = this.mauler.getBoundingBox().inflate(0.8, 1.2, 0.8).intersects(this.target.getBoundingBox());
            if (this.mauler.onGround() && falling || hitTarget && highEnough) {
                this.executeMaceSmash();
                this.hasSmashed = true;
            }
        }
    }

    private void executeMaceSmash() {
        if (!(this.mauler.level() instanceof ServerLevel serverLevel)) return;
        double fallDistance = this.mauler.fallDistance;
        AABB smashArea = this.mauler.getBoundingBox().inflate(3.5D);
        List<LivingEntity> victims = serverLevel.getEntitiesOfClass(LivingEntity.class, smashArea, entity -> {
            if (entity instanceof Player player && this.target == player) return true;
            return !(entity instanceof Creeper) && entity instanceof Enemy && entity.isAlive() && !this.mauler.isAlliedTo(entity);
        });

        if (!victims.isEmpty()) {
            Vec3 movement = this.mauler.getDeltaMovement();
            this.mauler.setDeltaMovement(movement.x, 0.01F, movement.z);

            float bonusDamage;
            if (fallDistance <= 3.0D) {
                bonusDamage = (float) (4.0D * fallDistance);
            } else if (fallDistance <= 8.0D) {
                bonusDamage = (float) (12.0D + 2.0D * (fallDistance - 3.0D));
            } else {
                bonusDamage = (float) (22.0D + fallDistance - 8.0D);
            }

            float damage = (float) this.mauler.getAttributeValue(Attributes.ATTACK_DAMAGE) + bonusDamage;
            this.mauler.swing(InteractionHand.MAIN_HAND);
            this.mauler.playSound(MillagerSounds.MAULER_SMASH, 1.5F, 1.0F);

            BlockParticleOption particles = new BlockParticleOption(ParticleTypes.BLOCK, serverLevel.getBlockState(this.mauler.blockPosition().below()));
            serverLevel.sendParticles(particles, this.mauler.getX(), this.mauler.getY(), this.mauler.getZ(), 200, 3.0, 0.2, 3.0, 0.15);
            int knockbackScale = fallDistance > 5.0D ? 2 : 1;

            for (LivingEntity victim : victims) {
                victim.hurt(this.mauler.damageSources().mobAttack(this.mauler), damage);
                Vec3 delta = victim.position().subtract(this.mauler.position());
                double distance = delta.length();
                double power = (3.5D - distance) * 0.7D * knockbackScale * (1.0D - victim.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                if (power > 0) {
                    Vec3 push = delta.normalize().scale(power);
                    victim.push(push.x, 0.7D, push.z);
                }
            }
        } else {
            this.mauler.playSound(MillagerSounds.MAULER_SMASH, 1.0F, 1.0F);
        }
        this.mauler.resetFallDistance();
    }
}
