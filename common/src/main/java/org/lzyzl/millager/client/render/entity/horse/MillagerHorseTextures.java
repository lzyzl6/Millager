package org.lzyzl.millager.client.render.entity.horse;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;

import java.util.Locale;

import static org.lzyzl.millager.Millager.MOD_ID;

public final class MillagerHorseTextures {

    private MillagerHorseTextures() {
    }

    public static ResourceLocation getTexture(Horse horse) {
        return new ResourceLocation(MOD_ID, "textures/entity/horse/" + horse.getVariant().name().toLowerCase(Locale.ROOT)
                + "_" + horse.getMarkings().name().toLowerCase(Locale.ROOT) + ".png");
    }
}
