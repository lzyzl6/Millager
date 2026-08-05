package org.lzyzl.millager.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.lzyzl.millager.entity.millager.Swordmaster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class SwordmasterDeathProtectionMixin {

    @Inject(method = "checkTotemDeathProtection", at = @At("HEAD"), cancellable = true)
    private void millager$checkSwordmasterDeathProtection(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;

        InteractionHand hand = InteractionHand.MAIN_HAND;
        ItemStack protectionItem = self.getMainHandItem();
        CompoundTag tag = protectionItem.getTag();
        if (tag == null || !tag.contains("MillagerDeathProtection")) {
            hand = InteractionHand.OFF_HAND;
            protectionItem = self.getOffhandItem();
            tag = protectionItem.getTag();
        }
        boolean swordmasterProtection = self instanceof Swordmaster
                && tag != null && tag.contains("SwordmasterDeathProtection");
        boolean infusedProtection = tag != null && tag.contains("MillagerDeathProtection");
        if (!swordmasterProtection && !infusedProtection) return;

        protectionItem.shrink(1);
        self.setItemSlot(hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND, protectionItem);
        self.setHealth(1.0F);
        self.removeAllEffects();
        self.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
        self.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
        self.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
        self.level().broadcastEntityEvent(self, (byte) 35);
        cir.setReturnValue(true);
    }
}
