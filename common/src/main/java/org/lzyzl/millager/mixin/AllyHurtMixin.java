package org.lzyzl.millager.mixin;

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

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void millager$onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity sourceEntity = source.getEntity();
        Entity directEntity = source.getDirectEntity();
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().getGameRules().getRule(MillagerGameRules.FRIENDLY_FIRE).get()) return;
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
