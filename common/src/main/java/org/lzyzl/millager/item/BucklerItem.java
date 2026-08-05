package org.lzyzl.millager.item;

import net.minecraft.world.item.ShieldItem;

public class BucklerItem extends ShieldItem {

    public static final int BLOCK_DISABLE_COOLDOWN = 60;
    public static final int BLOCK_DELAY_TICKS = 3;
    public static final float MINIMUM_DURABILITY_DAMAGE = 2.5F;

    public BucklerItem(Properties properties) {
        super(properties);
    }

}
