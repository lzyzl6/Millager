package org.lzyzl.millager;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.*;
import org.lzyzl.millager.config.MillagerConfig;
import org.lzyzl.millager.mixin.GameRuleDefaultAccessor;
import org.lzyzl.millager.registry.DeferredHolder;
import org.lzyzl.millager.registry.DeferredRegister;

import static org.lzyzl.millager.Millager.MOD_ID;

public final class MillagerGameRules {

    private static final DeferredRegister<GameRule<?>> GAME_RULES =
            DeferredRegister.create(BuiltInRegistries.GAME_RULE.key(), MOD_ID);

    public static final DeferredHolder<GameRule<?>, GameRule<Boolean>> RAID_DEFENSES =
            GAME_RULES.register(
                    "raid_defenses",
                    () -> createBoolRule(GameRuleCategory.MOBS, MillagerConfig.raidDefenses()));

    public static final DeferredHolder<GameRule<?>, GameRule<Boolean>> RAID_DEFENDERS_RETREAT =
            GAME_RULES.register(
                    "raid_defenders_retreat",
                    () -> createBoolRule(GameRuleCategory.MOBS, MillagerConfig.raidDefendersRetreat()));

    public static final DeferredHolder<GameRule<?>, GameRule<Boolean>> FRIENDLY_FIRE =
            GAME_RULES.register(
                    "friendly_fire",
                    () -> createBoolRule(GameRuleCategory.MOBS, MillagerConfig.friendlyFire()));

    public static final DeferredHolder<GameRule<?>, GameRule<Boolean>> ENABLE_WILDER_PATROLS =
            GAME_RULES.register(
                    "enable_wild_patrols",
                    () -> createBoolRule(GameRuleCategory.SPAWNING, MillagerConfig.enableWildPatrols()));

    private static GameRule<Boolean> createBoolRule(GameRuleCategory category, boolean defaultValue) {
        return new GameRule<>(category, GameRuleType.BOOL, BoolArgumentType.bool(),
                GameRuleTypeVisitor::visitBoolean, Codec.BOOL, b -> b ? 1 : 0, defaultValue, FeatureFlagSet.of());
    }

    public static void initialize() {
        GAME_RULES.register();
    }

    public static void updateDefaults() {
        setDefault(RAID_DEFENSES, MillagerConfig.raidDefenses());
        setDefault(RAID_DEFENDERS_RETREAT, MillagerConfig.raidDefendersRetreat());
        setDefault(FRIENDLY_FIRE, MillagerConfig.friendlyFire());
        setDefault(ENABLE_WILDER_PATROLS, MillagerConfig.enableWildPatrols());
    }

    @SuppressWarnings("unchecked")
    private static void setDefault(DeferredHolder<GameRule<?>, GameRule<Boolean>> holder, boolean defaultValue) {
        ((GameRuleDefaultAccessor<Boolean>) (Object) holder.get()).millager$setDefaultValue(defaultValue);
    }
}
