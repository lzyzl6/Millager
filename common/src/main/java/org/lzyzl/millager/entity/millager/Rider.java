package org.lzyzl.millager.entity.millager;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.NonNull;

public interface Rider {

    void createMount(ServerLevelAccessor level, MobSpawnType spawnReason, SpawnGroupData spawnGroupData);

    static Item getRandomHorseArmor(@NonNull RandomSource random, int maxLevel) {
        return getRandomHorseArmor(random, 0, maxLevel);
    }

    static Item getRandomHorseArmor(RandomSource random, int minLevel, int maxLevel) {
        int i = random.nextIntBetweenInclusive(minLevel, maxLevel);
        return switch (i) {
            case 1 -> Items.IRON_HORSE_ARMOR;
            case 2 -> Items.GOLDEN_HORSE_ARMOR;
            case 3 -> Items.DIAMOND_HORSE_ARMOR;
            default -> Items.LEATHER_HORSE_ARMOR;
        };
    }
}
