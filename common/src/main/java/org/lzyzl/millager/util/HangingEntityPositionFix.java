package org.lzyzl.millager.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public final class HangingEntityPositionFix {

    private static final ThreadLocal<Boolean> ENABLED = ThreadLocal.withInitial(() -> false);

    private HangingEntityPositionFix() {
    }

    public static void begin() {
        ENABLED.set(true);
    }

    public static void end() {
        ENABLED.remove();
    }

    public static void update(CompoundTag entityTag, BlockPos entityBlockPos) {
        if (!ENABLED.get() || !isHangingEntity(entityTag.getString("id"))
                || !entityTag.contains("TileX", Tag.TAG_INT) || !entityTag.contains("TileY", Tag.TAG_INT)
                || !entityTag.contains("TileZ", Tag.TAG_INT)) {
            return;
        }
        if (entityTag.getInt("TileX") == entityBlockPos.getX() && entityTag.getInt("TileY") == entityBlockPos.getY()
                && entityTag.getInt("TileZ") == entityBlockPos.getZ()) {
            return;
        }
        entityTag.putInt("TileX", entityBlockPos.getX());
        entityTag.putInt("TileY", entityBlockPos.getY());
        entityTag.putInt("TileZ", entityBlockPos.getZ());
    }

    private static boolean isHangingEntity(String id) {
        return id.equals("minecraft:item_frame") || id.equals("minecraft:glow_item_frame")
                || id.equals("minecraft:painting");
    }
}
