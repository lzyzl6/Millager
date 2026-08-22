package org.lzyzl.millager.mixin.client;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.lzyzl.millager.client.gui.screens.PreviewEntityLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void millager$hidePreviewLivingName(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (PreviewEntityLevel.isPreview(entity)) cir.setReturnValue(false);
    }

}
