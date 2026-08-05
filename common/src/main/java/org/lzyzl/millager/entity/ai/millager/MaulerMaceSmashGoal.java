package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.lzyzl.millager.entity.millager.Mauler;

import java.util.EnumSet;
import java.util.List;

public class MaulerMaceSmashGoal extends Goal {
    private final Mauler mauler;
    private LivingEntity target;
    private int attackTick = 0;
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

        double distSqr = this.mauler.distanceToSqr(this.target);
        return distSqr >= 16.0D && distSqr < 144.0D && this.mauler.onGround();
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
            Vec3 vec = this.target.position().subtract(this.mauler.position());
            Vec3 horiz = new Vec3(vec.x, 0, vec.z).normalize().scale(0.8);
            this.mauler.setDeltaMovement(horiz.x, 0.9, horiz.z);
        }
        if (this.hasJumped && !this.hasSmashed && this.attackTick > 15) {
            boolean isFalling = this.mauler.getDeltaMovement().y < 0;
            boolean isHighEnough = this.mauler.fallDistance > 1.5F;
            boolean hitTarget = this.mauler.getBoundingBox().inflate(0.8, 1.2, 0.8).intersects(this.target.getBoundingBox());

            if ((this.mauler.onGround() && isFalling) || (hitTarget && isHighEnough)) {
                this.executeMaceSmash();
                this.hasSmashed = true;
            }
        }
    }

    private void executeMaceSmash() {
        if (!(this.mauler.level() instanceof ServerLevel serverLevel)) return;

        double fallDist = this.mauler.fallDistance;

        AABB smashArea = this.mauler.getBoundingBox().inflate(3.5D);
        List<LivingEntity> victims = serverLevel.getEntitiesOfClass(LivingEntity.class, smashArea,
                e -> {
            if(e instanceof Player player && this.target == player) return true;
            return !(e instanceof Creeper) && e instanceof Enemy && e.isAlive() && !this.mauler.isAlliedTo(e);
        });

        if (!victims.isEmpty()) {
            this.mauler.setDeltaMovement(this.mauler.getDeltaMovement().with(Direction.Axis.Y, 0.01F));

            float smashBonus;
            if (fallDist <= 3.0D) {
                smashBonus = (float)(4.0D * fallDist);
            } else if (fallDist <= 8.0D) {
                smashBonus = (float)(12.0D + 2.0D * (fallDist - 3.0D));
            } else {
                smashBonus = (float)(22.0D + (fallDist - 8.0D));
            }

            float baseDamage = (float)this.mauler.getAttributeValue(Attributes.ATTACK_DAMAGE);
            float finalDamage = baseDamage + smashBonus;

            this.mauler.startUsingItem(InteractionHand.MAIN_HAND);
            this.mauler.swing(this.mauler.getUsedItemHand());
            var sound = fallDist > 5.0D ? SoundEvents.MACE_SMASH_GROUND_HEAVY : SoundEvents.MACE_SMASH_GROUND;
            this.mauler.playSound(sound, 1.5F, 1.0F);

            BlockParticleOption blockParticle = new BlockParticleOption(ParticleTypes.BLOCK, serverLevel.getBlockState(this.mauler.blockPosition().below()).getBlock().defaultBlockState());
            serverLevel.sendParticles(blockParticle, this.mauler.getX(), this.mauler.getY(), this.mauler.getZ(), 200, 3.0, 0.2, 3.0, 0.15);
            int kbScale = fallDist > 5.0D ? 2 : 1;

            for (LivingEntity victim : victims) {
                victim.hurt(this.mauler.damageSources().mobAttack(this.mauler), finalDamage);

                Vec3 delta = victim.position().subtract(this.mauler.position());
                double dist = delta.length();
                double power = (3.5D - dist) * 0.7D * kbScale * (1.0D - victim.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));

                if (power > 0) {
                    Vec3 push = delta.normalize().scale(power);
                    victim.push(push.x, 0.7D, push.z);
                }
            }
        } else {
            this.mauler.playSound(SoundEvents.GENERIC_BIG_FALL, 1.0F, 1.0F);
        }

        this.mauler.resetFallDistance();
    }

}
