package org.lzyzl.millager.block;

import com.mojang.serialization.MapCodec;
import org.jspecify.annotations.NonNull;

public class VillagerWallHeadBlock extends AbstractWallHeadBlock {

    public static final MapCodec<VillagerWallHeadBlock> CODEC = simpleCodec(VillagerWallHeadBlock::new);

    public @NonNull MapCodec<VillagerWallHeadBlock> codec() {
        return CODEC;
    }

    public VillagerWallHeadBlock(Properties properties) {
        super(AbstractHeadBlock.MillagerTypes.VILLAGER, properties);
    }

}
