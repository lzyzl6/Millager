package org.lzyzl.millager.item;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Elixir extends Item {

    public Elixir(Properties properties) {
        super(properties);
    }

    public @NonNull ItemStack finishUsingItem(@NonNull ItemStack itemStack, @NonNull Level level, @NonNull LivingEntity livingEntity) {
        if (!level.isClientSide()) {
            var activeEffects = livingEntity.getActiveEffects();

            if (!activeEffects.isEmpty()) {
                List<Holder<MobEffect>> toRemove = new ArrayList<>(activeEffects.size());

                for (var instance : activeEffects) {
                    if (instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                        toRemove.add(instance.getEffect());
                    }
                }

                if (!toRemove.isEmpty()) {
                    for (var effectHolder : toRemove) {
                        livingEntity.removeEffect(effectHolder);
                    }
                }
            }
        }
        return super.finishUsingItem(itemStack, level, livingEntity);
    }
}
