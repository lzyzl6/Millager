package org.lzyzl.millager.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.entity.projectile.MolotovCocktailPlus;

public class MolotovCocktailPlusItem extends Item {
    public static float PROJECTILE_SHOOT_POWER = 1.5F;

    public MolotovCocktailPlusItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NonNull InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SPLASH_POTION_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (level instanceof ServerLevel serverLevel) {
            MolotovCocktailPlus projectile = new MolotovCocktailPlus(level, player, itemStack);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, PROJECTILE_SHOOT_POWER, 1.0F);
            serverLevel.addFreshEntity(projectile);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) itemStack.shrink(1);
        player.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}
