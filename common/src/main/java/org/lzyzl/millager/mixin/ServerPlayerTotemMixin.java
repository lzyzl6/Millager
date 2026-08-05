package org.lzyzl.millager.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.advancement.MillagerCriteria;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerTotemMixin {

    @Unique private int millager$totemCheckTimer = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void millager$checkTotems(CallbackInfo ci) {
        if (++millager$totemCheckTimer < 20) return;
        millager$totemCheckTimer = 0;
        ServerPlayer self = (ServerPlayer) (Object) this;
        Inventory inventory = self.getInventory();
        int totems = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(MillagerItems.totemOfHealing.asItem())
                    || stack.is(MillagerItems.crackedTotemOfHealing.asItem())
                    || stack.is(MillagerItems.damagedTotemOfHealing.asItem())) {
                totems += stack.getCount();
            }
        }
        if (totems > 0) MillagerCriteria.HOLD_TOTEMS.get().trigger(self, totems);
    }
}
