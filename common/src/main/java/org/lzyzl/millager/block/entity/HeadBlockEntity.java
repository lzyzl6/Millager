package org.lzyzl.millager.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.MillagerBlocks;

public class HeadBlockEntity extends BlockEntity {

    private @Nullable ResolvableProfile owner;
    private @Nullable ResourceLocation noteBlockSound;
    private int animationTickCount;
    private boolean isAnimating;
    private @Nullable Component customName;

    public HeadBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(MillagerBlocks.HEAD_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    @Override
    protected void saveAdditional(@NonNull CompoundTag tag, HolderLookup.@NonNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.owner != null) {
            tag.put("profile", ResolvableProfile.CODEC.encodeStart(NbtOps.INSTANCE, this.owner).getOrThrow());
        }
        if (this.noteBlockSound != null) {
            tag.putString("note_block_sound", this.noteBlockSound.toString());
        }
        if (this.customName != null) {
            tag.putString("custom_name", Component.Serializer.toJson(this.customName, registries));
        }
    }

    @Override
    protected void loadAdditional(@NonNull CompoundTag tag, HolderLookup.@NonNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("profile")) {
            this.owner = ResolvableProfile.CODEC.parse(NbtOps.INSTANCE, tag.get("profile")).getOrThrow();
        }
        if (tag.contains("note_block_sound")) {
            this.noteBlockSound = ResourceLocation.tryParse(tag.getString("note_block_sound"));
        }
        if (tag.contains("custom_name")) {
            this.customName = Component.Serializer.fromJson(tag.getString("custom_name"), registries);
        }
    }

    public static void animation(Level level, BlockPos blockPos, BlockState blockState, HeadBlockEntity entity) {
        if (blockState.hasProperty(SkullBlock.POWERED) && blockState.getValue(SkullBlock.POWERED)) {
            entity.isAnimating = true;
            ++entity.animationTickCount;
        } else {
            entity.isAnimating = false;
        }
    }

    public float getAnimation(float f) {
        return this.isAnimating ? (float)this.animationTickCount + f : (float)this.animationTickCount;
    }

    public @Nullable ResolvableProfile getOwnerProfile() {
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
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider provider) {
        return this.saveCustomOnly(provider);
    }

}
