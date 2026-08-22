package org.lzyzl.millager.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.player.Player;
import org.lzyzl.millager.MillagerGameRules;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.entity.millager.Rider;
import org.lzyzl.millager.entity.projectile.RioterProjectile;
import org.lzyzl.millager.util.MillagerTargetingHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class AllyHurtMixin {

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void millager$onHurtServer(ServerLevel serverLevel, DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        Entity sourceEntity = damageSource.getEntity();
        Entity directEntity = damageSource.getDirectEntity();
        LivingEntity self = (LivingEntity) (Object) this;
        if (directEntity instanceof RioterProjectile rioterProjectile && rioterProjectile.isRioterProjectile()
                && !MillagerTargetingHelper.isHostileToMillager(self)
                && !(MillagerTargetingHelper.isFriendlyToMillager(self) && serverLevel.getGameRules().get(MillagerGameRules.FRIENDLY_FIRE.get()))) cir.setReturnValue(false);
        if (serverLevel.getGameRules().get(MillagerGameRules.FRIENDLY_FIRE.get())) return;
        if (sourceEntity instanceof LivingEntity livingSource
                && MillagerTargetingHelper.areFriendly(livingSource, self)) {
            cir.setReturnValue(false);
        }
        if (directEntity instanceof LivingEntity livingDirect
                && MillagerTargetingHelper.areFriendly(livingDirect, self)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "hurtServer", at = @At("TAIL"))
    @SuppressWarnings("ConstantValue")
    private void millager$onHorseHurt(ServerLevel serverLevel, DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || !(damageSource.getEntity() instanceof Player player)) return;
        if (!((Object) this instanceof Horse horse)) return;
        for (Entity passenger : horse.getPassengers()) {
            if (passenger instanceof AbstractMillager millager && millager instanceof Rider) {
                millager.recordPlayerAttack(player, amount);
                return;
            }
        }
    }
}
