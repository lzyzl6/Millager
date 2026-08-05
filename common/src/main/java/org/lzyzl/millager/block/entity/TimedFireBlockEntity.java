package org.lzyzl.millager.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.MillagerBlocks;

public class TimedFireBlockEntity extends BlockEntity {

    private int timer = 2400;

    public TimedFireBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(MillagerBlocks.TIMED_FIRE_ENTITY.get(), blockPos, blockState);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Timer", this.timer);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        this.timer = input.getIntOr("Timer", 2400);
    }

    public void tick(Level level, BlockPos pos) {
        if (--timer <= 0) {
            level.removeBlock(pos, false);
        }
    }
}
