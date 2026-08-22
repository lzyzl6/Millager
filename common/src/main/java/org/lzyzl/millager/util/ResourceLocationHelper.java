package org.lzyzl.millager.util;

import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("removal")
public final class ResourceLocationHelper {

    public static ResourceLocation create(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    public static ResourceLocation create(String path) {
        return new ResourceLocation(path);
    }
}
