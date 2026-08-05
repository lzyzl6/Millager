package org.lzyzl.millager.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.entity.millager.Breacher;
import org.lzyzl.millager.item.BucklerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class BucklerMixin {
    @Unique
    private static final double LEG_BLOCK_HEIGHT = 0.2D;

    @Inject(method = "isDamageSourceBlocked", at = @At("HEAD"), cancellable = true)
    private void millager$bucklerOnlyBlocksUpperBody(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        ItemStack active = self.getUseItem();
        if (active.isEmpty() || !active.is(MillagerItems.buckler.get())) return;

        if (self.getTicksUsingItem() < BucklerItem.BLOCK_DELAY_TICKS) {
            cir.setReturnValue(false);
            return;
        }
        if (damageSource.getDirectEntity() instanceof Projectile projectile
                && projectile.getY() < self.getY() + self.getBbHeight() * LEG_BLOCK_HEIGHT) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "hurtCurrentlyUsedShield", at = @At("HEAD"), cancellable = true)
    private void millager$bucklerDamageThreshold(float amount, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        ItemStack active = self.getUseItem();
        if (active.isEmpty() || !active.is(MillagerItems.buckler.get())) return;

        if (amount >= BucklerItem.MINIMUM_DURABILITY_DAMAGE) {
            int damage = 1 + (int) Math.floor(amount);
            active.hurtAndBreak(damage, self, entity -> entity.broadcastBreakEvent(self.getUsedItemHand()));
            if (active.isEmpty()) {
                self.stopUsingItem();
                self.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
            }
        }
        if (self instanceof Breacher breacher && amount > 7.0F + self.level().getDifficulty().getId()) {
            breacher.setShieldCooldown(BucklerItem.BLOCK_DISABLE_COOLDOWN);
            breacher.stopUsingItem();
        }
        ci.cancel();
    }

    @Inject(method = "blockedByShield", at = @At("HEAD"), cancellable = true)
    private void millager$bucklerShieldDisable(LivingEntity attacker, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        ItemStack active = self.getUseItem();
        if (active.isEmpty() || !active.is(MillagerItems.buckler.get())) return;

        if (attacker.canDisableShield()) {
            if (self instanceof Breacher breacher) {
                breacher.setShieldCooldown(BucklerItem.BLOCK_DISABLE_COOLDOWN);
                breacher.stopUsingItem();
                ci.cancel();
            }
        }
    }
}
