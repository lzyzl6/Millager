package org.lzyzl.millager.mixin.client;

import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.villager.Villager;
import org.lzyzl.millager.client.gui.screens.PreviewEntityLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class PreviewVillagerMixin {

    @Inject(method = "registerBrainGoals", at = @At("HEAD"), cancellable = true)
    private void millager$skipPreviewBrainGoals(Brain<Villager> brain, CallbackInfo ci) {
        Villager villager = (Villager) (Object) this;
        if (PreviewEntityLevel.isPreview(villager)) ci.cancel();
    }

}
