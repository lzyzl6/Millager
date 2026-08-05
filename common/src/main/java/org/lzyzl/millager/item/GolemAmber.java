package org.lzyzl.millager.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.MillagerEntityTypes;
import org.lzyzl.millager.entity.golem.BeeGolem;

public class GolemAmber extends Item {

    public GolemAmber(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NonNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CANDLE_BREAK, SoundSource.NEUTRAL, 1.0F, 0.5f);
        if (!level.isClientSide()) {
            BeeGolem beeGolem = MillagerEntityTypes.Bee_Golem.get().create(level);
            if (beeGolem == null) return InteractionResultHolder.pass(itemStack);
            Vec3 look = player.getLookAngle();
            beeGolem.setPos(player.getX() + look.x, player.getEyeY() + look.y, player.getZ() + look.z);
            beeGolem.setOwnerUUID(player.getStringUUID());
            level.addFreshEntity(beeGolem);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) itemStack.shrink(1);
        player.swing(hand);
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}
