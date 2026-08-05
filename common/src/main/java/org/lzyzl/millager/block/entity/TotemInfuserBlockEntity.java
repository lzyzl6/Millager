package org.lzyzl.millager.block.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.MillagerBlocks;
import org.lzyzl.millager.MillagerSounds;
import org.lzyzl.millager.block.menu.TotemInfuserMenu;

import java.util.ArrayList;
import java.util.List;

public class TotemInfuserBlockEntity extends BlockEntity implements MenuProvider, WorldlyContainer {

    private final SimpleContainer inventory = new SimpleContainer(3) {
        @Override
        public void setChanged() {
            super.setChanged();
            TotemInfuserBlockEntity.this.setChanged();
        }
    };

    public TotemInfuserBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(MillagerBlocks.TOTEM_INFUSER_ENTITY.get(), blockPos, blockState);
    }

    @Override
    public @NonNull Component getDisplayName() {
        return Component.translatable("container.totem_infuser");
    }

    @Override
    public AbstractContainerMenu createMenu(int i, @NonNull Inventory inventory, @NonNull Player player) {
        return new TotemInfuserMenu(i, inventory, this);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput valueInput) {
        super.loadAdditional(valueInput);
        ContainerHelper.loadAllItems(valueInput, this.inventory.getItems());
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        ContainerHelper.saveAllItems(valueOutput, this.inventory.getItems());

    }

    @Override
    public int getContainerSize() {
        return 3;
    }

    @Override
    public boolean isEmpty() {
        return this.inventory.isEmpty();
    }

    @Override
    public @NonNull ItemStack getItem(int i) {
        return this.inventory.getItem(i);
    }

    @Override
    public @NonNull ItemStack removeItem(int i, int j) {
        return this.inventory.removeItem(i, j);
    }

    @Override
    public @NonNull ItemStack removeItemNoUpdate(int i) {
        return this.inventory.removeItemNoUpdate(i);
    }

    @Override
    public void setItem(int i, @NonNull ItemStack itemStack) {
        this.inventory.setItem(i, itemStack);
        updateOutput();
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.inventory.clearContent();
    }

    @Override
    public void setRemoved() {
        this.inventory.setItem(2,ItemStack.EMPTY);
        super.setRemoved();
    }

    @Override
    public int @NonNull [] getSlotsForFace(@NonNull Direction side) {
        if (side == Direction.UP) return new int[]{};
        if (side == Direction.DOWN) return new int[]{1};
        return new int[]{0,1};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, @NonNull ItemStack stack, @Nullable Direction side) {
        if(slot == 0)  return stack.is(Items.TOTEM_OF_UNDYING);
        return slot == 1 && this.inventory.getItem(1).getCount() < stack.getMaxStackSize()
                && !stack.has(DataComponents.DEATH_PROTECTION);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, @NonNull ItemStack stack, @NonNull Direction side) {
        return slot != 2;
    }

    @Override
    public void preRemoveSideEffects(@NonNull BlockPos blockPos, @NonNull BlockState blockState) {
        this.inventory.setItem(2, ItemStack.EMPTY);
        super.preRemoveSideEffects(blockPos, blockState);
    }

    private void updateOutput() {
        ItemStack totemStack = this.inventory.getItem(0);
        ItemStack inputStack = this.inventory.getItem(1);

        if (totemStack.is(Items.TOTEM_OF_UNDYING) && !inputStack.isEmpty()) {

            ItemStack outputStack = inputStack.copyWithCount(1);
            outputStack.set(DataComponents.DEATH_PROTECTION, DeathProtection.TOTEM_OF_UNDYING);

            ItemLore existingLore = outputStack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
            List<Component> newLines = new ArrayList<>(existingLore.lines());
            newLines.add(Component.translatable("tooltip.millager.undying").withStyle(ChatFormatting.YELLOW));
            outputStack.set(DataComponents.LORE, new ItemLore(newLines));
            this.inventory.setItem(2, outputStack);
        }else {
            this.inventory.setItem(2, ItemStack.EMPTY);
        }
    }

    public void onInfuseComplete() {
        if (this.level == null) return;

        this.inventory.removeItem(0, 1);
        this.inventory.removeItem(1, 1);
        this.inventory.removeItemNoUpdate(2);
        this.level.playSound(null, this.worldPosition, MillagerSounds.TOTEM_INFUSE, SoundSource.BLOCKS, 1.0F, 0.65F);

        if (this.level instanceof ServerLevel serverLevel) {
            double x = this.worldPosition.getX() + 0.5;
            double y = this.worldPosition.getY() + 0.1;
            double z = this.worldPosition.getZ() + 0.5;

            for (int j = 0; j < 24; j++) {
                double angle = j * Math.PI * 2.0 / 24.0;
                serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                        x + Math.cos(angle) * 0.8, y, z + Math.sin(angle) * 0.8,
                        1, 0, 0.1, 0, 0.05);
            }
        }
        this.setChanged();
    }
}
