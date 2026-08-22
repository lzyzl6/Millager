package org.lzyzl.millager.entity.millager;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.NonNull;

public interface Rider {

    static void createMount(Mob rider, ServerLevelAccessor level, MobSpawnType spawnReason, SpawnGroupData spawnGroupData,
                            double minSpeed, double minHealth, int minArmorLevel, int maxArmorLevel) {
        Horse horse = EntityType.HORSE.create(level.getLevel());
        if (horse == null) return;
        horse.moveTo(rider.getX(), rider.getY(), rider.getZ(), rider.getYRot(), rider.getXRot());
        horse.finalizeSpawn(level, level.getCurrentDifficultyAt(horse.blockPosition()), spawnReason, spawnGroupData, null);
        setMinimumBaseValue(horse.getAttribute(Attributes.MOVEMENT_SPEED), minSpeed);
        setMinimumBaseValue(horse.getAttribute(Attributes.MAX_HEALTH), minHealth);
        horse.setHealth(horse.getMaxHealth());
        horse.inventory.setItem(1, new ItemStack(getRandomHorseArmor(level.getRandom(), minArmorLevel, maxArmorLevel)));
        horse.setTamed(true);
        horse.addTag("millager_mount");
        rider.startRiding(horse);
        level.addFreshEntity(horse);
    }

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

    private static void setMinimumBaseValue(AttributeInstance attribute, double minimum) {
        if (attribute != null && attribute.getBaseValue() < minimum) attribute.setBaseValue(minimum);
    }
}
