package org.lzyzl.millager.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class ForgeRegistryFactory implements DeferredRegister.Factory {

    private static IEventBus modBus;

    public static void setModBus(IEventBus bus) {
        modBus = bus;
    }

    static IEventBus modBus() {
        if (modBus == null) {
            throw new IllegalStateException("Forge mod event bus not set before registration");
        }
        return modBus;
    }

    private static ResourceLocation id(String modId, String name) {
        return ResourceLocation.fromNamespaceAndPath(modId, name);
    }

    private static <R, V extends R> DeferredHolder<R, V> wrap(ResourceKey<R> key, RegistryObject<V> forge) {
        DeferredHolder<R, V> holder = new DeferredHolder<>(key);
        holder.bind(forge);
        return holder;
    }

    private static <I extends Item> DeferredItem<I> wrapItem(ResourceKey<Item> key, RegistryObject<I> forge) {
        DeferredItem<I> item = new DeferredItem<>(key);
        item.bind(forge);
        return item;
    }

    private static <B extends Block> DeferredBlock<B> wrapBlock(ResourceKey<Block> key, RegistryObject<B> forge) {
        DeferredBlock<B> block = new DeferredBlock<>(key);
        block.bind(forge);
        return block;
    }

    @Override
    public <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> key, String modId) {
        return new ForgeDR<>(net.minecraftforge.registries.DeferredRegister.create(key, modId), key, modId);
    }

    @Override
    public DeferredRegister.Items createItems(String modId) {
        return new ForgeItems(net.minecraftforge.registries.DeferredRegister.create(ForgeRegistries.ITEMS, modId), modId);
    }

    @Override
    public DeferredRegister.Blocks createBlocks(String modId) {
        return new ForgeBlocks(net.minecraftforge.registries.DeferredRegister.create(ForgeRegistries.BLOCKS, modId), modId);
    }

    @Override
    public DeferredRegister.Entities createEntities(String modId) {
        return new ForgeEntities(net.minecraftforge.registries.DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, modId), modId);
    }

    @Override
    public DeferredRegister.BlockEntityTypes createBlockEntityTypes(String modId) {
        return new ForgeBlockEntityTypes(net.minecraftforge.registries.DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, modId), modId);
    }

    static class ForgeDR<T> extends DeferredRegister<T> {
        final net.minecraftforge.registries.DeferredRegister<T> forge;

        ForgeDR(net.minecraftforge.registries.DeferredRegister<T> forge,
                ResourceKey<? extends Registry<T>> key, String modId) {
            super(key, modId);
            this.forge = forge;
        }

        @Override
        public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> supplier) {
            ResourceKey<T> key = ResourceKey.create(registryKey, id(modId, name));
            return wrap(key, forge.register(name, supplier));
        }

        @Override
        public void register() {
            forge.register(modBus());
        }
    }

    static class ForgeItems extends DeferredRegister.Items {
        final net.minecraftforge.registries.DeferredRegister<Item> forge;

        ForgeItems(net.minecraftforge.registries.DeferredRegister<Item> forge, String modId) {
            super(modId);
            this.forge = forge;
        }

        @Override
        public <I extends Item> DeferredItem<I> registerItem(
                String name, Function<Item.Properties, ? extends I> factory, Supplier<Item.Properties> props) {
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(modId, name));
            return wrapItem(key, forge.register(name, () -> factory.apply(props.get())));
        }

        @Override
        public DeferredItem<SpawnEggItem> registerSpawnEgg(
                String name, Supplier<? extends EntityType<? extends Mob>> type,
                int backgroundColor, int highlightColor, Supplier<Item.Properties> props) {
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(modId, name));
            return wrapItem(key, forge.register(name,
                    () -> new ForgeSpawnEggItem(type, backgroundColor, highlightColor, props.get())));
        }

        @Override
        public <I extends Item> DeferredHolder<Item, I> register(String name, Supplier<? extends I> supplier) {
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(modId, name));
            return wrap(key, forge.register(name, supplier));
        }

        @Override
        public void register() {
            forge.register(modBus());
        }
    }

    static class ForgeBlocks extends DeferredRegister.Blocks {
        final net.minecraftforge.registries.DeferredRegister<Block> forge;

        ForgeBlocks(net.minecraftforge.registries.DeferredRegister<Block> forge, String modId) {
            super(modId);
            this.forge = forge;
        }

        @Override
        public <I extends Block> DeferredBlock<I> registerBlock(
                String name, Function<BlockBehaviour.Properties, ? extends I> factory,
                Supplier<BlockBehaviour.Properties> props) {
            ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id(modId, name));
            return wrapBlock(key, forge.register(name, () -> factory.apply(props.get())));
        }

        @Override
        public <I extends Block> DeferredHolder<Block, I> register(String name, Supplier<? extends I> supplier) {
            ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id(modId, name));
            return wrap(key, forge.register(name, supplier));
        }

        @Override
        public void register() {
            forge.register(modBus());
        }
    }

    static class ForgeEntities extends DeferredRegister.Entities {
        final net.minecraftforge.registries.DeferredRegister<EntityType<?>> forge;

        ForgeEntities(net.minecraftforge.registries.DeferredRegister<EntityType<?>> forge, String modId) {
            super(modId);
            this.forge = forge;
        }

        @Override
        public <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> registerEntityType(
                String name, EntityType.EntityFactory<E> factory, MobCategory category,
                UnaryOperator<EntityType.Builder<E>> op) {
            ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id(modId, name));
            return wrap(key, forge.register(name, () ->
                    op.apply(EntityType.Builder.of(factory, category)).build(modId + ":" + name)));
        }

        @Override
        public <I extends EntityType<?>> DeferredHolder<EntityType<?>, I> register(
                String name, Supplier<? extends I> supplier) {
            ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id(modId, name));
            return wrap(key, forge.register(name, supplier));
        }

        @Override
        public void register() {
            forge.register(modBus());
        }
    }

    static class ForgeBlockEntityTypes extends DeferredRegister.BlockEntityTypes {
        final net.minecraftforge.registries.DeferredRegister<BlockEntityType<?>> forge;

        ForgeBlockEntityTypes(net.minecraftforge.registries.DeferredRegister<BlockEntityType<?>> forge, String modId) {
            super(modId);
            this.forge = forge;
        }

        @Override
        public <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> registerBlockEntity(
                String name, BlockEntityFactory<T> factory, DeferredBlock<?>... validBlocks) {
            ResourceKey<BlockEntityType<?>> key = ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id(modId, name));
            return wrap(key, forge.register(name, () -> {
                Block[] blocks = new Block[validBlocks.length];
                for (int i = 0; i < validBlocks.length; i++) {
                    blocks[i] = validBlocks[i].get();
                }
                return BlockEntityType.Builder.of(factory::create, blocks).build(null);
            }));
        }

        @Override
        public <I extends BlockEntityType<?>> DeferredHolder<BlockEntityType<?>, I> register(
                String name, Supplier<? extends I> supplier) {
            ResourceKey<BlockEntityType<?>> key = ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id(modId, name));
            return wrap(key, forge.register(name, supplier));
        }

        @Override
        public void register() {
            forge.register(modBus());
        }
    }
}
