package org.lzyzl.millager.util;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;

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
        if (!ENABLED.get() || !isHangingEntity(entityTag.read("id", Codec.STRING).orElse(""))
                || !(entityTag.get("block_pos") instanceof IntArrayTag positionTag)) {
            return;
        }
        int[] position = positionTag.getAsIntArray();
        if (position.length != 3 || position[0] == entityBlockPos.getX() && position[1] == entityBlockPos.getY()
                && position[2] == entityBlockPos.getZ()) {
            return;
        }
        entityTag.putIntArray("block_pos", new int[]{entityBlockPos.getX(), entityBlockPos.getY(), entityBlockPos.getZ()});
    }

    private static boolean isHangingEntity(String id) {
        return id.equals("minecraft:item_frame") || id.equals("minecraft:glow_item_frame")
                || id.equals("minecraft:painting");
    }
}
