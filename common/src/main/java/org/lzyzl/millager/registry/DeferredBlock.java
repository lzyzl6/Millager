package org.lzyzl.millager.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class DeferredBlock<V extends Block> extends DeferredHolder<Block, V> {

    public DeferredBlock(ResourceKey<Block> key) {
        super(key);
    }

    public DeferredBlock(ResourceKey<Block> key, Supplier<V> value) {
        super(key, value);
    }
}
