package org.lzyzl.millager.mixin.client;

import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import org.lzyzl.millager.client.gui.screens.PreviewEntityLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZombieVillager.class)
public abstract class PreviewZombieVillagerMixin {

    @Inject(method = "initializeVillagerData", at = @At("HEAD"), cancellable = true)
    private void millager$useDefaultPreviewVillagerData(CallbackInfoReturnable<VillagerData> cir) {
        ZombieVillager zombieVillager = (ZombieVillager) (Object) this;
        if (PreviewEntityLevel.isPreview(zombieVillager)) cir.setReturnValue(Villager.createDefaultVillagerData());
    }

}
