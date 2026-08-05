package org.lzyzl.millager.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;

import java.util.Set;
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

    private static <I extends Item> DeferredItem<I> wrapItem(net.neoforged.neoforge.registries.DeferredHolder<Item, I> neo) {
        DeferredItem<I> item = new DeferredItem<>(neo.getKey());
        item.bind(neo);
        item.bindHolder(() -> neo);
        return item;
    }

    private static <B extends Block> DeferredBlock<B> wrapBlock(net.neoforged.neoforge.registries.DeferredHolder<Block, B> neo) {
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
        return new NeoEntities(
                net.neoforged.neoforge.registries.DeferredRegister.create(Registries.ENTITY_TYPE, modid),
                modid
        );
    }

    @Override
    public DeferredRegister.BlockEntityTypes createBlockEntityTypes(String modid) {
        return new NeoBlockEntityTypes(
                net.neoforged.neoforge.registries.DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, modid),
                modid
        );
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
            return wrapItem(neo.register(name, () -> factory.apply(props.get())));
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
            return wrapBlock(neo.register(name, () -> factory.apply(props.get())));
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
        final net.neoforged.neoforge.registries.DeferredRegister<EntityType<?>> neo;

        NeoEntities(net.neoforged.neoforge.registries.DeferredRegister<EntityType<?>> neo, String modid) {
            super(modid);
            this.neo = neo;
        }

        @Override
        public <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> registerEntityType(
                String name, EntityType.EntityFactory<E> factory, MobCategory category, UnaryOperator<EntityType.Builder<E>> op) {
            return wrap(neo.register(name, () -> {
                EntityType.Builder<E> builder = op.apply(EntityType.Builder.of(factory, category));
                return builder.build(modId + ":" + name);
            }));
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

    static class NeoBlockEntityTypes extends DeferredRegister.BlockEntityTypes {
        final net.neoforged.neoforge.registries.DeferredRegister<BlockEntityType<?>> neo;

        NeoBlockEntityTypes(net.neoforged.neoforge.registries.DeferredRegister<BlockEntityType<?>> neo, String modid) {
            super(modid);
            this.neo = neo;
        }

        @Override
        public <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> registerBlockEntity(
                String name, BlockEntityFactory<T> factory, DeferredBlock<?>... validBlocks) {
            return wrap(neo.register(name, () -> {
                Block[] blocks = new Block[validBlocks.length];
                for (int i = 0; i < validBlocks.length; i++) {
                    blocks[i] = validBlocks[i].get();
                }
                return new BlockEntityType<>(factory::create, Set.of(blocks), null);
            }));
        }

        @Override
        public <I extends BlockEntityType<?>> DeferredHolder<BlockEntityType<?>, I> register(String name, Supplier<? extends I> supplier) {
            return wrap(neo.register(name, supplier));
        }

        @Override
        public void register() {
            neo.register(modBus());
        }
    }
}
