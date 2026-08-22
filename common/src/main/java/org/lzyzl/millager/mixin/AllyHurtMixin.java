package org.lzyzl.millager.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
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

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void millager$onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity sourceEntity = source.getEntity();
        Entity directEntity = source.getDirectEntity();
        LivingEntity self = (LivingEntity) (Object) this;
        if (directEntity instanceof RioterProjectile rioterProjectile && rioterProjectile.isRioterProjectile()
                && !MillagerTargetingHelper.isHostileToMillager(self)
                && !(MillagerTargetingHelper.isFriendlyToMillager(self) && self.level().getGameRules().getRule(MillagerGameRules.FRIENDLY_FIRE).get())) cir.setReturnValue(false);
        if (self.level().getGameRules().getRule(MillagerGameRules.FRIENDLY_FIRE).get()) return;
        if (sourceEntity instanceof LivingEntity livingSource
                && MillagerTargetingHelper.areFriendly(livingSource, self)) {
            cir.setReturnValue(false);
        }
        if (directEntity instanceof LivingEntity livingDirect
                && MillagerTargetingHelper.areFriendly(livingDirect, self)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "hurt", at = @At("TAIL"))
    @SuppressWarnings("ConstantValue")
    private void millager$onHorseHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || !(source.getEntity() instanceof Player player)) return;
        if (!((Object) this instanceof Horse horse)) return;
        for (Entity passenger : horse.getPassengers()) {
            if (passenger instanceof AbstractMillager millager && millager instanceof Rider) {
                millager.recordPlayerAttack(player, amount);
                return;
            }
        }
    }
}
