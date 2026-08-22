package org.lzyzl.millager.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import org.lzyzl.millager.config.MillagerConfig;
import org.lzyzl.millager.entity.millager.AbstractMillager;

import java.util.Optional;
import java.util.Set;

public final class MillagerTargetingHelper {

    private static final ThreadLocal<Boolean> CHECKING_EXTERNAL_ALLIANCE = ThreadLocal.withInitial(() -> false);
    private static final Set<String> DEFAULT_FRIENDLY_ENTITY_TYPES = Set.of(
            "guardvillagers:guard",
            "recruits:recruit",
            "recruits:recruit_shieldman",
            "recruits:bowman",
            "recruits:crossbowman",
            "recruits:nomad",
            "recruits:horseman",
            "recruits:messenger",
            "recruits:scout",
            "recruits:patrol_leader",
            "recruits:captain",
            "recruits:villager_noble",
            "recruits:siege_engineer",
            "jerotesvillage:carved_villager",
            "jerotesvillage:carved_allay",
            "jerotesvillage:carved_iron_golem",
            "jerotesvillage:spear_machine",
            "jerotesvillage:carved_hound"
    );

    private MillagerTargetingHelper() {
    }

    public static boolean isCheckingExternalAlliance() {
        return CHECKING_EXTERNAL_ALLIANCE.get();
    }

    public static boolean isMillagerEntity(Entity entity) {
        return MiscHelper.isMillagerFaction(entity);
    }

    public static TargetRelation relation(LivingEntity entity) {
        TargetRelation special = specialRelation(entity);
        return special != null ? special : defaultRelation(entity);
    }

    public static boolean isFriendlyToMillager(LivingEntity entity) {
        TargetRelation special = specialRelation(entity);
        if (special != null) return special == TargetRelation.FRIENDLY;

        Optional<TargetRelation> override = MillagerConfig.targetRelation(id(entity));
        return override.orElseGet(() -> defaultRelation(entity)) == TargetRelation.FRIENDLY;
    }

    public static boolean isFriendlyToMillager(AbstractMillager millager, LivingEntity entity) {
        TargetRelation special = specialRelation(millager, entity);
        if (special != null) return special == TargetRelation.FRIENDLY;

        Optional<TargetRelation> override = MillagerConfig.targetRelation(id(entity));
        return override.orElseGet(() -> defaultRelation(entity)) == TargetRelation.FRIENDLY;
    }

    public static boolean isHostileToMillager(LivingEntity entity) {
        TargetRelation special = specialRelation(entity);
        if (special != null) return special == TargetRelation.HOSTILE;

        Optional<TargetRelation> override = MillagerConfig.targetRelation(id(entity));
        return override.orElseGet(() -> defaultRelation(entity)) == TargetRelation.HOSTILE;
    }

    public static boolean isHostileToMillager(AbstractMillager millager, LivingEntity entity) {
        TargetRelation special = specialRelation(millager, entity);
        if (special != null) return special == TargetRelation.HOSTILE;

        Optional<TargetRelation> override = MillagerConfig.targetRelation(id(entity));
        return override.orElseGet(() -> defaultRelation(entity)) == TargetRelation.HOSTILE;
    }

    public static boolean canAttack(AbstractMillager millager, LivingEntity entity) {
        return isHostileToMillager(millager, entity) && entity != millager;
    }

    public static boolean hasBeeGolemOverride(LivingEntity entity) {
        return MillagerConfig.beeGolemOverride(id(entity)).isPresent();
    }

    public static boolean canBeeGolemAttack(LivingEntity entity) {
        return MillagerConfig.beeGolemOverride(id(entity)).orElseGet(() -> defaultBeeGolemAttack(entity));
    }

    public static boolean defaultBeeGolemAttack(LivingEntity entity) {
        return entity instanceof Enemy;
    }

    public static boolean areFriendly(LivingEntity first, LivingEntity second) {
        if (first instanceof AbstractMillager millager) return isFriendlyToMillager(millager, second);
        if (second instanceof AbstractMillager millager) return isFriendlyToMillager(millager, first);
        if (isMillagerEntity(first)) return isFriendlyToMillager(second);
        return isMillagerEntity(second) && isFriendlyToMillager(first);
    }

    public static String id(Entity entity) {
        return String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
    }

    private static TargetRelation specialRelation(LivingEntity entity) {
        if (entity instanceof IronGolem golem && MultigolemDetector.isZombieGolem(golem)) return TargetRelation.HOSTILE;
        if (DEFAULT_FRIENDLY_ENTITY_TYPES.contains(id(entity))) return TargetRelation.FRIENDLY;
        return null;
    }

    private static TargetRelation specialRelation(AbstractMillager millager, LivingEntity entity) {
        TargetRelation special = specialRelation(entity);
        if (special != null) return special;
        if (MiscHelper.isMillagerFaction(entity)) return null;
        if (isExternallyAllied(entity, millager)) return TargetRelation.FRIENDLY;
        return null;
    }

    private static boolean isExternallyAllied(LivingEntity entity, AbstractMillager millager) {
        if (CHECKING_EXTERNAL_ALLIANCE.get()) return false;
        CHECKING_EXTERNAL_ALLIANCE.set(true);
        try {
            return entity.isAlliedTo(millager);
        } finally {
            CHECKING_EXTERNAL_ALLIANCE.remove();
        }
    }

    private static TargetRelation defaultRelation(LivingEntity entity) {
        if (MiscHelper.isMillagerFaction(entity)) return TargetRelation.FRIENDLY;
        if (entity instanceof Creeper) return TargetRelation.NEUTRAL;
        if (entity instanceof Enemy) return TargetRelation.HOSTILE;
        return TargetRelation.NEUTRAL;
    }
}
