package org.lzyzl.millager.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.MillagerItems;

public class CrackedHealingTotem extends Item {

    public CrackedHealingTotem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(@NonNull ItemStack itemStack, @NonNull Level level, @NonNull Entity entity, int slotId, boolean isSelected) {
        if (level instanceof ServerLevel serverLevel) {
            if (entity instanceof LivingEntity living && living.getAttribute(Attributes.MAX_HEALTH) != null && living.getHealth() < living.getMaxHealth()) {
                if (serverLevel.getGameTime() % (serverLevel.getDifficulty().getId() * 20L) == 0) {
                    living.setHealth(living.getHealth() + 1);
                    if (serverLevel.getRandom().nextInt(250 - serverLevel.getDifficulty().getId() * 33) == 0) {
                        itemStack.shrink(1);
                        serverLevel.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.ITEM_BREAK, SoundSource.NEUTRAL, 1.0F, 1.0F);
                        if (living instanceof Player player) player.getInventory().add(MillagerItems.damagedTotemOfHealing.get().getDefaultInstance());
                    }
                }
            }
        }
    }
}
