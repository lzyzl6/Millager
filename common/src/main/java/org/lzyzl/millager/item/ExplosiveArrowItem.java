package org.lzyzl.millager.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.entity.projectile.ExplosiveArrow;

public class ExplosiveArrowItem extends ArrowItem {

    public ExplosiveArrowItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull AbstractArrow createArrow(@NonNull Level level, ItemStack itemStack, @NonNull LivingEntity livingEntity) {
        return new ExplosiveArrow(level, livingEntity, itemStack.copyWithCount(1));
    }
}
