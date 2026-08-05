package org.lzyzl.millager.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

public class LiquorCauldronBlock extends LayeredCauldronBlock {

    public LiquorCauldronBlock(Properties properties) {
        super(properties, precipitation -> false, CauldronInteraction.EMPTY);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 3));
    }

    @Override
    public boolean isFull(@NonNull BlockState blockState) {
        return true;
    }

    @Override
    public @NonNull ItemStack getCloneItemStack(@NonNull BlockGetter var0, @NonNull BlockPos var1, @NonNull BlockState var2) {
        return new ItemStack(Items.CAULDRON);
    }

    @Override
    public void entityInside(@NonNull BlockState blockState, @NonNull Level level, @NonNull BlockPos blockPos, Entity entity) {
        if(entity.isOnFire()) {
            entity.setRemainingFireTicks(8 * 20);
        }
    }


}
