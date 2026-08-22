package org.lzyzl.millager.mixin.forge;

import net.minecraft.server.MinecraftServer;
import org.lzyzl.millager.compat.goety.GoetyReinforcementController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class GoetyServerTickMixin {

    @Inject(method = "tickServer", at = @At("TAIL"), require = 0)
    private void millager$tickGoetyReinforcements(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        GoetyReinforcementController.tickServer((MinecraftServer) (Object) this);
    }
}
