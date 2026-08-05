package org.lzyzl.millager.mixin.client;

import net.minecraft.client.renderer.entity.HorseRenderer;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Markings;
import org.lzyzl.millager.client.render.entity.horse.MillagerHorseRenderState;
import org.lzyzl.millager.client.render.entity.horse.MillagerHorseTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HorseRenderer.class)
public class HorseRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/animal/equine/Horse;Lnet/minecraft/client/renderer/entity/state/HorseRenderState;F)V", at = @At("TAIL"))
    private void millager$markCombinedTexture(Horse horse, HorseRenderState state, float tickDelta, CallbackInfo ci) {
        ((MillagerHorseRenderState) state).millager$setUsesCombinedTexture(!horse.isBaby() && horse.getTags().contains("millager_mount") && horse.getMarkings() != Markings.NONE);
    }

    @Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/HorseRenderState;)Lnet/minecraft/resources/Identifier;", at = @At("RETURN"), cancellable = true)
    private void millager$useCombinedTexture(HorseRenderState state, CallbackInfoReturnable<Identifier> cir) {
        if (((MillagerHorseRenderState) state).millager$usesCombinedTexture()) {
            cir.setReturnValue(MillagerHorseTextures.getTexture(state.variant, state.markings));
        }
    }
}
