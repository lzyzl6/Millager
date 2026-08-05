package org.lzyzl.millager.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.block.entity.TotemInfuserBlockEntity;

public class TotemInfuserBlock extends BaseEntityBlock {

    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 9.0, 16.0);

    public TotemInfuserBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos blockPos, @NonNull BlockState blockState) {
        return new TotemInfuserBlockEntity(blockPos, blockState);
    }

    @Override
    public void animateTick(@NonNull BlockState blockState, @NonNull Level level, @NonNull BlockPos blockPos, @NonNull RandomSource randomSource) {
        super.animateTick(blockState, level, blockPos, randomSource);

        double centerX = blockPos.getX() + 0.5;
        double centerY = blockPos.getY() + 0.5625;
        double centerZ = blockPos.getZ() + 0.5;

        double angle = randomSource.nextDouble() * Math.PI * 2.0;
        double spawnRadius = 0.85 + randomSource.nextDouble() * 0.4;

        double spawnX = centerX + Math.cos(angle) * spawnRadius;
        double spawnZ = centerZ + Math.sin(angle) * spawnRadius;
        double spawnY = blockPos.getY() + 0.8 + randomSource.nextDouble() * 0.5;

        double diffX = centerX - spawnX;
        double diffY = centerY - spawnY;
        double diffZ = centerZ - spawnZ;

        double speedFactor = 0.1;

        level.addParticle(ParticleTypes.END_ROD,
                spawnX, spawnY, spawnZ,
                diffX * speedFactor, diffY * speedFactor, diffZ * speedFactor
        );
    }

    @Override
    public @NonNull InteractionResult use(@NonNull BlockState blockState, @NonNull Level level, @NonNull BlockPos blockPos, @NonNull Player player, @NonNull InteractionHand hand, @NonNull BlockHitResult blockHitResult) {
        if (!level.isClientSide() && level.getBlockEntity(blockPos) instanceof TotemInfuserBlockEntity tbE) {
            player.openMenu(tbE);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @NonNull VoxelShape getShape(@NonNull BlockState state, @NonNull BlockGetter world, @NonNull BlockPos pos, @NonNull CollisionContext context) {
        return SHAPE;
    }

    public boolean useShapeForLightOcclusion(@NonNull BlockState blockState) {
        return true;
    }

    @Override
    public boolean isPathfindable(@NonNull BlockState blockState, @NonNull BlockGetter blockGetter, @NonNull BlockPos blockPos, @NonNull PathComputationType pathComputationType) {
        return false;
    }
}
