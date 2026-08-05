package org.lzyzl.millager.behavior.patrol;

public final class PatrolConfig {

    public static volatile int WILD_TIMER_BASE = 18000;
    public static volatile int WILD_TIMER_RAND = 1200;
    public static volatile float WILD_PATROL_SPAWN_CHANCE = 0.25f;
    public static volatile float WILD_CAVALRY_CHANCE = 0.5f;

    public static volatile int MAX_NEARBY_MILLAGERS = 16;
    public static volatile int MILLAGER_CAP_RADIUS = 48;

    public static volatile int INFANTRY_MIN_SIZE = 5;
    public static volatile int INFANTRY_MAX_SIZE = 8;
    public static volatile int CAVALRY_MIN_SIZE = 4;
    public static volatile int CAVALRY_MAX_SIZE = 7;

    public static volatile int SPAWN_MIN_DIST = 24;
    public static volatile int SPAWN_MAX_DIST = 48;

    public static volatile int MIN_DAYS_PLAYED = 3;

    public static volatile int COMMAND_POST_SCAN_INTERVAL = 100;
    public static volatile int STRUCTURE_PATROL_DELAY = 20;
    public static volatile int COMMAND_POST_SCAN_RADIUS = 6;
    public static volatile int STRUCTURE_PATROL_CAP = 10;
    public static volatile int STRUCTURE_PATROL_CAP_RADIUS = 64;
    public static volatile int COMMAND_POST_PATROL_SIZE = 3;
    public static volatile int COMMAND_POST_PATROL_NEAR_DIST = 24;

    public static volatile int RUINED_CP_PATROL_SIZE_MIN = 5;
    public static volatile int RUINED_CP_PATROL_SIZE_MAX = 7;
    public static volatile int RUINED_CP_PATROL_NEAR_DIST = 24;
}
