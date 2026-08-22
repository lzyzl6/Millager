package org.lzyzl.millager.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.NullMarked;

import java.util.function.Supplier;

@NullMarked
public class DeferredItem<V extends Item> extends DeferredHolder<Item, V> implements ItemLike {

    public DeferredItem(ResourceKey<Item> key) {
        super(key);
    }

    public DeferredItem(ResourceKey<Item> key, Supplier<V> value) {
        super(key, value);
    }

    @Override
    public Item asItem() {
        return get();
    }
}
