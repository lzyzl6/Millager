package org.lzyzl.millager.client.render.entity.horse;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.equine.Markings;
import net.minecraft.world.entity.animal.equine.Variant;

import java.util.Locale;

import static org.lzyzl.millager.Millager.MOD_ID;

public final class MillagerHorseTextures {

    private MillagerHorseTextures() {
    }

    public static Identifier getTexture(Variant variant, Markings markings) {
        return Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/horse/" + variant.name().toLowerCase(Locale.ROOT)
                + "_" + markings.name().toLowerCase(Locale.ROOT) + ".png");
    }
}
