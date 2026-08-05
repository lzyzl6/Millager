package org.lzyzl.millager.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lzyzl.millager.MillagerItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZombieVillager.class)
public class ZombieHealMixin {

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void millager$onMobInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ZombieVillager self = (ZombieVillager) (Object) this;
        ItemStack itemStack = player.getItemInHand(hand);
        if (!self.isConverting() && itemStack.is(MillagerItems.elixir.asItem())) {
            player.swing(hand);
            if (!player.getAbilities().instabuild) itemStack.shrink(1);
            if (!self.level().isClientSide()) {
                self.startConverting(player.getUUID(), 200);
            }
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
