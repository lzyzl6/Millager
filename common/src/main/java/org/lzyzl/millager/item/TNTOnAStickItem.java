package org.lzyzl.millager.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.entity.projectile.TNTOnAStick;

public class TNTOnAStickItem extends Item implements ProjectileItem {


    public TNTOnAStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull ItemUseAnimation getUseAnimation(@NonNull ItemStack stack) {
        return ItemUseAnimation.TRIDENT;
    }

    @Override
    public int getUseDuration(@NonNull ItemStack stack, @NonNull LivingEntity user) {
        return 72000;
    }

    @Override
    public boolean releaseUsing(@NonNull ItemStack stack, @NonNull Level level, @NonNull LivingEntity user, int timeLeft) {
        if (!(user instanceof Player player)) return false;

        int duration = this.getUseDuration(stack, user) - timeLeft;
        float power = getPowerForTime(duration);

        if (power >= 0.1f) {
            if (!level.isClientSide()) {
                ItemStack itemStack = new ItemStack(this);
                TNTOnAStick tntOnAStick = new TNTOnAStick(level, player , itemStack);

                tntOnAStick.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 2.2F, 1.0F);
                level.addFreshEntity(tntOnAStick);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TNT_PRIMED, SoundSource.NEUTRAL, 0.5F, 1.0F);

           stack.consume(1, player);
        }
        return true;
    }

    public static float getPowerForTime(int ticks) {
        float f = (float)ticks / 20.0f;
        f = (f * f + f * 2.0f) / 3.0f;
        return Math.min(f, 1.0f);
    }

    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand interactionHand) {
        player.startUsingItem(interactionHand);
        return InteractionResult.CONSUME;

    }


    public @NonNull Projectile asProjectile(@NonNull Level level, Position position, @NonNull ItemStack itemStack, @NonNull Direction direction) {
        return new TNTOnAStick(level, position.x(), position.y(), position.z(), itemStack);
    }

}
