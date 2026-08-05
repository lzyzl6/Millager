package org.lzyzl.millager.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.MillagerSounds;

public class Liquor extends Item {

    public Liquor(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull ItemStack finishUsingItem(@NonNull ItemStack itemStack, @NonNull Level level, @NonNull LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(itemStack, level, livingEntity);
        Player player = livingEntity instanceof Player ? (Player) livingEntity : null;
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, itemStack);
        }
        if (!level.isClientSide) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 1800));
        }
        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        if (player == null || !player.getAbilities().instabuild) {
            if (result.isEmpty()) {
                return new ItemStack(Items.GLASS_BOTTLE);
            }
            if (player != null && !player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
                player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
            }
        }
        livingEntity.gameEvent(GameEvent.DRINK);
        return result;
    }

    @Override
    public int getUseDuration(@NonNull ItemStack itemStack) {
        return 50;
    }

    @Override
    public @NonNull UseAnim getUseAnimation(@NonNull ItemStack itemStack) {
        return UseAnim.DRINK;
    }

    @Override
    public @NonNull SoundEvent getDrinkingSound() {
        return MillagerSounds.QUAFFING.value();
    }

    @Override
    public @NonNull SoundEvent getEatingSound() {
        return MillagerSounds.QUAFFING.value();
    }

    @Override
    public @NonNull InteractionResultHolder<ItemStack> use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }
}
