package org.lzyzl.millager.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import org.lzyzl.millager.client.gui.screens.MillagerConfigScreen;

public final class MillagerModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return MillagerConfigScreen::new;
    }
}

