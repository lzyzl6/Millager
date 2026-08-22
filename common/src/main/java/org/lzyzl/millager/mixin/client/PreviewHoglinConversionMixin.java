package org.lzyzl.millager.mixin.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import org.lzyzl.millager.client.gui.screens.PreviewEntityLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Hoglin.class)
public abstract class PreviewHoglinConversionMixin {

    @Inject(method = "isConverting", at = @At("HEAD"), cancellable = true)
    private void millager$skipPreviewConversion(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (PreviewEntityLevel.isPreview(entity)) cir.setReturnValue(false);
    }
}
