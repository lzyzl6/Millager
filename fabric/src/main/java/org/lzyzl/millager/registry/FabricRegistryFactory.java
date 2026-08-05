package org.lzyzl.millager.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class FabricRegistryFactory implements DeferredRegister.Factory {

    @Override
    public <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> key, String modId) {
        return new FabDR<>(key, modId);
    }

    @Override
    public DeferredRegister.Items createItems(String modId) {
        return new FabItems(modId);
    }

    @Override
    public DeferredRegister.Blocks createBlocks(String modId) {
        return new FabBlocks(modId);
    }

    @Override
    public DeferredRegister.Entities createEntities(String modId) {
        return new FabEntities(modId);
    }

    @Override
    public DeferredRegister.BlockEntityTypes createBlockEntityTypes(String modId) {
        return new FabBlockEntityTypes(modId);
    }

    static ResourceLocation id(String modId, String name) {
        return ResourceLocation.fromNamespaceAndPath(modId, name);
    }

    static class FabDR<T> extends DeferredRegister<T> {
        final List<Runnable> finalizers = new ArrayList<>();

        FabDR(ResourceKey<? extends Registry<T>> registryKey, String modId) {
            super(registryKey, modId);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> supplier) {
            ResourceKey<T> key = ResourceKey.create(registryKey, id(modId, name));
            DeferredHolder<T, I> holder = new DeferredHolder<>(key);
            finalizers.add(() -> {
                Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(registryKey.location());
                I value = supplier.get();
                Holder.Reference<T> ref = Registry.registerForHolder(registry, key, value);
                holder.bind(() -> value);
                holder.bindHolder(() -> ref);
            });
            return holder;
        }

        @Override
        public void register() {
            finalizers.forEach(Runnable::run);
            finalizers.clear();
        }
    }

    static class FabItems extends DeferredRegister.Items {
        final List<Runnable> finalizers = new ArrayList<>();

        FabItems(String modId) {
            super(modId);
        }

        @Override
        public <I extends Item> DeferredItem<I> registerItem(
                String name, Function<Item.Properties, ? extends I> factory, Supplier<Item.Properties> props) {
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(modId, name));
            DeferredItem<I> holder = new DeferredItem<>(key);
            finalizers.add(() -> {
                I item = factory.apply(props.get());
                Registry.register(BuiltInRegistries.ITEM, key, item);
                holder.bind(() -> item);
            });
            return holder;
        }

        @Override
        public <I extends Item> DeferredHolder<Item, I> register(String name, Supplier<? extends I> supplier) {
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(modId, name));
            DeferredHolder<Item, I> holder = new DeferredHolder<>(key);
            finalizers.add(() -> {
                I value = supplier.get();
                Registry.register(BuiltInRegistries.ITEM, key, value);
                holder.bind(() -> value);
            });
            return holder;
        }

        @Override
        public void register() {
            finalizers.forEach(Runnable::run);
            finalizers.clear();
        }
    }

    static class FabBlocks extends DeferredRegister.Blocks {
        final List<Runnable> finalizers = new ArrayList<>();

        FabBlocks(String modId) {
            super(modId);
        }

        @Override
        public <I extends Block> DeferredBlock<I> registerBlock(
                String name, Function<BlockBehaviour.Properties, ? extends I> factory, Supplier<BlockBehaviour.Properties> props) {
            ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id(modId, name));
            DeferredBlock<I> holder = new DeferredBlock<>(key);
            finalizers.add(() -> {
                I block = factory.apply(props.get());
                Registry.register(BuiltInRegistries.BLOCK, key, block);
                holder.bind(() -> block);
            });
            return holder;
        }

        @Override
        public <I extends Block> DeferredHolder<Block, I> register(String name, Supplier<? extends I> supplier) {
            ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id(modId, name));
            DeferredHolder<Block, I> holder = new DeferredHolder<>(key);
            finalizers.add(() -> {
                I value = supplier.get();
                Registry.register(BuiltInRegistries.BLOCK, key, value);
                holder.bind(() -> value);
            });
            return holder;
        }

        @Override
        public void register() {
            finalizers.forEach(Runnable::run);
            finalizers.clear();
        }
    }

    static class FabEntities extends DeferredRegister.Entities {
        final List<Runnable> finalizers = new ArrayList<>();

        FabEntities(String modId) {
            super(modId);
        }

        @Override
        public <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> registerEntityType(
                String name, EntityType.EntityFactory<E> factory, MobCategory category, UnaryOperator<EntityType.Builder<E>> op) {
            ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id(modId, name));
            DeferredHolder<EntityType<?>, EntityType<E>> holder = new DeferredHolder<>(key);
            finalizers.add(() -> {
                EntityType.Builder<E> builder = op.apply(EntityType.Builder.of(factory, category));
                EntityType<E> type = builder.build(key.location().toString());
                Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
                holder.bind(() -> type);
            });
            return holder;
        }

        @Override
        public <I extends EntityType<?>> DeferredHolder<EntityType<?>, I> register(String name, Supplier<? extends I> supplier) {
            ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id(modId, name));
            DeferredHolder<EntityType<?>, I> holder = new DeferredHolder<>(key);
            finalizers.add(() -> {
                I value = supplier.get();
                Registry.register(BuiltInRegistries.ENTITY_TYPE, key, value);
                holder.bind(() -> value);
            });
            return holder;
        }

        @Override
        public void register() {
            finalizers.forEach(Runnable::run);
            finalizers.clear();
        }
    }

    static class FabBlockEntityTypes extends DeferredRegister.BlockEntityTypes {
        final List<Runnable> finalizers = new ArrayList<>();

        FabBlockEntityTypes(String modId) {
            super(modId);
        }

        @Override
        public <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> registerBlockEntity(
                String name, BlockEntityFactory<T> factory, DeferredBlock<?>... validBlocks) {
            ResourceKey<BlockEntityType<?>> key = ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id(modId, name));
            DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> holder = new DeferredHolder<>(key);
            finalizers.add(() -> {
                Block[] blocks = new Block[validBlocks.length];
                for (int i = 0; i < validBlocks.length; i++) {
                    blocks[i] = validBlocks[i].get();
                }
                BlockEntityType<T> type = BlockEntityType.Builder.of(factory::create, blocks).build(null);
                Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, type);
                holder.bind(() -> type);
            });
            return holder;
        }

        @Override
        public <I extends BlockEntityType<?>> DeferredHolder<BlockEntityType<?>, I> register(String name, Supplier<? extends I> supplier) {
            ResourceKey<BlockEntityType<?>> key = ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id(modId, name));
            DeferredHolder<BlockEntityType<?>, I> holder = new DeferredHolder<>(key);
            finalizers.add(() -> {
                I value = supplier.get();
                Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, value);
                holder.bind(() -> value);
            });
            return holder;
        }

        @Override
        public void register() {
            finalizers.forEach(Runnable::run);
            finalizers.clear();
        }
    }
}
