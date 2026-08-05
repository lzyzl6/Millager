package org.lzyzl.millager.mixin.client;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.lzyzl.millager.entity.golem.BeeGolem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lzyzl.millager.Millager.MOD_ID;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "checkEntityPostEffect", at = @At("HEAD"), cancellable = true)
    private void millager$checkEntityPostEffect(Entity entity, CallbackInfo ci) {
        if(entity instanceof BeeGolem) {
            ((GameRendererInvoker) this).invokeSetPostEffect(Identifier.fromNamespaceAndPath(MOD_ID, "drone_vision"));
            ci.cancel();
        }
    }
}