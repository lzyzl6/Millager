package org.lzyzl.millager.block;

import com.mojang.serialization.MapCodec;
import org.jspecify.annotations.NonNull;

public class IllagerWallHeadBlock extends AbstractWallHeadBlock {

    public static final MapCodec<IllagerWallHeadBlock> CODEC = simpleCodec(IllagerWallHeadBlock::new);

    public @NonNull MapCodec<IllagerWallHeadBlock> codec() {
        return CODEC;
    }

    public IllagerWallHeadBlock(Properties properties) {
        super(AbstractHeadBlock.MillagerTypes.ILLAGER, properties);
    }

}
