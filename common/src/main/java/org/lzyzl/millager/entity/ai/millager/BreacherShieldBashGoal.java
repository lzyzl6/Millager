package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.lzyzl.millager.MillagerSounds;
import org.lzyzl.millager.entity.millager.Breacher;
import org.lzyzl.millager.util.MillagerTargetingHelper;

import java.util.EnumSet;

public class BreacherShieldBashGoal extends Goal {
    private static final double RANGE = 3.0D;
    private static final double FRONT_ARC_DOT = 0.5D;

    private final Breacher breacher;
    private int ticks;
    private boolean struck;

    public BreacherShieldBashGoal(Breacher breacher) {
        this.breacher = breacher;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.breacher.getTarget();
        return this.breacher.canUseShield() && this.breacher.getBashCooldown() <= 0 && target != null && target.isAlive() && this.isSurrounded();
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticks > 0;
    }

    @Override
    public void start() {
        this.breacher.releaseUsingItem();
        this.breacher.setBashing(true);
        this.ticks = 8;
        this.struck = false;
        this.breacher.setBashCooldown(50);
    }

    @Override
    public void tick() {
        if (--this.ticks > 3 || this.struck) {
            return;
        }

        this.struck = true;
        if (!(this.breacher.level() instanceof ServerLevel level)) {
            return;
        }

        LivingEntity target = this.breacher.getTarget();
        if (target != null && target.isAlive()) {
            this.breacher.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        this.breacher.swing(InteractionHand.OFF_HAND);
        AABB bashArea = this.breacher.getBoundingBox().inflate(RANGE, 1.0D, RANGE);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, bashArea, this::isValidVictim)) {
            Vec3 awayFromBreacher = victim.position().subtract(this.breacher.position());
            victim.knockback(this.getKnockbackStrength(), -awayFromBreacher.x, -awayFromBreacher.z);
            victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 0), this.breacher);
        }
        MobEffectInstance strength = this.breacher.getEffect(MobEffects.STRENGTH);
        this.breacher.addEffect(new MobEffectInstance(MobEffects.STRENGTH, strength == null ? 240 : strength.getDuration(), strength == null ? 1 : strength.getAmplifier() + 1), this.breacher);
        level.playSound(null, this.breacher.blockPosition(), MillagerSounds.BREACHER_SHIELD_BASH, SoundSource.HOSTILE, 1.0F, 0.9F);
    }

    @Override
    public void stop() {
        this.breacher.setBashing(false);
        this.ticks = 0;
    }

    private boolean isValidVictim(LivingEntity victim) {
        LivingEntity target = this.breacher.getTarget();
        if (victim instanceof Player player && (player != target || player.isCreative() || player.isSpectator())) {
            return false;
        }
        return victim != this.breacher && victim.isAlive() && !this.breacher.isAlliedTo(victim)
                && ((victim == target && victim instanceof Player) || victim instanceof Enemy && MillagerTargetingHelper.canAttack(this.breacher, victim))
                && this.isInFront(victim);
    }

    private boolean isSurrounded() {
        if (!(this.breacher.level() instanceof ServerLevel level)) return false;
        AABB nearbyArea = this.breacher.getBoundingBox().inflate(RANGE, 1.0D, RANGE);
        return level.getEntitiesOfClass(LivingEntity.class, nearbyArea, this::isValidVictim).size() > 1;
    }

    private double getKnockbackStrength() {
        return 0.7D + this.breacher.level().getDifficulty().getId() * 0.3D;
    }

    private boolean isInFront(LivingEntity target) {
        Vec3 offset = new Vec3(target.getX() - this.breacher.getX(), 0.0D, target.getZ() - this.breacher.getZ());
        if (offset.lengthSqr() > RANGE * RANGE || offset.lengthSqr() < 1.0E-4D) {
            return false;
        }
        Vec3 facing = Vec3.directionFromRotation(0.0F, this.breacher.yBodyRot);
        return facing.dot(offset.normalize()) >= FRONT_ARC_DOT;
    }
}
