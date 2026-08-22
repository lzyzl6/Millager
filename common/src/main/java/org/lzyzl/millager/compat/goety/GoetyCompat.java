package org.lzyzl.millager.compat.goety;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.Millager;
import org.lzyzl.millager.behavior.MiscConfig;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class GoetyCompat {

    private static final String RAIDER_SERVANT = "com.Polarice3.Goety.common.entities.ally.illager.raider.RaiderServant";
    private static final String SOUL_ENERGY_HELPER = "com.Polarice3.Goety.utils.SEHelper";
    private static boolean availabilityChecked;
    private static boolean available;
    private static @Nullable Class<?> raiderServantClass;
    private static @Nullable Method getOwnerId;
    private static @Nullable Method isRaiding;

    private GoetyCompat() {
    }

    public static boolean isAvailable() {
        if (!availabilityChecked) {
            try {
                raiderServantClass = Class.forName(RAIDER_SERVANT);
                getOwnerId = raiderServantClass.getMethod("getOwnerId");
                isRaiding = raiderServantClass.getMethod("isRaiding");
                available = true;
            } catch (ReflectiveOperationException ignored) {
                available = false;
            }
            availabilityChecked = true;
        }
        return available;
    }

    public static void onRaidingHornUsed(Player player) {
        ServerLevel level = getRaidingHornLevel(player);
        if (level == null) return;
        BlockPos center = player.blockPosition();
        if (getOwnedRaidingServants(level, player.getUUID(), center).isEmpty()) return;

        GoetyReinforcementController.startRaid(level, player.getUUID(), center);
    }

    private static @Nullable ServerLevel getRaidingHornLevel(Player player) {
        if (!MiscConfig.ENABLE_GOETY_RAIDS
                || !(player.level() instanceof ServerLevel level)
                || !level.isVillage(player.blockPosition())) return null;
        return level;
    }

    public static void clearGoodwillAndNotify(Player player) {
        if (clearGoodwill(player)) {
            player.displayClientMessage(Component.translatable("message.millager.goety_reputation"), true);
        }
    }

    public static List<LivingEntity> getOwnedRaidingServants(ServerLevel level, UUID owner, BlockPos center) {
        List<LivingEntity> servants = new ArrayList<>();
        if (!isAvailable() || raiderServantClass == null) return servants;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(center).inflate(128.0D), raiderServantClass::isInstance)) {
            if (isOwnedRaidingServant(entity, owner)) servants.add(entity);
        }
        return servants;
    }

    public static boolean isRaidTarget(LivingEntity entity, UUID owner) {
        if (entity instanceof Player player) {
            return owner.equals(player.getUUID()) && player.isAlive() && !player.isInvulnerable()
                    && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player);
        }
        return isOwnedRaidingServant(entity, owner);
    }

    public static boolean isRaidOwnerPresent(Player player, UUID owner) {
        return owner.equals(player.getUUID()) && player.isAlive() && !player.isSpectator();
    }

    private static boolean isOwnedRaidingServant(LivingEntity entity, UUID owner) {
        if (!isAvailable() || raiderServantClass == null || getOwnerId == null || isRaiding == null
                || !raiderServantClass.isInstance(entity) || !entity.isAlive()) return false;
        try {
            return owner.equals(getOwnerId.invoke(entity)) && Boolean.TRUE.equals(isRaiding.invoke(entity));
        } catch (ReflectiveOperationException exception) {
            available = false;
            Millager.LOGGER.warn("Unable to inspect Goety raiding servant", exception);
            return false;
        }
    }

    private static boolean clearGoodwill(Player player) {
        try {
            Class<?> helper = Class.forName(SOUL_ENERGY_HELPER);
            Method getAllyEntities = helper.getMethod("getAllyEntities", Player.class);
            Method removeAllyEntity = helper.getMethod("removeAllyEntity", Player.class, LivingEntity.class);
            Method getAllyEntityTypes = helper.getMethod("getAllyEntityTypes", Player.class);
            Method removeAllyEntityType = helper.getMethod("removeAllyEntityType", Player.class, EntityType.class);
            boolean removed = false;

            Object allyEntities = getAllyEntities.invoke(null, player);
            if (allyEntities instanceof List<?> entities) {
                for (Object entity : List.copyOf(entities)) {
                    if (entity instanceof LivingEntity livingEntity && isRaidGoodwillTarget(livingEntity)) {
                        removed |= Boolean.TRUE.equals(removeAllyEntity.invoke(null, player, livingEntity));
                    }
                }
            }

            Object allyTypes = getAllyEntityTypes.invoke(null, player);
            if (allyTypes instanceof List<?> entityTypes) {
                for (Object entityType : List.copyOf(entityTypes)) {
                    if (entityType instanceof EntityType<?> type && isRaidGoodwillTarget(type)) {
                        removed |= Boolean.TRUE.equals(removeAllyEntityType.invoke(null, player, type));
                    }
                }
            }
            return removed;
        } catch (ReflectiveOperationException exception) {
            Millager.LOGGER.warn("Unable to clear Goety raid target entries from the Grimoire of Goodwill", exception);
            return false;
        }
    }

    private static boolean isRaidGoodwillTarget(LivingEntity entity) {
        return isRaidGoodwillTarget(entity.getType());
    }

    private static boolean isRaidGoodwillTarget(EntityType<?> entityType) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        return Millager.MOD_ID.equals(id.getNamespace());
    }
}
