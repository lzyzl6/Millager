package org.lzyzl.millager.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class Liquor extends PotionItem {

    public Liquor(Properties properties) {
        super(properties);
    }

    public @NonNull ItemStack finishUsingItem(@NonNull ItemStack itemStack, @NonNull Level level, @NonNull LivingEntity livingEntity) {
        livingEntity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 1800));
        return super.finishUsingItem(itemStack, level, livingEntity);
    }
}
