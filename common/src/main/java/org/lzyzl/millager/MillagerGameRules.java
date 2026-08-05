package org.lzyzl.millager;

import net.minecraft.world.level.GameRules;
import org.lzyzl.millager.config.MillagerConfig;

public final class MillagerGameRules {

    public static void initialize() {
    }

    public static final GameRules.Key<GameRules.BooleanValue> RAID_DEFENSES = registerBool("millager:raid_defenses", GameRules.Category.MOBS, MillagerConfig.raidDefenses());
    public static final GameRules.Key<GameRules.BooleanValue> RAID_DEFENDERS_RETREAT = registerBool("millager:raid_defenders_retreat", GameRules.Category.MOBS, MillagerConfig.raidDefendersRetreat());
    public static final GameRules.Key<GameRules.BooleanValue> FRIENDLY_FIRE = registerBool("millager:friendly_fire", GameRules.Category.MOBS, MillagerConfig.friendlyFire());
    public static final GameRules.Key<GameRules.BooleanValue> ENABLE_WILDER_PATROLS = registerBool("millager:enable_wild_patrols", GameRules.Category.SPAWNING, MillagerConfig.enableWildPatrols());

    private static GameRules.Key<GameRules.BooleanValue> registerBool(String name, GameRules.Category category, boolean defaultValue) {
        GameRules.Key<GameRules.BooleanValue> key = new GameRules.Key<>(name, category);
        GameRules.Type<GameRules.BooleanValue> type = GameRules.BooleanValue.create(defaultValue);
        GameRules.GAME_RULE_TYPES.put(key, type);
        return key;
    }

    public static void updateDefaults() {
        GameRules.GAME_RULE_TYPES.put(RAID_DEFENSES, GameRules.BooleanValue.create(MillagerConfig.raidDefenses()));
        GameRules.GAME_RULE_TYPES.put(RAID_DEFENDERS_RETREAT, GameRules.BooleanValue.create(MillagerConfig.raidDefendersRetreat()));
        GameRules.GAME_RULE_TYPES.put(FRIENDLY_FIRE, GameRules.BooleanValue.create(MillagerConfig.friendlyFire()));
        GameRules.GAME_RULE_TYPES.put(ENABLE_WILDER_PATROLS, GameRules.BooleanValue.create(MillagerConfig.enableWildPatrols()));
    }
}
