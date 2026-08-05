package org.lzyzl.millager.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.block.entity.TimedFireBlockEntity;

import java.util.Map;
import java.util.function.Function;

public class TimedFireBlock extends BaseFireBlock implements EntityBlock {

    public static final MapCodec<TimedFireBlock> CODEC = simpleCodec(TimedFireBlock::new);
    public static final IntegerProperty AGE;
    public static final BooleanProperty NORTH;
    public static final BooleanProperty EAST;
    public static final BooleanProperty SOUTH;
    public static final BooleanProperty WEST;
    public static final BooleanProperty UP;
    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION;
    private final Function<BlockState, VoxelShape> shapes;

    public TimedFireBlock(Properties properties) {
        super(properties, 2.0f);
        this.registerDefaultState((this.stateDefinition.any().setValue(AGE, 0).setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false)).setValue(UP, false));
        this.shapes = this.makeShapes();
    }

    private Function<BlockState, VoxelShape> makeShapes() {
        Map<Direction, VoxelShape> map = Shapes.rotateAll(Block.boxZ(16.0F, 0.0F, 1.0F));
        return this.getShapeForEachState((blockState) -> {
            VoxelShape voxelShape = Shapes.empty();

            for(Map.Entry<Direction, BooleanProperty> entry : PROPERTY_BY_DIRECTION.entrySet()) {
                if (blockState.getValue(entry.getValue())) {
                    voxelShape = Shapes.or(voxelShape, map.get(entry.getKey()));
                }
            }

            return voxelShape.isEmpty() ? SHAPE : voxelShape;
        }, AGE);
    }

    @Override
    protected @NonNull MapCodec<? extends BaseFireBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NonNull BlockState updateShape(BlockState blockState, @NonNull LevelReader levelReader, @NonNull ScheduledTickAccess scheduledTickAccess, @NonNull BlockPos blockPos, @NonNull Direction direction, @NonNull BlockPos blockPos2, @NonNull BlockState blockState2, @NonNull RandomSource randomSource) {
        return this.getStateWithAge(levelReader, blockPos, blockState.getValue(AGE)) ;
    }

    @Override
    protected @NonNull VoxelShape getShape(@NonNull BlockState blockState, @NonNull BlockGetter blockGetter, @NonNull BlockPos blockPos, @NonNull CollisionContext collisionContext) {
        return this.shapes.apply(blockState);
    }

    private BlockState getStateWithAge(LevelReader levelReader, BlockPos blockPos, int i) {
        BlockState blockState = getState(levelReader, blockPos);
        return blockState.is(Blocks.FIRE) ? blockState.setValue(AGE, i) : blockState;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, NORTH, EAST, SOUTH, WEST, UP);
    }

    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new TimedFireBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NonNull BlockState state, @NonNull BlockEntityType<T> type) {
        return level.isClientSide() ? null : (l, p, s, be) -> ((TimedFireBlockEntity)be).tick(l, p);
    }

    @Override
    public boolean canSurvive(@NonNull BlockState state, @NonNull LevelReader level, @NonNull BlockPos pos) {
        return true;
    }

    @Override
    protected boolean canBurn(@NonNull BlockState blockState) {
        return true;
    }

    static {
        AGE = BlockStateProperties.AGE_15;
        NORTH = PipeBlock.NORTH;
        EAST = PipeBlock.EAST;
        SOUTH = PipeBlock.SOUTH;
        WEST = PipeBlock.WEST;
        UP = PipeBlock.UP;
        PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter((entry) -> entry.getKey() != Direction.DOWN).collect(Util.toMap());
    }
}