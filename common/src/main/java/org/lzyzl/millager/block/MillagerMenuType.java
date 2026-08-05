package org.lzyzl.millager.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import org.lzyzl.millager.registry.DeferredHolder;
import org.lzyzl.millager.registry.DeferredRegister;
import org.lzyzl.millager.block.menu.TotemInfuserMenu;

import static org.lzyzl.millager.Millager.MOD_ID;

public class MillagerMenuType {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<TotemInfuserMenu>> TOTEM_INFUSER =
            MENU_TYPES.register("totem_infuser",
                    () -> new MenuType<>(TotemInfuserMenu::new, FeatureFlagSet.of()));

    public static void initialize() {
        MENU_TYPES.register();
    }
}
