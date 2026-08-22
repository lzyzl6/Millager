package org.lzyzl.millager.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.entity.projectile.TNTOnAStick;

public class TNTOnAStickItem extends Item {

    public TNTOnAStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull UseAnim getUseAnimation(@NonNull ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(@NonNull ItemStack stack, @NonNull LivingEntity entity) {
        return 72000;
    }

    @Override
    public void releaseUsing(@NonNull ItemStack stack, @NonNull Level level, @NonNull LivingEntity user, int timeLeft) {
        if (!(user instanceof Player player)) return;

        int duration = this.getUseDuration(stack, user) - timeLeft;
        float power = getPowerForTime(duration);

        if (power >= 0.1f) {
            if (!level.isClientSide()) {
                ItemStack itemStack = new ItemStack(this);
                TNTOnAStick tntOnAStick = new TNTOnAStick(level, player, itemStack);

                tntOnAStick.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 2.2F, 1.0F);
                level.addFreshEntity(tntOnAStick);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TNT_PRIMED, SoundSource.NEUTRAL, 0.5F, 1.0F);

           stack.consume(1, player);
        }
    }

    public static float getPowerForTime(int ticks) {
        float f = (float)ticks / 20.0f;
        f = (f * f + f * 2.0f) / 3.0f;
        return Math.min(f, 1.0f);
    }

    @Override
    public @NonNull InteractionResultHolder<ItemStack> use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand interactionHand) {
        player.startUsingItem(interactionHand);
        return InteractionResultHolder.consume(player.getItemInHand(interactionHand));
    }
}
