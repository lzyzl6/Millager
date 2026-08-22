package org.lzyzl.millager.mixin.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.lzyzl.millager.client.gui.screens.PreviewEntityLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {

    @Inject(method = "getPackedLightCoords", at = @At("HEAD"), cancellable = true)
    private void millager$fullBrightPreview(T entity, float partialTick, CallbackInfoReturnable<Integer> cir) {
        if (PreviewEntityLevel.isPreview(entity)) cir.setReturnValue(0xF000F0);
    }

    @Inject(method = "finalizeRenderState", at = @At("HEAD"), cancellable = true)
    private void millager$skipPreviewShadow(T entity, S state, CallbackInfo ci) {
        if (PreviewEntityLevel.isPreview(entity)) ci.cancel();
    }

}
