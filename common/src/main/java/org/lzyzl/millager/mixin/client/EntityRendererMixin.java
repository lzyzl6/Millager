package org.lzyzl.millager.mixin.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.lzyzl.millager.client.gui.screens.PreviewEntityLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(method = "getPackedLightCoords", at = @At("HEAD"), cancellable = true)
    private void millager$fullBrightPreview(T entity, float partialTick, CallbackInfoReturnable<Integer> cir) {
        if (PreviewEntityLevel.isPreview(entity)) cir.setReturnValue(0xF000F0);
    }

}
