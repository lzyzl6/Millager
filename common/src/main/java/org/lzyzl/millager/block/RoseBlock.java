package org.lzyzl.millager.block;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class RoseBlock extends FlowerBlock {

    public RoseBlock(Holder<MobEffect> suspiciousStewEffect, int effectDuration, BlockBehaviour.Properties properties) {
        super(suspiciousStewEffect, effectDuration, properties);
    }
}
