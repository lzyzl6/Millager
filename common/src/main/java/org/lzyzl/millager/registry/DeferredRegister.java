package org.lzyzl.millager.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
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

import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;


public abstract class DeferredRegister<T> {

    protected final ResourceKey<? extends Registry<T>> registryKey;
    protected final String modId;

    protected DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String modId) {
        this.registryKey = registryKey;
        this.modId = modId;
    }

    public abstract <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> supplier);

    public abstract void register();

    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> key, String modid) {
        return factory().create(key, modid);
    }

    public static <T> DeferredRegister<T> create(Registry<T> registry, String modid) {
        return factory().create(registry.key(), modid);
    }

    public static Items createItems(String modid) {
        return factory().createItems(modid);
    }

    public static Blocks createBlocks(String modid) {
        return factory().createBlocks(modid);
    }

    public static Entities createEntities(String modid) {
        return factory().createEntities(modid);
    }

    public static BlockEntityTypes createBlockEntityTypes(String modid) {
        return factory().createBlockEntityTypes(modid);
    }

    public abstract static class Items extends DeferredRegister<Item> {
        protected Items(String modid) {
            super(Registries.ITEM, modid);
        }

        public abstract <I extends Item> DeferredItem<I> registerItem(
                String name, Function<Item.Properties, ? extends I> factory, Supplier<Item.Properties> props);

        public <I extends Item> DeferredItem<I> registerItem(
                String name, Function<Item.Properties, ? extends I> factory) {
            return registerItem(name, factory, Item.Properties::new);
        }

        public abstract DeferredItem<SpawnEggItem> registerSpawnEgg(
                String name, Supplier<? extends EntityType<? extends Mob>> type,
                int backgroundColor, int highlightColor, Supplier<Item.Properties> props);
    }

    public abstract static class Blocks extends DeferredRegister<Block> {
        protected Blocks(String modid) {
            super(Registries.BLOCK, modid);
        }

        public abstract <I extends Block> DeferredBlock<I> registerBlock(
                String name, Function<BlockBehaviour.Properties, ? extends I> factory, Supplier<BlockBehaviour.Properties> props);
    }

    public abstract static class Entities extends DeferredRegister<EntityType<?>> {
        protected Entities(String modid) {
            super(Registries.ENTITY_TYPE, modid);
        }

        public abstract <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> registerEntityType(
                String name, EntityType.EntityFactory<E> factory, MobCategory category, UnaryOperator<EntityType.Builder<E>> op);
    }

    public abstract static class BlockEntityTypes extends DeferredRegister<BlockEntityType<?>> {
        protected BlockEntityTypes(String modid) {
            super(Registries.BLOCK_ENTITY_TYPE, modid);
        }

        public abstract <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> registerBlockEntity(
                String name, BlockEntityFactory<T> factory, DeferredBlock<?>... validBlocks);
    }

    public interface Factory {
        <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> key, String modid);

        Items createItems(String modid);

        Blocks createBlocks(String modid);

        Entities createEntities(String modid);

        BlockEntityTypes createBlockEntityTypes(String modid);
    }

    private static Factory factoryInstance;

    private static Factory factory() {
        if (factoryInstance == null) {
            factoryInstance = ServiceLoader.load(Factory.class)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No DeferredRegister.Factory service found on classpath"));
        }
        return factoryInstance;
    }
}
