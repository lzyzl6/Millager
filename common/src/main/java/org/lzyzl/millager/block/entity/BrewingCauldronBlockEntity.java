package org.lzyzl.millager.block.entity;

import com.mojang.math.Transformation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.MillagerBlocks;
import org.lzyzl.millager.block.LiquorCauldronBlock;

import java.util.List;

import static net.minecraft.world.level.block.Block.UPDATE_CLIENTS;

public class BrewingCauldronBlockEntity extends BlockEntity {

    private int timer = 24000;
    private float animationTicks = 0;

    public static final int START_COLOR = 4159204;
    public static final int END_COLOR = 5578058;

    public BrewingCauldronBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(MillagerBlocks.BREWING_CAULDRON_ENTITY.get(), blockPos, blockState);
    }

    public static void tick(Level world, BlockPos pos, BlockState state, BrewingCauldronBlockEntity be) {
        if (!(world instanceof ServerLevel level)) return;

        be.timer--;
        be.animationTicks++;

        float progress = Mth.clamp(1.0f - ((float) be.timer / 24000.0f), 0.0f, 1.0f);
        AABB area = new AABB(pos).inflate(0.1, 0.5, 0.1);
        List<Display.ItemDisplay> visuals = world.getEntitiesOfClass(Display.ItemDisplay.class, area, e -> e.getTags().contains("liquor_visual"));

        if (be.timer % 5 == 0 ) {
            spawnBrewingParticles(level, pos, progress);
        }
        updateVisuals(be, progress,visuals);

        if (be.timer % 20 == 0) {
            world.sendBlockUpdated(pos, state, state, Block.UPDATE_NEIGHBORS | UPDATE_CLIENTS);
        }


        if (be.timer <= 0) {
            finishBrewing(world, pos,visuals);
        }
    }

    private static void finishBrewing(Level world, BlockPos pos, List<Display.ItemDisplay> visuals) {

        visuals.forEach(entity -> entity.addTag("brewing_finished"));
        BlockState liquorState = MillagerBlocks.LIQUOR_CAULDRON.get().defaultBlockState()
                .setValue(LiquorCauldronBlock.LEVEL, 3);
        world.setBlock(pos, liquorState, Block.UPDATE_NEIGHBORS | UPDATE_CLIENTS);
        visuals.forEach(Display.ItemDisplay::discard);

        if (world instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.BUBBLE_POP,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    10, 0.2, 0.1, 0.2, 0.02);
        }
        world.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private static void updateVisuals(BrewingCauldronBlockEntity be, float progress, List<Display.ItemDisplay> visuals) {
        for (Display.ItemDisplay display : visuals) {
            display.setTransformationInterpolationDelay(-1);
            display.setTransformationInterpolationDuration(1);

            float scale = 0.7F - progress * 0.2F + Mth.sin(be.animationTicks * 0.1F) * 0.05F;
            float bobbing = Mth.sin(be.animationTicks * 0.1F) * 0.04F;
            display.setTransformation(new Transformation(
                    new Vector3f(0.0F, bobbing, 0.0F),
                    new Quaternionf().rotationY(be.animationTicks * 0.1F),
                    new Vector3f(scale), null));
        }
    }

    private static void spawnBrewingParticles(ServerLevel world, BlockPos pos, float progress) {
        double px = pos.getX() + 0.3 + world.random.nextDouble() * 0.4;
        double pz = pos.getZ() + 0.3 + world.random.nextDouble() * 0.4;

        int color = getInterpolatedColor(progress);

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        world.sendParticles(new DustParticleOptions(new Vector3f(r, g, b), 0.8f),
                px, pos.getY() + 1.05, pz, 1, 0, 0, 0, 0.01);
        if (progress > 0.5f && world.random.nextFloat() + progress > 1.0f) {
            if(world.random.nextFloat() + progress > 1.3f) world.playSound(null,pos, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS, 1.0F, 1.0F);
            world.sendParticles(ParticleTypes.BUBBLE, px, pos.getY() + 1.1, pz, 1, 0, 0.02, 0, 0.01);
        } else if(progress > 0.8f && world.random.nextFloat() + progress > 1.3f ) world.playSound(null,pos, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS, 0.3F, 1.0F);

    }

    public float getBrewingProgress() {
        return Mth.clamp(1.0f - ((float) this.timer / 24000.0f), 0.0f, 1.0f);
    }

    public static int getInterpolatedColor(float progress) {
        int r = (int) Mth.lerp(progress, (START_COLOR >> 16 & 0xFF), (END_COLOR >> 16 & 0xFF));
        int g = (int) Mth.lerp(progress, (START_COLOR >> 8 & 0xFF), (END_COLOR >> 8 & 0xFF));
        int b = (int) Mth.lerp(progress, (START_COLOR & 0xFF), (END_COLOR & 0xFF));
        return (r << 16) | (g << 8) | b;
    }

    @Override
    protected void saveAdditional(@NonNull CompoundTag tag, HolderLookup.@NonNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("BrewingTimer", this.timer);
    }

    @Override
    protected void loadAdditional(@NonNull CompoundTag tag, HolderLookup.@NonNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.timer = tag.getInt("BrewingTimer");
        if (this.timer == 0) this.timer = 24000;
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("BrewingTimer", this.timer);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

}
