package org.lzyzl.millager.block;

import com.mojang.serialization.MapCodec;
import org.jspecify.annotations.NonNull;

public class VillagerHeadBlock extends AbstractHeadBlock{
    public static final MapCodec<VillagerHeadBlock> CODEC = simpleCodec(VillagerHeadBlock::new);

    public @NonNull MapCodec<VillagerHeadBlock> codec() {
        return CODEC;
    }

    public VillagerHeadBlock(Properties properties) {
        super(MillagerTypes.VILLAGER, properties);
    }
}
