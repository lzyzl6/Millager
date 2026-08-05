package org.lzyzl.millager.client;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import org.lzyzl.millager.client.gui.screens.MillagerConfigScreen;

public final class MillagerForgeConfigScreen {

    private MillagerForgeConfigScreen() {
    }

    public static void register() {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(MillagerConfigScreen::new));
    }
}

