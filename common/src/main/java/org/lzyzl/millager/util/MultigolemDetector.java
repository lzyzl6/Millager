package org.lzyzl.millager.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.animal.IronGolem;

import java.util.Map;
import java.util.WeakHashMap;

public final class MultigolemDetector {

    private static final Map<IronGolem, Boolean> ZOMBIE_CACHE = new WeakHashMap<>();

    public static boolean isZombieGolem(IronGolem golem) {
        return ZOMBIE_CACHE.computeIfAbsent(golem, MultigolemDetector::checkNbt);
    }

    private static boolean checkNbt(IronGolem golem) {
        try {
            CompoundTag tag = new CompoundTag();
            golem.saveWithoutId(tag);
            return isZombieVariant(tag);
        } catch (Exception e) {
            return false;
        }
    }

    static boolean isZombieVariant(CompoundTag entityTag) {
        for (String root : new String[]{"fabric:attachments", "neoforge:attachments"}) {
            for (String key : new String[]{"multigolem:identity", "multigolem:variant"}) {
                if (readVariant(entityTag, root, key).equals("zombie")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String readVariant(CompoundTag entityTag, String rootKey, String attachmentKey) {
        if (!entityTag.contains(rootKey)) return "";
        CompoundTag root = entityTag.getCompound(rootKey);
        if (!root.contains(attachmentKey)) return "";

        return extractVariant(root.get(attachmentKey));
    }

    private static String extractVariant(Tag tag) {
        if (tag instanceof CompoundTag ct) {
            if (ct.contains("value")) {
                return extractVariant(ct.get("value"));
            }
            if (ct.contains("variant")) {
                return ct.getString("variant");
            }
        }
        if (tag instanceof StringTag stringTag) {
            return stringTag.getAsString();
        }
        return "";
    }
}
