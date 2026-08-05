package org.lzyzl.millager.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
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
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {

        BeeGolem beeGolem = MillagerEntityTypes.Bee_Golem.get().create(level, EntitySpawnReason.TRIGGERED);

        if (beeGolem != null) {
            Vec3 vec3 = player.getHeadLookAngle();
            beeGolem.setPos(player.getX() + vec3.x, player.getEyeY() + vec3.y, player.getZ() + vec3.z);
            beeGolem.setOwnerUUID(player.getStringUUID());

            level.addFreshEntity(beeGolem);
            player.awardStat(Stats.ITEM_USED.get(this));
            ItemStack itemStack = player.getItemInHand(hand);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CANDLE_BREAK, SoundSource.NEUTRAL, 1.0F, 0.5f);
            itemStack.consume(1, player);
            player.swing(player.getUsedItemHand());
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

}