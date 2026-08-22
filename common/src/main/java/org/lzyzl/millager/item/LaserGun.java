package org.lzyzl.millager.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import org.jspecify.annotations.NonNull;

public class LaserGun extends Item {

    public LaserGun(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull ItemUseAnimation getUseAnimation(@NonNull ItemStack itemStack) {
        return ItemUseAnimation.SPYGLASS;
    }

}
