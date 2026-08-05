package org.lzyzl.millager.mixin.client;

import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import org.lzyzl.millager.MillagerItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Inject(method = "aiStep", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/Input;forwardImpulse:F", opcode = 181, shift = At.Shift.AFTER))
    private void millager$restoreBucklerMovementSpeed(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        if (!self.isUsingItem() || !self.getUseItem().is(MillagerItems.buckler.get())) return;

        Input input = self.input;
        input.leftImpulse *= 5.0F;
        input.forwardImpulse *= 5.0F;
    }
}
