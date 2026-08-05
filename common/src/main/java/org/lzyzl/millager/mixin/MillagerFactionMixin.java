package org.lzyzl.millager.mixin;

import net.minecraft.world.entity.Entity;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.lzyzl.millager.util.MiscHelper.isMillagerFaction;

@Mixin(Entity.class)
public abstract class MillagerFactionMixin {

    @Inject(method = "isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void millager$onIsAlliedTo(Entity other, CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof AbstractMillager millager && millager.isProfessionOrderOwner(other)) {
            cir.setReturnValue(true);
        }
        if (isMillagerFaction(self) && isMillagerFaction(other)) {
            cir.setReturnValue(true);
        }
    }
}
