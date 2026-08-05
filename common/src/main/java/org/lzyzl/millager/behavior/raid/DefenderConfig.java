package org.lzyzl.millager.behavior.raid;

public final class DefenderConfig {
    public static volatile String TEAM_NAME = "millager_defense";

    public static volatile int TICK_DECREMENT_EASY = 4;
    public static volatile int TICK_DECREMENT_NORMAL = 2;
    public static volatile int TICK_DECREMENT_HARD = 1;

    public static volatile int TIMER_BASE_REAL = 2100;
    public static volatile int TIMER_PER_BED_REAL = 175;
    public static volatile int TIMER_REAL_MIN = 1400;
    public static volatile int TIMER_REAL_MAX = 6300;

    public static volatile int DEPLOYED_DISPLAY_TICKS = 100;
    public static volatile int BED_CACHE_INTERVAL = 1200;
    public static volatile int MAX_HEIGHT_DIFF = 48;
    public static volatile int SPAWN_SEARCH_ATTEMPTS = 20;

    public static volatile int SQUAD_MIN_SIZE = 3;
    public static volatile int SQUAD_MAX_SIZE = 5;
    public static volatile int SQUAD_COUNT_VARIANCE = 1;
    public static volatile int MAX_SQUADS_PER_WAVE = 4;
    public static volatile int SQUAD_SPAWN_MIN_DISTANCE = 36;
    public static volatile int SQUAD_SPAWN_MAX_DISTANCE = 52;
    public static volatile int SQUAD_MEMBER_SPAWN_RADIUS = 4;
    public static volatile double CAVALRY_SPAWN_CLEARANCE = 1.0D;

    public static volatile int FAST_WAVE_FIRST_SEGMENT_SIZE = 4;
    public static volatile int FAST_WAVE_SECOND_SEGMENT_SIZE = 4;
    public static volatile int FAST_WAVE_FIRST_SEGMENT_SECONDS = 12;
    public static volatile int FAST_WAVE_SECOND_SEGMENT_SECONDS = 7;
    public static volatile int FAST_WAVE_REMAINING_SECONDS = 4;
    public static volatile double SURGE_ENEMY_WEIGHT_EASY = 0.5D;
    public static volatile double SURGE_ENEMY_WEIGHT_NORMAL = 0.4D;
    public static volatile double SURGE_ENEMY_WEIGHT_HARD = 0.2D;
}
