package org.lzyzl.millager.mixin;

import net.minecraft.util.Mth;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.item.BucklerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerBucklerMixin {

    @Inject(method = "blockUsingShield", at = @At("HEAD"), cancellable = true)
    private void millager$disableBucklerInsteadOfShield(LivingEntity attacker, CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (!self.getUseItem().is(MillagerItems.buckler.get()) || !attacker.canDisableShield()) return;

        self.knockback(0.5D, attacker.getX() - self.getX(), attacker.getZ() - self.getZ());
        self.getCooldowns().addCooldown(MillagerItems.buckler.get(), BucklerItem.BLOCK_DISABLE_COOLDOWN);
        self.stopUsingItem();
        self.level().broadcastEntityEvent(self, (byte)30);
        ci.cancel();
    }

    @Inject(method = "hurtCurrentlyUsedShield", at = @At("HEAD"), cancellable = true)
    private void millager$damageBuckler(float amount, CallbackInfo ci) {
        Player self = (Player) (Object) this;
        ItemStack active = self.getUseItem();
        if (active.isEmpty() || !active.is(MillagerItems.buckler.get())) return;

        if (!self.level().isClientSide()) {
            self.awardStat(Stats.ITEM_USED.get(active.getItem()));
        }
        if (amount >= BucklerItem.MINIMUM_DURABILITY_DAMAGE) {
            active.hurtAndBreak(1 + Mth.floor(amount), self, LivingEntity.getSlotForHand(self.getUsedItemHand()));
            if (active.isEmpty()) {
                self.stopUsingItem();
            }
        }
        ci.cancel();
    }
}
