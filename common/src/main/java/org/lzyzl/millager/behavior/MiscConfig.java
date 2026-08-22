package org.lzyzl.millager.behavior;

public final class MiscConfig {
    public static volatile boolean GENERATE_COMMAND_POSTS = true;
    public static volatile boolean GENERATE_RUINED_COMMAND_POSTS = true;
    public static volatile boolean GENERATE_FLOATING_ISLANDS = true;
    public static volatile boolean GENERATE_STRONG_ROOMS = true;
    public static volatile boolean GENERATE_TRADING_HALLS = true;
    public static volatile boolean GENERATE_INFANTRY_HUTS = true;
    public static volatile int GUARD_VILLAGER_BANNER_SPAWN_CHANCE = 5;
    public static volatile int FAST_HORSE_DESPAWN_TICKS = 140;
    public static volatile int MOUNT_HORSE_DESPAWN_TICKS = 900;
    public static volatile int DOCTOR_IRON_GOLEM_LIMIT = 3;

    public static boolean shouldGenerateStructure(String path) {
        return switch (path) {
            case "command_post" -> GENERATE_COMMAND_POSTS;
            case "ruined_command_post" -> GENERATE_RUINED_COMMAND_POSTS;
            case "floating_island" -> GENERATE_FLOATING_ISLANDS;
            case "strong_room" -> GENERATE_STRONG_ROOMS;
            case "trading_hall" -> GENERATE_TRADING_HALLS;
            default -> true;
        };
    }
}
