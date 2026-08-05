package org.lzyzl.millager.block.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.MillagerBlocks;

public class HeadBlockEntity extends BlockEntity {

    private @Nullable GameProfile owner;
    private @Nullable ResourceLocation noteBlockSound;
    private int animationTickCount;
    private boolean isAnimating;
    private @Nullable Component customName;

    public HeadBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(MillagerBlocks.HEAD_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    @Override
    protected void saveAdditional(@NonNull CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.owner != null) {
            tag.put("profile", NbtUtils.writeGameProfile(new CompoundTag(), this.owner));
        }
        if (this.noteBlockSound != null) {
            tag.putString("note_block_sound", this.noteBlockSound.toString());
        }
        if (this.customName != null) {
            tag.putString("custom_name", Component.Serializer.toJson(this.customName));
        }
    }

    @Override
    public void load(@NonNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("profile", Tag.TAG_COMPOUND)) {
            this.owner = NbtUtils.readGameProfile(tag.getCompound("profile"));
        }
        if (tag.contains("note_block_sound")) {
            this.noteBlockSound = ResourceLocation.tryParse(tag.getString("note_block_sound"));
        }
        if (tag.contains("custom_name")) {
            this.customName = Component.Serializer.fromJson(tag.getString("custom_name"));
        }
    }

    public static void animation(Level level, BlockPos blockPos, BlockState blockState, HeadBlockEntity entity) {
        if (level.hasNeighborSignal(blockPos)) {
            entity.isAnimating = true;
            ++entity.animationTickCount;
        } else {
            entity.isAnimating = false;
        }
    }

    public float getAnimation(float f) {
        return this.isAnimating ? (float)this.animationTickCount + f : (float)this.animationTickCount;
    }

    public @Nullable GameProfile getOwnerProfile() {
        return this.owner;
    }

    public @Nullable ResourceLocation getNoteBlockSound() {
        return this.noteBlockSound;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NonNull CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

}
