package org.lzyzl.millager.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.lzyzl.millager.MillagerGameRules;
import org.lzyzl.millager.entity.projectile.RioterProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.lzyzl.millager.util.MiscHelper.isMillagerFaction;

@Mixin(LivingEntity.class)
public class AllyHurtMixin {

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void millager$onHurtServer(ServerLevel serverLevel, DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        Entity sourceEntity = damageSource.getEntity();
        Entity directEntity = damageSource.getDirectEntity();
        LivingEntity self = (LivingEntity) (Object) this;
        if (serverLevel.getGameRules().get(MillagerGameRules.FRIENDLY_FIRE.get())) return;
        if (
                directEntity instanceof RioterProjectile rioterProjectile &&
                        rioterProjectile.isRioterProjectile() && isMillagerFaction(self)
        ) cir.setReturnValue(false);
        if (isMillagerFaction(self) && isMillagerFaction(sourceEntity)) {
            cir.setReturnValue(false);
        }
        if (isMillagerFaction(self) && isMillagerFaction(directEntity)) {
            cir.setReturnValue(false);
        }
    }
}
