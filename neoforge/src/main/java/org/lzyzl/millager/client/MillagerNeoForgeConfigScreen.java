package org.lzyzl.millager.client;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.lzyzl.millager.client.gui.screens.MillagerConfigScreen;

public final class MillagerNeoForgeConfigScreen {

    private MillagerNeoForgeConfigScreen() {
    }

    public static void register(ModContainer modContainer) {
        IConfigScreenFactory configScreenFactory = (container, parent) -> new MillagerConfigScreen(parent);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, configScreenFactory);
    }
}

