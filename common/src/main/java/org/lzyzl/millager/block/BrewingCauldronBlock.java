package org.lzyzl.millager.block;


import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.MillagerBlocks;
import org.lzyzl.millager.block.entity.BrewingCauldronBlockEntity;

import java.util.List;


public class BrewingCauldronBlock extends BaseEntityBlock {

    public static final MapCodec<BrewingCauldronBlock> CODEC = simpleCodec(BrewingCauldronBlock::new);
    private static final VoxelShape SHAPE_INSIDE = Block.column(12.0F, 4.0F, 16.0F);
    protected static final VoxelShape SHAPE = Util.make(() -> Shapes.join(Shapes.block(), Shapes.or(Block.column(16.0F, 8.0F, 0.0F, 3.0F), Block.column(8.0F, 16.0F, 0.0F, 3.0F), Block.column(12.0F, 0.0F, 3.0F), SHAPE_INSIDE), BooleanOp.ONLY_FIRST));

    public BrewingCauldronBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull MapCodec<BrewingCauldronBlock> codec() { return CODEC; }

    @Override
    public @NonNull ItemStack getCloneItemStack(@NonNull LevelReader var0, @NonNull BlockPos var1, @NonNull BlockState var2, boolean var3) {
        return new ItemStack(Items.CAULDRON);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos blockPos, @NonNull BlockState blockState) {
        return new BrewingCauldronBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NonNull Level world, @NonNull BlockState state, @NonNull BlockEntityType<T> type) {
        return createTickerHelper(type, MillagerBlocks.BREWING_CAULDRON_ENTITY.get(), BrewingCauldronBlockEntity::tick);
    }

    @Override
    protected void affectNeighborsAfterRemoval(@NonNull BlockState state, ServerLevel level, @NonNull BlockPos pos, boolean movedByPiston) {

        AABB area = new AABB(pos).inflate(0.1, 0.5, 0.1);
        List<Display.ItemDisplay> visuals = level.getEntitiesOfClass(
                Display.ItemDisplay.class,
                area,
                e -> e.entityTags().contains("liquor_visual")
        );
        for (Display.ItemDisplay display : visuals) {
            if(!display.entityTags().contains("brewing_finished")) Block.popResource(level, pos, display.getItemStack());
            display.discard();
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    protected void entityInside(@NonNull BlockState blockState, @NonNull Level level, @NonNull BlockPos blockPos, Entity entity, @NonNull InsideBlockEffectApplier insideBlockEffectApplier, boolean bl) {
        if(entity.isOnFire()) {
            insideBlockEffectApplier.apply(InsideBlockEffectType.FIRE_IGNITE);
        }
    }

    //以下为 AbstractCauldronBlock 定义
    protected boolean hasAnalogOutputSignal(@NonNull BlockState blockState) {
        return true;
    }

    protected boolean isPathfindable(@NonNull BlockState blockState, @NonNull PathComputationType pathComputationType) {
        return false;
    }

    protected @NonNull VoxelShape getShape(@NonNull BlockState blockState, @NonNull BlockGetter blockGetter, @NonNull BlockPos blockPos, @NonNull CollisionContext collisionContext) {
        return SHAPE;
    }

    protected @NonNull VoxelShape getInteractionShape(@NonNull BlockState blockState, @NonNull BlockGetter blockGetter, @NonNull BlockPos blockPos) {
        return SHAPE_INSIDE;
    }

}
