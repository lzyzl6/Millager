package org.lzyzl.millager.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.MillagerSounds;

public class PottedRoseBlock extends FlowerPotBlock {

    private static final int WILT_CHANCE = 15;
    private static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D);

    public PottedRoseBlock(Block flower, Properties properties) {
        super(flower, properties);
    }

    @Override
    public @NonNull VoxelShape getShape(@NonNull BlockState state, @NonNull BlockGetter level, @NonNull BlockPos pos, @NonNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean isRandomlyTicking(@NonNull BlockState state) {
        return true;
    }

    @Override
    public void randomTick(@NonNull BlockState state, ServerLevel level, @NonNull BlockPos pos, @NonNull RandomSource random) {
        if (level.getRawBrightness(pos, 0) >= 8 || !hasNearbySoulSand(level, pos) || random.nextInt(WILT_CHANCE) != 0) return;
        level.playSound(null, pos, MillagerSounds.ROSE_WILT, SoundSource.BLOCKS, 0.7F, 0.7F);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5D, pos.getY() + 0.45D, pos.getZ() + 0.5D, 4, 0.15D, 0.1D, 0.15D, 0.005D);
        level.setBlock(pos, Blocks.POTTED_WITHER_ROSE.defaultBlockState(), 3);
    }

    private static boolean hasNearbySoulSand(ServerLevel level, BlockPos pos) {
        for (BlockPos nearbyPos : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (level.getBlockState(nearbyPos).is(Blocks.SOUL_SAND)) return true;
        }
        return false;
    }
}
