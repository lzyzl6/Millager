package org.lzyzl.millager.util;

import net.minecraft.resources.ResourceLocation;

public final class ResourceLocationHelper {

    @SuppressWarnings("removal")
    public static ResourceLocation create(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    @SuppressWarnings("removal")
    public static ResourceLocation create(String path) {
        return new ResourceLocation(path);
    }
}
