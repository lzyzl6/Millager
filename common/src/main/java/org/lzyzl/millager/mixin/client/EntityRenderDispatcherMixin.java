package org.lzyzl.millager.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import org.lzyzl.millager.client.gui.screens.PreviewEntityLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(method = "distanceToSqr(Lnet/minecraft/world/entity/Entity;)D", at = @At("HEAD"), cancellable = true)
    private void millager$previewDistance(Entity entity, CallbackInfoReturnable<Double> cir) {
        if (PreviewEntityLevel.isPreview(entity)) cir.setReturnValue(0.0D);
    }

    @Inject(method = "renderShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/Entity;FFLnet/minecraft/world/level/LevelReader;F)V", at = @At("HEAD"), cancellable = true)
    private static void millager$skipPreviewShadow(PoseStack poseStack, MultiBufferSource bufferSource, Entity entity,
                                                    float x, float y, LevelReader level, float radius, CallbackInfo ci) {
        if (PreviewEntityLevel.isPreview(entity)) ci.cancel();
    }
}
