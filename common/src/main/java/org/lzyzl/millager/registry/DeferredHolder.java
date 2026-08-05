package org.lzyzl.millager.registry;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

public class DeferredHolder<R, V extends R> implements Supplier<V> {

    protected final ResourceKey<R> key;
    protected Supplier<V> value;
    protected Supplier<Holder<R>> holder;

    public DeferredHolder(ResourceKey<R> key) {
        this.key = key;
    }

    public DeferredHolder(ResourceKey<R> key, Supplier<V> value) {
        this.key = key;
        this.value = value;
    }

    public void bind(Supplier<V> value) {
        this.value = value;
    }

    public void bindHolder(Supplier<Holder<R>> holder) {
        this.holder = holder;
    }

    public ResourceKey<R> getKey() {
        return key;
    }

    public Holder<R> holder() {
        if (holder == null) {
            throw new IllegalStateException("DeferredHolder has no bound Holder: " + key);
        }
        return holder.get();
    }

    @Override
    public V get() {
        if (value == null) {
            throw new IllegalStateException("DeferredHolder not yet registered: " + key);
        }
        return value.get();
    }
}
