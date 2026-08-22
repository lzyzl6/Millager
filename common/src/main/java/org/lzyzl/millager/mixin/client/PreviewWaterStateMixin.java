package org.lzyzl.millager.mixin.client;

import net.minecraft.world.entity.Entity;
import org.lzyzl.millager.client.gui.screens.PreviewEntityLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class PreviewWaterStateMixin {

    @Inject(method = "isInWaterOrBubble", at = @At("HEAD"), cancellable = true)
    private void millager$skipPreviewWaterState(CallbackInfoReturnable<Boolean> cir) {
        if (PreviewEntityLevel.isPreview((Entity) (Object) this)) cir.setReturnValue(false);
    }

}
