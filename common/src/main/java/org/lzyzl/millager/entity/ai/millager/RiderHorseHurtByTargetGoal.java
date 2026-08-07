package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.entity.millager.Rider;

import java.util.EnumSet;

public class RiderHorseHurtByTargetGoal<T extends AbstractMillager & Rider> extends HurtByTargetGoal {

    private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
    private @Nullable Horse horse;
    private final T rider;
    private final Class<?>[] toIgnoreDamage;
    private int timestamp;

    public RiderHorseHurtByTargetGoal(T rider, Class<?>... classes) {
        super(rider, classes);
        this.rider = rider;
        this.toIgnoreDamage = classes;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!(this.mob.getControlledVehicle() instanceof Horse hse)) return false;
        this.horse = hse;
        if (!this.horse.isAlive()) return false;
        int i = this.horse.getLastHurtByMobTimestamp();
        LivingEntity livingEntity = this.horse.getLastHurtByMob();
        if (i != this.timestamp && livingEntity != null) {
            if (livingEntity instanceof Player player) {
                if (!this.rider.shouldRetaliateAgainst(player)) return false;
                this.targetMob = player;
                return true;
            }
            if (livingEntity.getType() == EntityTypes.PLAYER && getServerLevel(this.mob).getGameRules().get(GameRules.UNIVERSAL_ANGER)) {
                return false;
            } else {
                for(Class<?> class_ : this.toIgnoreDamage) {
                    if (class_.isAssignableFrom(livingEntity.getClass())) {
                        return false;
                    }
                }
                return this.canAttack(livingEntity, HURT_BY_TARGETING);
            }
        } else {
            return false;
        }
    }

    @Override
    public void start() {
        super.start();
        if(this.horse == null) return;
        this.mob.setTarget(this.horse.getLastHurtByMob());
        this.targetMob = this.mob.getTarget();
        this.timestamp = this.horse.getLastHurtByMobTimestamp();
    }
}
