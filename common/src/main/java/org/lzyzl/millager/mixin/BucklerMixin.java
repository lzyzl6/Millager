package org.lzyzl.millager.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.lzyzl.millager.MillagerItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class BucklerMixin {
    @Unique
    private static final double LEG_BLOCK_HEIGHT = 1.0D / 3.0D;

    @Inject(method = "applyItemBlocking", at = @At("HEAD"), cancellable = true)
    private void millager$doNotBlockLowProjectiles(ServerLevel level, DamageSource source, float damage,
                                                   CallbackInfoReturnable<Float> callback) {
        LivingEntity user = (LivingEntity) (Object) this;
        ItemStack activeItem = user.getUseItem();
        if (activeItem.is(MillagerItems.buckler.asItem())
                && source.getDirectEntity() instanceof Projectile projectile
                && projectile.getY() < user.getY() + user.getBbHeight() * LEG_BLOCK_HEIGHT) {
            callback.setReturnValue(0.0F);
        }
    }
}

