package org.lzyzl.millager.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.util.MillagerTargetingHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MillagerFactionMixin {

    @Inject(method = "isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void millager$onIsAlliedTo(Entity other, CallbackInfoReturnable<Boolean> cir) {
        if (MillagerTargetingHelper.isCheckingExternalAlliance()) return;
        Entity self = (Entity) (Object) this;
        if (self instanceof AbstractMillager millager && millager.isProfessionOrderOwner(other)) {
            cir.setReturnValue(true);
        }
        if (self instanceof LivingEntity livingSelf && other instanceof LivingEntity livingOther
                && MillagerTargetingHelper.areFriendly(livingSelf, livingOther)) {
            cir.setReturnValue(true);
        }
    }
}
