package org.lzyzl.millager.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HorseMarkingLayer;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import org.lzyzl.millager.client.render.entity.horse.MillagerHorseRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseMarkingLayer.class)
public class HorseMarkingLayerMixin {

    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HorseRenderState;FF)V", at = @At("HEAD"), cancellable = true)
    private void millager$skipCombinedMarkings(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, HorseRenderState state, float limbSwing, float limbSwingAmount, CallbackInfo ci) {
        if (((MillagerHorseRenderState) state).millager$usesCombinedTexture()) {
            ci.cancel();
        }
    }
}
