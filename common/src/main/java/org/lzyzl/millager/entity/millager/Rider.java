package org.lzyzl.millager.entity.millager;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.NonNull;

public interface Rider {

    void createMount(ServerLevelAccessor level, EntitySpawnReason spawnReason, SpawnGroupData spawnGroupData);

    /**
     * 指定最低等级为皮革，伪随机生成马铠。<br>
     * 等级0-5分别对应皮革、铜、铁、金、钻石、下界合金。<br>
     * @param random 随机源
     * @param maxLevel 生成马铠的最高等级
     */
    static Item getRandomHorseArmor(@NonNull RandomSource random, int maxLevel) {
        return getRandomHorseArmor(random, 0, maxLevel);
    }

    /**
     * 伪随机生成马铠。<br>
     * 等级0-5分别对应皮革、铜、铁、金、钻石、下界合金。<br>
     * @param random 随机源
     * @param minLevel 生成马铠的最低等级
     * @param maxLevel 生成马铠的最高等级
    */
    static Item getRandomHorseArmor(RandomSource random, int minLevel, int maxLevel) {
        int i = random.nextIntBetweenInclusive(minLevel, maxLevel);
        return switch (i) {
            case 1 -> Items.COPPER_HORSE_ARMOR;
            case 2 -> Items.IRON_HORSE_ARMOR;
            case 3 -> Items.GOLDEN_HORSE_ARMOR;
            case 4 -> Items.DIAMOND_HORSE_ARMOR;
            case 5 -> Items.NETHERITE_HORSE_ARMOR;
            default -> Items.LEATHER_HORSE_ARMOR;
        };
    }
}
