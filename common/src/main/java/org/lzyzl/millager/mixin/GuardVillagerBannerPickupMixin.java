package org.lzyzl.millager.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import org.lzyzl.millager.util.VillageBannerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class GuardVillagerBannerPickupMixin {

    @Inject(method = "wantsToPickUp", at = @At("HEAD"), cancellable = true)
    private void millager$wantsVillageBanner(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        Mob guard = (Mob) (Object) this;
        if (millager$isGuardVillager(guard) && itemStack.getItem() instanceof BannerItem) cir.setReturnValue(millager$canEquipVillageBanner(guard, itemStack));
    }

    @Inject(method = "pickUpItem", at = @At("HEAD"), cancellable = true)
    private void millager$pickUpVillageBanner(ItemEntity itemEntity, CallbackInfo ci) {
        Mob guard = (Mob) (Object) this;
        ItemStack itemStack = itemEntity.getItem();
        if (millager$isGuardVillager(guard) && itemStack.getItem() instanceof BannerItem) {
            if (millager$canEquipVillageBanner(guard, itemStack)) {
                guard.onItemPickup(itemEntity);
                ItemStack banner = itemStack.copy();
                banner.setCount(1);
                guard.setItemSlot(EquipmentSlot.HEAD, banner);
                itemStack.shrink(1);
                if (itemStack.isEmpty()) itemEntity.discard();
            }
            ci.cancel();
        }
    }

    @Unique
    private static boolean millager$isGuardVillager(Mob mob) {
        return "guardvillagers:guard".equals(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString());
    }

    @Unique
    private static boolean millager$canEquipVillageBanner(Mob mob, ItemStack itemStack) {
        return VillageBannerHelper.isVillageBanner(itemStack)
                && mob.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
    }
}
