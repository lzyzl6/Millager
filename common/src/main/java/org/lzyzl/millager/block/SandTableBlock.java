package org.lzyzl.millager.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;

public class SandTableBlock extends HorizontalDirectionalBlock {

    public static final MapCodec<SandTableBlock> CODEC = simpleCodec(SandTableBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(0, 0, 0, 16, 7, 16),      // 底座 + 沙层部分 (base & sand 组合)
            Block.box(2, 7, 2, 11, 11, 7),     // 房屋微缩模型 (house)
            Block.box(2, 7, 11, 7, 12, 15),    // 树木微缩模型 (tree)
            Block.box(11, 7, 10, 15, 9, 14),   // 敌军模型 (enemy)
            Block.box(8, 7, 12, 10, 10, 12),   // 旗帜杆 (flag)
            Block.box(14, 7, 3, 14, 10, 5)     // 旗帜面 (flag)
    );
    private static final VoxelShape SHAPE_EAST = rotateShape(Direction.EAST);
    private static final VoxelShape SHAPE_SOUTH = rotateShape(Direction.SOUTH);
    private static final VoxelShape SHAPE_WEST = rotateShape(Direction.WEST);

    public SandTableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected @NonNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    private static VoxelShape rotateShape(Direction to) {
        VoxelShape[] buffer = {SandTableBlock.SHAPE_NORTH, Shapes.empty() };
        int times = (to.get2DDataValue() - Direction.NORTH.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((x1, y1, z1, x2, y2, z2) -> buffer[1] = Shapes.or(buffer[1],
                    Shapes.box(1 - z2, y1, x1, 1 - z1, y2, x2)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }
        return buffer[0];
    }

    @Override
    public @NonNull VoxelShape getShape(BlockState state, @NonNull BlockGetter world, @NonNull BlockPos pos, @NonNull CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

}
