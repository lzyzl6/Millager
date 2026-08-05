package org.lzyzl.millager.util;

import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.entity.projectile.ExplosiveArrow;
import org.lzyzl.millager.entity.projectile.MolotovCocktail;
import org.lzyzl.millager.entity.projectile.MolotovCocktailPlus;

public class DispenserHelper {

    public static void registerDispenserProjectile() {
        DispenserBlock.registerBehavior(MillagerItems.explosiveArrow, new AbstractProjectileDispenseBehavior() {
            @Override
            protected @NonNull Projectile getProjectile(@NonNull Level level, @NonNull Position position, @NonNull ItemStack itemStack) {
                ExplosiveArrow projectile = new ExplosiveArrow(level, position.x(), position.y(), position.z(), itemStack.copyWithCount(1));
                projectile.pickup = AbstractArrow.Pickup.ALLOWED;
                return projectile;
            }
        });
        DispenserBlock.registerBehavior(MillagerItems.molotovCocktail, new AbstractProjectileDispenseBehavior() {
            @Override
            protected @NonNull Projectile getProjectile(@NonNull Level level, @NonNull Position position, @NonNull ItemStack itemStack) {
                return new MolotovCocktail(level, position.x(), position.y(), position.z(), itemStack.copyWithCount(1));
            }
        });
        DispenserBlock.registerBehavior(MillagerItems.molotovCocktailPlus, new AbstractProjectileDispenseBehavior() {
            @Override
            protected @NonNull Projectile getProjectile(@NonNull Level level, @NonNull Position position, @NonNull ItemStack itemStack) {
                return new MolotovCocktailPlus(level, position.x(), position.y(), position.z(), itemStack.copyWithCount(1));
            }
        });
    }
}
