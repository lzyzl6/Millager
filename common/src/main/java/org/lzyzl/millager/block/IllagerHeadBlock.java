package org.lzyzl.millager.block;

import com.mojang.serialization.MapCodec;
import org.jspecify.annotations.NonNull;

public class IllagerHeadBlock extends AbstractHeadBlock {

    public static final MapCodec<IllagerHeadBlock> CODEC = simpleCodec(IllagerHeadBlock::new);

    public @NonNull MapCodec<IllagerHeadBlock> codec() {
        return CODEC;
    }

    public IllagerHeadBlock(Properties properties) {
        super(MillagerTypes.ILLAGER, properties);
    }

}
