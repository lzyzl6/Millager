package org.lzyzl.millager.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.lzyzl.millager.entity.golem.BeeGolem;
import org.lzyzl.millager.util.MillagerTargetingHelper;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class FactionTargetMixin {

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void millager$onSetTarget(@Nullable LivingEntity target, CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (self instanceof BeeGolem && target != null && MillagerTargetingHelper.hasBeeGolemOverride(target)) return;
        if (target != null && MillagerTargetingHelper.areFriendly(self, target)) {
            ci.cancel();
        }
    }
}
