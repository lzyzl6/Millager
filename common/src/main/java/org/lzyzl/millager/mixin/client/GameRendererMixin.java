package org.lzyzl.millager.mixin.client;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.lzyzl.millager.Millager;
import org.lzyzl.millager.entity.golem.BeeGolem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "checkEntityPostEffect", at = @At("HEAD"), cancellable = true)
    private void millager$checkEntityPostEffect(Entity entity, CallbackInfo ci) {
        if (!(entity instanceof BeeGolem)) {
            return;
        }

        ((GameRendererInvoker) this).invokeLoadEffect(
                ResourceLocation.fromNamespaceAndPath(Millager.MOD_ID, "shaders/post/drone_vision.json"));
        ci.cancel();
    }
}
