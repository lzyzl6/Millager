package org.lzyzl.millager.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.MillagerBlocks;
import org.lzyzl.millager.block.entity.HeadBlockEntity;

import java.util.Map;

public abstract class AbstractWallHeadBlock extends WallSkullBlock {

    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.NORTH, Block.box(4.0, 4.0, 8.0, 12.0, 14.0, 16.0),
            Direction.SOUTH, Block.box(4.0, 4.0, 0.0, 12.0, 14.0, 8.0),
            Direction.EAST, Block.box(0.0, 4.0, 4.0, 8.0, 14.0, 12.0),
            Direction.WEST, Block.box(8.0, 4.0, 4.0, 16.0, 14.0, 12.0)
    );

    public @NonNull VoxelShape getShape(BlockState blockState, @NonNull BlockGetter blockGetter, @NonNull BlockPos blockPos, @NonNull CollisionContext collisionContext) {
        return SHAPES.get(blockState.getValue(FACING));
    }

    public AbstractWallHeadBlock(SkullBlock.Type type, Properties properties) {
        super(type, properties);
    }

    @Override
    public @NonNull BlockEntity newBlockEntity(@NonNull BlockPos blockPos, @NonNull BlockState blockState) {
        return new HeadBlockEntity(blockPos, blockState);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(@NonNull Level level,
                                                                             @NonNull BlockState state,
                                                                             @NonNull BlockEntityType<T> type) {
        return createTickerHelper(type, MillagerBlocks.HEAD_BLOCK_ENTITY.get(), HeadBlockEntity::animation);
    }
}
