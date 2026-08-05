package org.lzyzl.millager.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class NeoForgeRegistryFactory implements DeferredRegister.Factory {

    private static IEventBus modBus;

    public static void setModBus(IEventBus bus) {
        modBus = bus;
    }

    static IEventBus modBus() {
        if (modBus == null) {
            throw new IllegalStateException("NeoForge modId event bus not set before registration");
        }
        return modBus;
    }

    private static <R, V extends R> DeferredHolder<R, V> wrap(net.neoforged.neoforge.registries.DeferredHolder<R, V> neo) {
        DeferredHolder<R, V> holder = new DeferredHolder<>(neo.getKey());
        holder.bind(neo);
        holder.bindHolder(() -> neo);
        return holder;
    }

    private static <I extends Item> DeferredItem<I> wrapItem(net.neoforged.neoforge.registries.DeferredItem<I> neo) {
        DeferredItem<I> item = new DeferredItem<>(neo.getKey());
        item.bind(neo);
        item.bindHolder(() -> neo);
        return item;
    }

    private static <B extends Block> DeferredBlock<B> wrapBlock(net.neoforged.neoforge.registries.DeferredBlock<B> neo) {
        DeferredBlock<B> block = new DeferredBlock<>(neo.getKey());
        block.bind(neo);
        block.bindHolder(() -> neo);
        return block;
    }

    @Override
    public <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> key, String modid) {
        return new NeoDR<>(net.neoforged.neoforge.registries.DeferredRegister.create(key, modid), key, modid);
    }

    @Override
    public DeferredRegister.Items createItems(String modid) {
        return new NeoItems(net.neoforged.neoforge.registries.DeferredRegister.createItems(modid), modid);
    }

    @Override
    public DeferredRegister.Blocks createBlocks(String modid) {
        return new NeoBlocks(net.neoforged.neoforge.registries.DeferredRegister.createBlocks(modid), modid);
    }

    @Override
    public DeferredRegister.Entities createEntities(String modid) {
        return new NeoEntities(net.neoforged.neoforge.registries.DeferredRegister.createEntities(modid), modid);
    }

    static class NeoDR<T> extends DeferredRegister<T> {
        final net.neoforged.neoforge.registries.DeferredRegister<T> neo;

        NeoDR(net.neoforged.neoforge.registries.DeferredRegister<T> neo, ResourceKey<? extends Registry<T>> key, String modid) {
            super(key, modid);
            this.neo = neo;
        }

        @Override
        public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> supplier) {
            return wrap(neo.register(name, supplier));
        }

        @Override
        public void register() {
            neo.register(modBus());
        }
    }

    static class NeoItems extends DeferredRegister.Items {
        final net.neoforged.neoforge.registries.DeferredRegister.Items neo;

        NeoItems(net.neoforged.neoforge.registries.DeferredRegister.Items neo, String modid) {
            super(modid);
            this.neo = neo;
        }

        @Override
        public <I extends Item> DeferredItem<I> registerItem(
                String name, Function<Item.Properties, ? extends I> factory, Supplier<Item.Properties> props) {
            return wrapItem(neo.registerItem(name, factory, props));
        }

        @Override
        public <I extends Item> DeferredHolder<Item, I> register(String name, Supplier<? extends I> supplier) {
            return wrap(neo.register(name, supplier));
        }

        @Override
        public void register() {
            neo.register(modBus());
        }
    }

    static class NeoBlocks extends DeferredRegister.Blocks {
        final net.neoforged.neoforge.registries.DeferredRegister.Blocks neo;

        NeoBlocks(net.neoforged.neoforge.registries.DeferredRegister.Blocks neo, String modid) {
            super(modid);
            this.neo = neo;
        }

        @Override
        public <I extends Block> DeferredBlock<I> registerBlock(
                String name, Function<BlockBehaviour.Properties, ? extends I> factory, Supplier<BlockBehaviour.Properties> props) {
            return wrapBlock(neo.registerBlock(name, factory, props));
        }

        @Override
        public <I extends Block> DeferredHolder<Block, I> register(String name, Supplier<? extends I> supplier) {
            return wrap(neo.register(name, supplier));
        }

        @Override
        public void register() {
            neo.register(modBus());
        }
    }

    static class NeoEntities extends DeferredRegister.Entities {
        final net.neoforged.neoforge.registries.DeferredRegister.Entities neo;

        NeoEntities(net.neoforged.neoforge.registries.DeferredRegister.Entities neo, String modid) {
            super(modid);
            this.neo = neo;
        }

        @Override
        public <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> registerEntityType(
                String name, EntityType.EntityFactory<E> factory, MobCategory category, UnaryOperator<EntityType.Builder<E>> op) {
            return wrap(neo.registerEntityType(name, factory, category, op));
        }

        @Override
        public <I extends EntityType<?>> DeferredHolder<EntityType<?>, I> register(String name, Supplier<? extends I> supplier) {
            return wrap(neo.register(name, supplier));
        }

        @Override
        public void register() {
            neo.register(modBus());
        }
    }
}
