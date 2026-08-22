package org.lzyzl.millager.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.lzyzl.millager.Millager;
import org.lzyzl.millager.MillagerGameRules;
import org.lzyzl.millager.behavior.MiscConfig;
import org.lzyzl.millager.behavior.patrol.PatrolConfig;
import org.lzyzl.millager.behavior.raid.DefenderConfig;
import org.lzyzl.millager.util.TargetRelation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class MillagerConfig {

    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .create();

    private static Path configPath;
    private static ConfigData current = new ConfigData();

    private MillagerConfig() {
    }

    public static synchronized void load(Path configDirectory) {
        configPath = configDirectory.resolve("millager.json");
        ConfigData loaded = new ConfigData();

        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                ConfigData parsed = GSON.fromJson(reader, ConfigData.class);
                if (parsed != null) loaded = parsed;
            } catch (Exception exception) {
                Millager.LOGGER.error("Could not read {}, using built-in defaults without overwriting the file", configPath, exception);
                current = normalize(new ConfigData());
                apply(current);
                return;
            }
        }

        current = normalize(loaded);
        apply(current);

        try {
            write(current);
        } catch (IOException exception) {
            Millager.LOGGER.error("Could not write {}", configPath, exception);
        }
    }

    public static synchronized ConfigData copy() {
        return GSON.fromJson(GSON.toJson(current), ConfigData.class);
    }

    public static synchronized boolean save(ConfigData data) {
        ConfigData normalized = normalize(data);

        try {
            write(normalized);
            current = normalized;
            apply(current);
            MillagerGameRules.updateDefaults();
            return true;
        } catch (IOException exception) {
            Millager.LOGGER.error("Could not write {}", configPath, exception);
            return false;
        }
    }

    public static boolean raidDefenses() {
        return current.gameRuleDefaults.raidDefenses;
    }

    public static boolean raidDefendersRetreat() {
        return current.gameRuleDefaults.raidDefendersRetreat;
    }

    public static boolean friendlyFire() {
        return current.gameRuleDefaults.friendlyFire;
    }

    public static boolean enableWildPatrols() {
        return current.gameRuleDefaults.enableWildPatrols;
    }

    public static Optional<TargetRelation> targetRelation(String entityId) {
        if (current.targeting == null || current.targeting.overrides == null) return Optional.empty();
        return Optional.ofNullable(TargetRelation.fromSerializedName(current.targeting.overrides.get(entityId)));
    }

    public static Optional<Boolean> beeGolemOverride(String entityId) {
        if (current.targeting == null || current.targeting.beeGolemOverrides == null) return Optional.empty();
        return Optional.ofNullable(current.targeting.beeGolemOverrides.get(entityId));
    }

    private static ConfigData normalize(ConfigData data) {
        data.comments = loadComments();
        if (data.gameRuleDefaults == null) data.gameRuleDefaults = new GameRuleDefaults();
        if (data.defender == null) data.defender = new Defender();
        if (data.patrol == null) data.patrol = new Patrol();
        if (data.misc == null) data.misc = new Misc();
        if (data.targeting == null) data.targeting = new Targeting();
        if (data.targeting.overrides == null) data.targeting.overrides = new LinkedHashMap<>();
        if (data.targeting.beeGolemOverrides == null) data.targeting.beeGolemOverrides = new LinkedHashMap<>();
        data.targeting.overrides.entrySet().removeIf(entry ->
                entry.getKey() == null || entry.getValue() == null || TargetRelation.fromSerializedName(entry.getValue()) == null);
        data.targeting.beeGolemOverrides.entrySet().removeIf(entry -> entry.getKey() == null || entry.getValue() == null);

        Defender defender = data.defender;
        if (defender.teamName == null || !defender.teamName.matches("[A-Za-z0-9_.+-]{1,16}")) {
            invalid("defender.team_name", defender.teamName, "millager_defense");
            defender.teamName = "millager_defense";
        }
        defender.tickDecrementEasy = clamp("defender.tick_decrement_easy", defender.tickDecrementEasy, 1, 100);
        defender.tickDecrementNormal = clamp("defender.tick_decrement_normal", defender.tickDecrementNormal, 1, 100);
        defender.tickDecrementHard = clamp("defender.tick_decrement_hard", defender.tickDecrementHard, 1, 100);
        defender.timerBaseReal = clamp("defender.timer_base_real", defender.timerBaseReal, 1, 72000);
        defender.timerPerBedReal = clamp("defender.timer_per_bed_real", defender.timerPerBedReal, 0, 72000);
        defender.timerRealMin = clamp("defender.timer_real_min", defender.timerRealMin, 1, 72000);
        defender.timerRealMax = clamp("defender.timer_real_max", defender.timerRealMax, defender.timerRealMin, 72000);
        defender.deployedDisplayTicks = clamp("defender.deployed_display_ticks", defender.deployedDisplayTicks, 0, 12000);
        defender.bedCacheInterval = clamp("defender.bed_cache_interval", defender.bedCacheInterval, 1, 72000);
        defender.maxHeightDiff = clamp("defender.max_height_diff", defender.maxHeightDiff, 0, 384);
        defender.spawnSearchAttempts = clamp("defender.spawn_search_attempts", defender.spawnSearchAttempts, 1, 1000);
        defender.squadMinSize = clamp("defender.squad_min_size", defender.squadMinSize, 1, 5);
        defender.squadMaxSize = clamp("defender.squad_max_size", defender.squadMaxSize, defender.squadMinSize, 5);
        defender.squadCountVariance = clamp("defender.squad_count_variance", defender.squadCountVariance, 0, 100);
        defender.maxSquadsPerWave = clamp("defender.max_squads_per_wave", defender.maxSquadsPerWave, 1, 32);
        defender.squadSpawnMinDistance = clamp("defender.squad_spawn_min_distance", defender.squadSpawnMinDistance, 0, 512);
        defender.squadSpawnMaxDistance = clamp("defender.squad_spawn_max_distance", defender.squadSpawnMaxDistance, defender.squadSpawnMinDistance, 512);
        defender.squadMemberSpawnRadius = clamp("defender.squad_member_spawn_radius", defender.squadMemberSpawnRadius, 0, 32);
        defender.cavalrySpawnClearance = clamp("defender.cavalry_spawn_clearance", defender.cavalrySpawnClearance, 16.0D);
        defender.fastClearFirstRaiderCount = clamp("defender.fast_clear_first_raider_count", defender.fastClearFirstRaiderCount, 1, 100);
        defender.fastClearSecondRaiderCount = clamp("defender.fast_clear_second_raider_count", defender.fastClearSecondRaiderCount, 1, 100);
        defender.fastClearFirstSecondsPerRaider = clamp("defender.fast_clear_first_seconds_per_raider", defender.fastClearFirstSecondsPerRaider, 0, 3600);
        defender.fastClearSecondSecondsPerRaider = clamp("defender.fast_clear_second_seconds_per_raider", defender.fastClearSecondSecondsPerRaider, 0, 3600);
        defender.fastClearRemainingSecondsPerRaider = clamp("defender.fast_clear_remaining_seconds_per_raider", defender.fastClearRemainingSecondsPerRaider, 0, 3600);
        defender.empoweredRaiderCountMultiplierEasy = clamp("defender.empowered_raider_count_multiplier_easy", defender.empoweredRaiderCountMultiplierEasy, 10.0D);
        defender.empoweredRaiderCountMultiplierNormal = clamp("defender.empowered_raider_count_multiplier_normal", defender.empoweredRaiderCountMultiplierNormal, 10.0D);
        defender.empoweredRaiderCountMultiplierHard = clamp("defender.empowered_raider_count_multiplier_hard", defender.empoweredRaiderCountMultiplierHard, 10.0D);
        defender.maxConsecutiveSpawnFailures = clamp("defender.max_consecutive_spawn_failures", defender.maxConsecutiveSpawnFailures, 1, 100);
        defender.spawnFailureRetryTicks = clamp("defender.spawn_failure_retry_ticks", defender.spawnFailureRetryTicks, 0, 72000);

        Patrol patrol = data.patrol;
        patrol.wildTimerBase = clamp("patrol.wild_timer_base", patrol.wildTimerBase, 1, 720000);
        patrol.wildTimerRand = clamp("patrol.wild_timer_rand", patrol.wildTimerRand, 0, 720000);
        patrol.wildPatrolSpawnChance = (float) clamp("patrol.wild_patrol_spawn_chance", patrol.wildPatrolSpawnChance, 1.0D);
        patrol.wildCavalryChance = (float) clamp("patrol.wild_cavalry_chance", patrol.wildCavalryChance, 1.0D);
        patrol.maxNearbyMillagers = clamp("patrol.max_nearby_millagers", patrol.maxNearbyMillagers, 1, 1000);
        patrol.millagerCapRadius = clamp("patrol.millager_cap_radius", patrol.millagerCapRadius, 1, 512);
        patrol.infantryMinSize = clamp("patrol.infantry_min_size", patrol.infantryMinSize, 1, 100);
        patrol.infantryMaxSize = clamp("patrol.infantry_max_size", patrol.infantryMaxSize, patrol.infantryMinSize, 100);
        patrol.cavalryMinSize = clamp("patrol.cavalry_min_size", patrol.cavalryMinSize, 1, 100);
        patrol.cavalryMaxSize = clamp("patrol.cavalry_max_size", patrol.cavalryMaxSize, patrol.cavalryMinSize, 100);
        patrol.spawnMinDist = clamp("patrol.spawn_min_dist", patrol.spawnMinDist, 0, 512);
        patrol.spawnMaxDist = clamp("patrol.spawn_max_dist", patrol.spawnMaxDist, patrol.spawnMinDist, 512);
        patrol.minDaysPlayed = clamp("patrol.min_days_played", patrol.minDaysPlayed, 0, 100000);
        patrol.commandPostScanInterval = clamp("patrol.command_post_scan_interval", patrol.commandPostScanInterval, 1, 72000);
        patrol.structurePatrolDelay = clamp("patrol.structure_patrol_delay", patrol.structurePatrolDelay, 0, 72000);
        patrol.commandPostScanRadius = clamp("patrol.command_post_scan_radius", patrol.commandPostScanRadius, 0, 64);
        patrol.structurePatrolCap = clamp("patrol.structure_patrol_cap", patrol.structurePatrolCap, 1, 1000);
        patrol.structurePatrolCapRadius = clamp("patrol.structure_patrol_cap_radius", patrol.structurePatrolCapRadius, 1, 512);
        patrol.commandPostPatrolSize = clamp("patrol.command_post_patrol_size", patrol.commandPostPatrolSize, 1, 100);
        patrol.commandPostPatrolNearDist = clamp("patrol.command_post_patrol_near_dist", patrol.commandPostPatrolNearDist, 0, 512);
        patrol.ruinedCpPatrolSizeMin = clamp("patrol.ruined_cp_patrol_size_min", patrol.ruinedCpPatrolSizeMin, 1, 100);
        patrol.ruinedCpPatrolSizeMax = clamp("patrol.ruined_cp_patrol_size_max", patrol.ruinedCpPatrolSizeMax, patrol.ruinedCpPatrolSizeMin, 100);
        patrol.ruinedCpPatrolNearDist = clamp("patrol.ruined_cp_patrol_near_dist", patrol.ruinedCpPatrolNearDist, 0, 512);

        Misc misc = data.misc;
        misc.guardVillagerBannerSpawnChance = clamp("misc.guard_villager_banner_spawn_chance", misc.guardVillagerBannerSpawnChance, 0, 100);
        misc.fastHorseDespawnTicks = clamp("misc.fast_horse_despawn_ticks", misc.fastHorseDespawnTicks, 0, 72000);
        misc.mountHorseDespawnTicks = clamp("misc.mount_horse_despawn_ticks", misc.mountHorseDespawnTicks, 0, 72000);
        misc.doctorIronGolemLimit = clamp("misc.doctor_iron_golem_limit", misc.doctorIronGolemLimit, -1, 1000);
        return data;
    }

    private static Map<String, String> loadComments() {
        Map<String, String> comments = new LinkedHashMap<>();

        try (InputStream stream = MillagerConfig.class.getResourceAsStream("/assets/millager/lang/en_us.json")) {
            if (stream == null) {
                Millager.LOGGER.warn("Could not find English translations for config comments");
                return comments;
            }

            JsonObject translations = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            addOverview(comments, translations, "game_rule_defaults");
            addComments(comments, translations, "game_rule_defaults", GameRuleDefaults.class);
            addOverview(comments, translations, "defender");
            addComments(comments, translations, "defender", Defender.class);
            addOverview(comments, translations, "patrol");
            addComments(comments, translations, "patrol", Patrol.class);
            addOverview(comments, translations, "misc");
            addComments(comments, translations, "misc", Misc.class);
            addOverview(comments, translations, "targeting");
        } catch (Exception exception) {
            Millager.LOGGER.warn("Could not load English config comments", exception);
        }

        return comments;
    }

    private static void addOverview(Map<String, String> comments, JsonObject translations, String section) {
        String translationKey = "millager.config.overview." + section;
        if (translations.has(translationKey)) comments.put(section, translations.get(translationKey).getAsString());
    }

    private static void addComments(Map<String, String> comments, JsonObject translations, String section, Class<?> type) {
        for (Field field : type.getFields()) {
            String name = field.getName().replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
            String translationKey = "millager.config.option." + section + "." + name + ".description";
            if (translations.has(translationKey)) comments.put(section + "." + name, translations.get(translationKey).getAsString());
        }
    }

    private static int clamp(String key, int value, int min, int max) {
        int normalized = Math.max(min, Math.min(max, value));
        if (normalized != value) invalid(key, value, normalized);
        return normalized;
    }

    private static double clamp(String key, double value, double max) {
        double normalized = Double.isFinite(value) ? Math.max(0.0, Math.min(max, value)) : 0.0;
        if (Double.compare(normalized, value) != 0) invalid(key, value, normalized);
        return normalized;
    }

    private static void invalid(String key, Object value, Object replacement) {
        Millager.LOGGER.warn("Invalid config value {}={} replaced with {}", key, value, replacement);
    }

    private static void apply(ConfigData data) {
        Defender defender = data.defender;
        DefenderConfig.TEAM_NAME = defender.teamName;
        DefenderConfig.TICK_DECREMENT_EASY = defender.tickDecrementEasy;
        DefenderConfig.TICK_DECREMENT_NORMAL = defender.tickDecrementNormal;
        DefenderConfig.TICK_DECREMENT_HARD = defender.tickDecrementHard;
        DefenderConfig.TIMER_BASE_REAL = defender.timerBaseReal;
        DefenderConfig.TIMER_PER_BED_REAL = defender.timerPerBedReal;
        DefenderConfig.TIMER_REAL_MIN = defender.timerRealMin;
        DefenderConfig.TIMER_REAL_MAX = defender.timerRealMax;
        DefenderConfig.DEPLOYED_DISPLAY_TICKS = defender.deployedDisplayTicks;
        DefenderConfig.BED_CACHE_INTERVAL = defender.bedCacheInterval;
        DefenderConfig.MAX_HEIGHT_DIFF = defender.maxHeightDiff;
        DefenderConfig.SPAWN_SEARCH_ATTEMPTS = defender.spawnSearchAttempts;
        DefenderConfig.SQUAD_MIN_SIZE = defender.squadMinSize;
        DefenderConfig.SQUAD_MAX_SIZE = defender.squadMaxSize;
        DefenderConfig.SQUAD_COUNT_VARIANCE = defender.squadCountVariance;
        DefenderConfig.MAX_SQUADS_PER_WAVE = defender.maxSquadsPerWave;
        DefenderConfig.SQUAD_SPAWN_MIN_DISTANCE = defender.squadSpawnMinDistance;
        DefenderConfig.SQUAD_SPAWN_MAX_DISTANCE = defender.squadSpawnMaxDistance;
        DefenderConfig.SQUAD_MEMBER_SPAWN_RADIUS = defender.squadMemberSpawnRadius;
        DefenderConfig.CAVALRY_SPAWN_CLEARANCE = defender.cavalrySpawnClearance;
        DefenderConfig.FAST_WAVE_FIRST_SEGMENT_SIZE = defender.fastClearFirstRaiderCount;
        DefenderConfig.FAST_WAVE_SECOND_SEGMENT_SIZE = defender.fastClearSecondRaiderCount;
        DefenderConfig.FAST_WAVE_FIRST_SEGMENT_SECONDS = defender.fastClearFirstSecondsPerRaider;
        DefenderConfig.FAST_WAVE_SECOND_SEGMENT_SECONDS = defender.fastClearSecondSecondsPerRaider;
        DefenderConfig.FAST_WAVE_REMAINING_SECONDS = defender.fastClearRemainingSecondsPerRaider;
        DefenderConfig.SURGE_ENEMY_WEIGHT_EASY = defender.empoweredRaiderCountMultiplierEasy;
        DefenderConfig.SURGE_ENEMY_WEIGHT_NORMAL = defender.empoweredRaiderCountMultiplierNormal;
        DefenderConfig.SURGE_ENEMY_WEIGHT_HARD = defender.empoweredRaiderCountMultiplierHard;
        DefenderConfig.MAX_CONSECUTIVE_SPAWN_FAILURES = defender.maxConsecutiveSpawnFailures;
        DefenderConfig.SPAWN_FAILURE_RETRY_TICKS = defender.spawnFailureRetryTicks;

        Patrol patrol = data.patrol;
        PatrolConfig.WILD_TIMER_BASE = patrol.wildTimerBase;
        PatrolConfig.WILD_TIMER_RAND = patrol.wildTimerRand;
        PatrolConfig.WILD_PATROL_SPAWN_CHANCE = patrol.wildPatrolSpawnChance;
        PatrolConfig.WILD_CAVALRY_CHANCE = patrol.wildCavalryChance;
        PatrolConfig.MAX_NEARBY_MILLAGERS = patrol.maxNearbyMillagers;
        PatrolConfig.MILLAGER_CAP_RADIUS = patrol.millagerCapRadius;
        PatrolConfig.INFANTRY_MIN_SIZE = patrol.infantryMinSize;
        PatrolConfig.INFANTRY_MAX_SIZE = patrol.infantryMaxSize;
        PatrolConfig.CAVALRY_MIN_SIZE = patrol.cavalryMinSize;
        PatrolConfig.CAVALRY_MAX_SIZE = patrol.cavalryMaxSize;
        PatrolConfig.SPAWN_MIN_DIST = patrol.spawnMinDist;
        PatrolConfig.SPAWN_MAX_DIST = patrol.spawnMaxDist;
        PatrolConfig.MIN_DAYS_PLAYED = patrol.minDaysPlayed;
        PatrolConfig.COMMAND_POST_SCAN_INTERVAL = patrol.commandPostScanInterval;
        PatrolConfig.STRUCTURE_PATROL_DELAY = patrol.structurePatrolDelay;
        PatrolConfig.COMMAND_POST_SCAN_RADIUS = patrol.commandPostScanRadius;
        PatrolConfig.STRUCTURE_PATROL_CAP = patrol.structurePatrolCap;
        PatrolConfig.STRUCTURE_PATROL_CAP_RADIUS = patrol.structurePatrolCapRadius;
        PatrolConfig.COMMAND_POST_PATROL_SIZE = patrol.commandPostPatrolSize;
        PatrolConfig.COMMAND_POST_PATROL_NEAR_DIST = patrol.commandPostPatrolNearDist;
        PatrolConfig.RUINED_CP_PATROL_SIZE_MIN = patrol.ruinedCpPatrolSizeMin;
        PatrolConfig.RUINED_CP_PATROL_SIZE_MAX = patrol.ruinedCpPatrolSizeMax;
        PatrolConfig.RUINED_CP_PATROL_NEAR_DIST = patrol.ruinedCpPatrolNearDist;

        Misc misc = data.misc;
        MiscConfig.GUARD_VILLAGER_BANNER_SPAWN_CHANCE = misc.guardVillagerBannerSpawnChance;
        MiscConfig.ENABLE_GOETY_RAIDS = misc.enableGoetyRaids;
        MiscConfig.GENERATE_COMMAND_POSTS = misc.generateCommandPosts;
        MiscConfig.GENERATE_RUINED_COMMAND_POSTS = misc.generateRuinedCommandPosts;
        MiscConfig.GENERATE_FLOATING_ISLANDS = misc.generateFloatingIslands;
        MiscConfig.GENERATE_STRONG_ROOMS = misc.generateStrongRooms;
        MiscConfig.GENERATE_TRADING_HALLS = misc.generateTradingHalls;
        MiscConfig.GENERATE_INFANTRY_HUTS = misc.generateInfantryHuts;
        MiscConfig.FAST_HORSE_DESPAWN_TICKS = misc.fastHorseDespawnTicks;
        MiscConfig.MOUNT_HORSE_DESPAWN_TICKS = misc.mountHorseDespawnTicks;
        MiscConfig.DOCTOR_IRON_GOLEM_LIMIT = misc.doctorIronGolemLimit;
    }

    private static void write(ConfigData data) throws IOException {
        Files.createDirectories(configPath.getParent());
        Path temporary = configPath.resolveSibling(configPath.getFileName() + ".tmp");

        try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
            GSON.toJson(data, writer);
        }

        try {
            Files.move(temporary, configPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporary, configPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static final class ConfigData {
        @SerializedName("_comments")
        public Map<String, String> comments = new LinkedHashMap<>();
        @SerializedName(value = "game_rule_defaults", alternate = "game_rules")
        public GameRuleDefaults gameRuleDefaults = new GameRuleDefaults();
        public Defender defender = new Defender();
        public Patrol patrol = new Patrol();
        public Misc misc = new Misc();
        public Targeting targeting = new Targeting();
    }

    public static final class GameRuleDefaults {
        public boolean raidDefenses = true;
        public boolean raidDefendersRetreat = true;
        public boolean friendlyFire = false;
        public boolean enableWildPatrols = false;
    }

    public static final class Defender {
        public String teamName = "millager_defense";
        public int tickDecrementEasy = 4;
        public int tickDecrementNormal = 2;
        public int tickDecrementHard = 1;
        public int timerBaseReal = 2100;
        public int timerPerBedReal = 175;
        public int timerRealMin = 1400;
        public int timerRealMax = 6300;
        public int deployedDisplayTicks = 100;
        public int bedCacheInterval = 1200;
        public int maxHeightDiff = 48;
        public int spawnSearchAttempts = 20;
        public int squadMinSize = 3;
        public int squadMaxSize = 5;
        public int squadCountVariance = 1;
        public int maxSquadsPerWave = 4;
        public int squadSpawnMinDistance = 36;
        public int squadSpawnMaxDistance = 52;
        public int squadMemberSpawnRadius = 4;
        public double cavalrySpawnClearance = 1.0D;
        @SerializedName(value = "fast_clear_first_raider_count", alternate = {"fast_clear_first_enemy_count", "fast_wave_first_segment_size"})
        public int fastClearFirstRaiderCount = 4;
        @SerializedName(value = "fast_clear_second_raider_count", alternate = {"fast_clear_second_enemy_count", "fast_wave_second_segment_size"})
        public int fastClearSecondRaiderCount = 4;
        @SerializedName(value = "fast_clear_first_seconds_per_raider", alternate = {"fast_clear_first_seconds_per_enemy", "fast_wave_first_segment_seconds"})
        public int fastClearFirstSecondsPerRaider = 12;
        @SerializedName(value = "fast_clear_second_seconds_per_raider", alternate = {"fast_clear_second_seconds_per_enemy", "fast_wave_second_segment_seconds"})
        public int fastClearSecondSecondsPerRaider = 7;
        @SerializedName(value = "fast_clear_remaining_seconds_per_raider", alternate = {"fast_clear_remaining_seconds_per_enemy", "fast_wave_remaining_seconds"})
        public int fastClearRemainingSecondsPerRaider = 4;
        @SerializedName(value = "empowered_raider_count_multiplier_easy", alternate = {"empowered_enemy_count_multiplier_easy", "surge_enemy_weight_easy"})
        public double empoweredRaiderCountMultiplierEasy = 0.5D;
        @SerializedName(value = "empowered_raider_count_multiplier_normal", alternate = {"empowered_enemy_count_multiplier_normal", "surge_enemy_weight_normal"})
        public double empoweredRaiderCountMultiplierNormal = 0.4D;
        @SerializedName(value = "empowered_raider_count_multiplier_hard", alternate = {"empowered_enemy_count_multiplier_hard", "surge_enemy_weight_hard"})
        public double empoweredRaiderCountMultiplierHard = 0.2D;
        public int maxConsecutiveSpawnFailures = 3;
        public int spawnFailureRetryTicks = 20;
    }

    public static final class Patrol {
        public int wildTimerBase = 18000;
        public int wildTimerRand = 1200;
        public float wildPatrolSpawnChance = 0.25F;
        public float wildCavalryChance = 0.5F;
        public int maxNearbyMillagers = 16;
        public int millagerCapRadius = 48;
        public int infantryMinSize = 5;
        public int infantryMaxSize = 8;
        public int cavalryMinSize = 4;
        public int cavalryMaxSize = 7;
        public int spawnMinDist = 24;
        public int spawnMaxDist = 48;
        public int minDaysPlayed = 3;
        public int commandPostScanInterval = 100;
        public int structurePatrolDelay = 20;
        public int commandPostScanRadius = 6;
        public int structurePatrolCap = 10;
        public int structurePatrolCapRadius = 64;
        public int commandPostPatrolSize = 3;
        public int commandPostPatrolNearDist = 24;
        public int ruinedCpPatrolSizeMin = 5;
        public int ruinedCpPatrolSizeMax = 7;
        public int ruinedCpPatrolNearDist = 24;
    }

    public static final class Misc {
        @SerializedName(value = "guard_villager_banner_spawn_chance", alternate = "guards_spawn_with_village_banners")
        @JsonAdapter(PercentageAdapter.class)
        public int guardVillagerBannerSpawnChance = 5;
        public boolean enableGoetyRaids = true;
        public boolean generateCommandPosts = true;
        public boolean generateRuinedCommandPosts = true;
        public boolean generateFloatingIslands = true;
        public boolean generateStrongRooms = true;
        public boolean generateTradingHalls = true;
        public boolean generateInfantryHuts = true;
        public int fastHorseDespawnTicks = 140;
        public int mountHorseDespawnTicks = 900;
        public int doctorIronGolemLimit = 3;
    }

    public static final class PercentageAdapter extends TypeAdapter<Integer> {
        @Override
        public void write(JsonWriter out, Integer value) throws IOException {
            out.value(value);
        }

        @Override
        public Integer read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.BOOLEAN) return in.nextBoolean() ? 5 : 0;
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return 5;
            }
            return in.nextInt();
        }
    }

    public static final class Targeting {
        public Map<String, String> overrides = new LinkedHashMap<>();
        public Map<String, Boolean> beeGolemOverrides = new LinkedHashMap<>();
    }
}
