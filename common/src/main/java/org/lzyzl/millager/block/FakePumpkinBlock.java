package org.lzyzl.millager.block;


import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

public class FakePumpkinBlock extends CarvedPumpkinBlock {

    public FakePumpkinBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull ItemStack getCloneItemStack(@NonNull LevelReader var0, @NonNull BlockPos var1, @NonNull BlockState var2, boolean var3) {
        return new ItemStack(Items.CARVED_PUMPKIN);
    }
}
