package org.lzyzl.millager.client.gui.screens;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ServerLinks;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.Millager;
import org.lzyzl.millager.mixin.client.EntityTypeAccessor;

import java.time.Duration;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.UnaryOperator;

public final class PreviewEntityLevel {

    private static volatile @Nullable ClientLevel previewLevel;
    private static final Set<Entity> previewEntities = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<EntityType<?>> failedRenderTypes = Collections.newSetFromMap(new IdentityHashMap<>());
    private static int nextEntityId = 1;

    private PreviewEntityLevel() {
    }

    static @Nullable Entity create(EntityType<?> type, @Nullable Level currentLevel) {
        Level level = currentLevel != null ? currentLevel : getPreviewLevel();
        if (level == null) return null;

        try {
            return createEntity(type, level);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static boolean isPreview(Entity entity) {
        return previewEntities.contains(entity);
    }

    public static boolean renderFailed(EntityType<?> type) {
        return failedRenderTypes.contains(type);
    }

    public static void failRender(EntityType<?> type, Throwable exception) {
        if (failedRenderTypes.add(type)) {
            Millager.LOGGER.error("Disabling target relation previews for {}", type, exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> @Nullable T createEntity(EntityType<T> type, Level level) {
        EntityType.EntityFactory<T> factory = (EntityType.EntityFactory<T>) ((EntityTypeAccessor) type).millager$getFactory();
        T entity = factory.create(type, level);
        if (entity == null) return null;
        entity.setId(nextEntityId++);
        previewEntities.add(entity);
        return entity;
    }

    private static synchronized @Nullable ClientLevel getPreviewLevel() {
        if (previewLevel != null) return previewLevel;
        RegistryAccess.Frozen registryAccess = loadRegistryAccess();
        if (registryAccess == null) return null;
        previewLevel = createLevel(registryAccess);
        return previewLevel;
    }

    private static RegistryAccess.@Nullable Frozen loadRegistryAccess() {
        try {
            List<? extends Registry<?>> registries = VanillaRegistries.createLookup().listRegistries()
                    .map(PreviewEntityLevel::materializeRegistry).toList();
            return new RegistryAccess.ImmutableRegistryAccess(registries).freeze();
        } catch (Throwable exception) {
            Millager.LOGGER.error("Failed to load target relation preview registries", exception);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Registry<?> materializeRegistry(HolderLookup.RegistryLookup<?> lookup) {
        if (lookup instanceof Registry<?> registry) return registry;
        if (lookup.key().equals(Registries.DIMENSION_TYPE)) {
            return copyRegistry((HolderLookup.RegistryLookup<DimensionType>) lookup,
                    PreviewEntityLevel::withoutTimelines);
        }
        return copyRegistry(lookup);
    }

    private static DimensionType withoutTimelines(DimensionType type) {
        return new DimensionType(type.hasFixedTime(), type.hasSkyLight(), type.hasCeiling(),
                type.hasEnderDragonFight(), type.coordinateScale(), type.minY(), type.height(), type.logicalHeight(),
                type.infiniburn(), type.ambientLight(), type.monsterSettings(), type.skybox(), type.cardinalLightType(),
                type.attributes(), HolderSet.empty(), type.defaultClock());
    }

    private static <T> Registry<T> copyRegistry(HolderLookup.RegistryLookup<T> lookup) {
        return copyRegistry(lookup, UnaryOperator.identity());
    }

    @SuppressWarnings("unchecked")
    private static <T> Registry<T> copyRegistry(HolderLookup.RegistryLookup<T> lookup, UnaryOperator<T> valueMapper) {
        ResourceKey<? extends Registry<T>> key = (ResourceKey<? extends Registry<T>>) lookup.key();
        MappedRegistry<T> registry = new MappedRegistry<>(key, lookup.registryLifecycle());
        lookup.listElements().forEach(holder ->
                registry.register(holder.key(), valueMapper.apply(holder.value()), RegistrationInfo.BUILT_IN));
        return registry.freeze();
    }

    private static @Nullable ClientLevel createLevel(RegistryAccess.Frozen registryAccess) {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            GameProfile profile = minecraft.getGameProfile();
            WorldSessionTelemetryManager telemetry = new WorldSessionTelemetryManager(
                    TelemetryEventSender.DISABLED, false, Duration.ZERO, "millager-preview");
            CommonListenerCookie cookie = new CommonListenerCookie(new LevelLoadTracker(), profile, telemetry,
                    registryAccess, FeatureFlags.DEFAULT_FLAGS, "millager-preview", null, null, Map.of(),
                    new ChatComponent.State(List.of(), List.of(), List.of()), Map.of(), ServerLinks.EMPTY,
                    Map.of(), false);
            ClientPacketListener connection = new ClientPacketListener(minecraft,
                    new Connection(PacketFlow.CLIENTBOUND), cookie);
            ClientLevel.ClientLevelData levelData = new ClientLevel.ClientLevelData(Difficulty.NORMAL, false, false);
            return new ClientLevel(connection, levelData, Level.OVERWORLD,
                    registryAccess.lookupOrThrow(Registries.DIMENSION_TYPE).getOrThrow(BuiltinDimensionTypes.OVERWORLD),
                    2, 2, minecraft.levelRenderer, false, 0L, 63);
        } catch (Throwable exception) {
            Millager.LOGGER.error("Failed to create target relation preview level", exception);
            return null;
        }
    }

}
