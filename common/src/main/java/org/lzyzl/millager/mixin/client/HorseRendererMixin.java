package org.lzyzl.millager.mixin.client;

import net.minecraft.client.renderer.entity.HorseRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Markings;
import org.lzyzl.millager.client.render.entity.horse.MillagerHorseTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HorseRenderer.class)
public class HorseRendererMixin {

    @Inject(method = "getTextureLocation(Lnet/minecraft/world/entity/animal/horse/Horse;)Lnet/minecraft/resources/ResourceLocation;", at = @At("RETURN"), cancellable = true)
    private void millager$useCombinedTexture(Horse horse, CallbackInfoReturnable<ResourceLocation> cir) {
        if (!horse.isBaby() && horse.getTags().contains("millager_mount") && horse.getMarkings() != Markings.NONE) {
            cir.setReturnValue(MillagerHorseTextures.getTexture(horse));
        }
    }
}
