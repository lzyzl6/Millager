package org.lzyzl.millager.util;

import net.minecraft.world.level.block.DispenserBlock;
import org.lzyzl.millager.MillagerItems;

public class DispenserHelper {

    public static void registerDispenserProjectile() {
        DispenserBlock.registerProjectileBehavior(MillagerItems.explosiveArrow);
        DispenserBlock.registerProjectileBehavior(MillagerItems.molotovCocktail);
        DispenserBlock.registerProjectileBehavior(MillagerItems.molotovCocktailPlus);
    }
}
