package org.lzyzl.millager.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lzyzl.millager.util.MiscHelper.isMillagerFaction;

@Mixin(Mob.class)
public abstract class FactionTargetMixin {

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void millager$onSetTarget(@Nullable LivingEntity target, CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (isMillagerFaction(self) && isMillagerFaction(target)) {
            ci.cancel();
        }
    }
}
