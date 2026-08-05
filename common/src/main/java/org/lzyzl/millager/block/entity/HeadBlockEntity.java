package org.lzyzl.millager.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.MillagerBlocks;

public class HeadBlockEntity extends BlockEntity {

    private @Nullable ResolvableProfile owner;
    private @Nullable Identifier noteBlockSound;
    private int animationTickCount;
    private boolean isAnimating;
    private @Nullable Component customName;

    public HeadBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(MillagerBlocks.HEAD_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.storeNullable("profile", ResolvableProfile.CODEC, this.owner);
        valueOutput.storeNullable("note_block_sound", Identifier.CODEC, this.noteBlockSound);
        valueOutput.storeNullable("custom_name", ComponentSerialization.CODEC, this.customName);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.owner = valueInput.read("profile", ResolvableProfile.CODEC).orElse(null);
        this.noteBlockSound = valueInput.read("note_block_sound", Identifier.CODEC).orElse(null);
        this.customName = parseCustomNameSafe(valueInput, "custom_name");
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

    public @Nullable Identifier getNoteBlockSound() {
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
    @Override
    protected void applyImplicitComponents(@NonNull DataComponentGetter dataComponentGetter) {
        super.applyImplicitComponents(dataComponentGetter);
        this.owner = dataComponentGetter.get(DataComponents.PROFILE);
        this.noteBlockSound = dataComponentGetter.get(DataComponents.NOTE_BLOCK_SOUND);
        this.customName = dataComponentGetter.get(DataComponents.CUSTOM_NAME);
    }
    @Override
    protected void collectImplicitComponents(DataComponentMap.@NonNull Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.PROFILE, this.owner);
        builder.set(DataComponents.NOTE_BLOCK_SOUND, this.noteBlockSound);
        builder.set(DataComponents.CUSTOM_NAME, this.customName);
    }
}
