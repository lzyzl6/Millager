package org.lzyzl.millager.mixin.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;
import org.lzyzl.millager.client.gui.screens.PreviewEntityLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class PreviewTeamMixin {

    @Inject(method = "getTeam", at = @At("HEAD"), cancellable = true)
    private void millager$skipPreviewTeam(CallbackInfoReturnable<PlayerTeam> cir) {
        if (PreviewEntityLevel.isPreview((Entity) (Object) this)) cir.setReturnValue(null);
    }

}
