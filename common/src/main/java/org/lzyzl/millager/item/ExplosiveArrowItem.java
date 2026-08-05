package org.lzyzl.millager.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.entity.projectile.ExplosiveArrow;

public class ExplosiveArrowItem extends ArrowItem {

    public ExplosiveArrowItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull AbstractArrow createArrow(@NonNull Level level, ItemStack itemStack, @NonNull LivingEntity livingEntity, @Nullable ItemStack itemStack2) {
        return new ExplosiveArrow(level, livingEntity, itemStack.copyWithCount(1), itemStack2);
    }

    @Override
    public @NonNull Projectile asProjectile(@NonNull Level level, Position position, ItemStack itemStack, @NonNull Direction direction) {
        return new ExplosiveArrow(level, position.x(), position.y(), position.z(), itemStack.copyWithCount(1), null);
    }
}
