package org.lzyzl.millager.client.gui.screens;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientRegistryLayer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.Millager;
import org.lzyzl.millager.mixin.client.ClientPacketListenerAccessor;
import org.lzyzl.millager.mixin.client.EntityTypeAccessor;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;

public final class PreviewEntityLevel {

    private static volatile @Nullable ClientLevel previewLevel;
    private static final Map<Entity, Boolean> PREVIEW_ENTITIES = new WeakHashMap<>();
    private static int nextEntityId = 1;

    private PreviewEntityLevel() {
    }

    static @Nullable Entity create(EntityType<?> type, @Nullable Level currentLevel) {
        Level level = currentLevel != null ? currentLevel : getPreviewLevel();
        if (level == null) return null;

        try {
            Entity entity = createEntity(type, level);
            PREVIEW_ENTITIES.put(entity, Boolean.TRUE);
            return entity;
        } catch (Throwable exception) {
            Millager.LOGGER.error("Failed to create target relation preview entity for {}", type, exception);
            return null;
        }
    }

    static @Nullable LivingEntity createPlayer(@Nullable Level currentLevel) {
        ClientLevel level = currentLevel instanceof ClientLevel clientLevel ? clientLevel : getPreviewLevel();
        if (level == null) return null;

        try {
            RemotePlayer player = new PreviewPlayer(level, previewProfile());
            player.setId(nextEntityId++);
            PREVIEW_ENTITIES.put(player, Boolean.TRUE);
            return player;
        } catch (Throwable exception) {
            Millager.LOGGER.error("Failed to create target relation player preview entity", exception);
            return null;
        }
    }

    public static boolean isPreview(Entity entity) {
        return PREVIEW_ENTITIES.containsKey(entity);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> T createEntity(EntityType<T> type, Level level) {
        EntityType.EntityFactory<T> factory = ((EntityTypeAccessor<T>) type).millager$getFactory();
        T entity = factory.create(type, level);
        entity.setId(nextEntityId++);
        return entity;
    }

    private static synchronized @Nullable ClientLevel getPreviewLevel() {
        if (previewLevel != null) return previewLevel;
        LayeredRegistryAccess<ClientRegistryLayer> registryAccess = loadRegistryAccess();
        if (registryAccess == null) return null;
        previewLevel = createLevel(registryAccess);
        return previewLevel;
    }

    private static @Nullable LayeredRegistryAccess<ClientRegistryLayer> loadRegistryAccess() {
        try {
            LayeredRegistryAccess<ClientRegistryLayer> registryAccess = ClientRegistryLayer.createRegistryAccess();
            HolderLookup.Provider lookupProvider = VanillaRegistries.createLookup();
            List<Registry<?>> registries = new ArrayList<>();
            RegistryDataLoader.WORLDGEN_REGISTRIES.forEach(data ->
                    registries.add(copyRegistry(lookupProvider.lookup(data.key()).orElseThrow())));
            RegistryAccess.Frozen remote = new RegistryAccess.ImmutableRegistryAccess(registries).freeze();
            return registryAccess.replaceFrom(ClientRegistryLayer.REMOTE, remote);
        } catch (Throwable exception) {
            Millager.LOGGER.error("Failed to load target relation preview registries", exception);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Registry<T> copyRegistry(HolderLookup.RegistryLookup<T> lookup) {
        ResourceKey<? extends Registry<T>> key = (ResourceKey<? extends Registry<T>>) lookup.key();
        MappedRegistry<T> registry = new MappedRegistry<>(key, lookup.registryLifecycle());
        lookup.listElements().forEach(holder ->
                registry.register(holder.key(), holder.value(), lookup.registryLifecycle()));
        return registry.freeze();
    }

    private static @Nullable ClientLevel createLevel(LayeredRegistryAccess<ClientRegistryLayer> registryAccess) {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            GameProfile profile = minecraft.getUser().getGameProfile();
            WorldSessionTelemetryManager telemetry = new WorldSessionTelemetryManager(
                    TelemetryEventSender.DISABLED, false, Duration.ZERO, "millager-preview");
            ClientPacketListener connection = new ClientPacketListener(minecraft,
                    Objects.requireNonNull(minecraft.screen), new Connection(PacketFlow.CLIENTBOUND), null,
                    profile, telemetry);
            ((ClientPacketListenerAccessor) connection).millager$setRegistryAccess(registryAccess);
            ClientLevel.ClientLevelData levelData = new ClientLevel.ClientLevelData(Difficulty.NORMAL, false, false);
            levelData.setSpawn(BlockPos.ZERO, 0.0F);
            Holder<DimensionType> dimensionType = connection.registryAccess()
                    .registryOrThrow(Registries.DIMENSION_TYPE)
                    .getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD);
            return new ClientLevel(connection, levelData, Level.OVERWORLD, dimensionType,
                    2, 2, minecraft::getProfiler, minecraft.levelRenderer, false, 0L);
        } catch (Throwable exception) {
            Millager.LOGGER.error("Failed to create target relation preview level", exception);
            return null;
        }
    }

    private static GameProfile previewProfile() {
        return new GameProfile(UUID.nameUUIDFromBytes("millager-preview".getBytes(StandardCharsets.UTF_8)), "Preview");
    }

    private static final class PreviewPlayer extends RemotePlayer {

        private PreviewPlayer(ClientLevel level, GameProfile profile) {
            super(level, profile);
        }

        @Override
        protected @Nullable PlayerInfo getPlayerInfo() {
            return null;
        }

    }

}
