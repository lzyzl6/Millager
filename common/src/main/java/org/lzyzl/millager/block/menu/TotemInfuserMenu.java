package org.lzyzl.millager.block.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.block.MillagerMenuType;
import org.lzyzl.millager.block.entity.TotemInfuserBlockEntity;

public class TotemInfuserMenu extends AbstractContainerMenu {

    private final Container container;

    public TotemInfuserMenu(int i, Inventory inventory) {
        this(i, inventory, new SimpleContainer(3));
    }

    public TotemInfuserMenu(int i, Inventory inventory, Container container) {
        super(MillagerMenuType.TOTEM_INFUSER.get(), i);
        checkContainerSize(container, 3);
        this.container = container;
        container.startOpen(inventory.player);

        this.addSlot(new Slot(container, 0, 81, 18) {
            @Override
            public boolean mayPlace(@NonNull ItemStack itemStack) {
                return itemStack.is(Items.TOTEM_OF_UNDYING);
            }
        });

        this.addSlot(new Slot(container, 1, 81, 54) {

            @Override
            public boolean mayPlace(@NonNull ItemStack itemStack) {
                return TotemInfuserBlockEntity.isValidInfusionInput(itemStack);
            }

        });

        this.addSlot(new Slot(container, 2, 117, 54) {

            @Override
            public boolean mayPlace(@NonNull ItemStack itemStack) {
                return false;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public void onTake(@NonNull Player player, @NonNull ItemStack itemStack) {
                super.onTake(player, itemStack);
                if (container instanceof TotemInfuserBlockEntity entity) {
                    entity.onInfuseComplete();
                }
            }
        });

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);

        if (slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();

            if (i < 3) {

                if (!this.moveItemStackTo(itemStack2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                if (i == 2) {
                    slot.onQuickCraft(itemStack2, itemStack);
                }
            }

            else {
                if (itemStack2.is(Items.TOTEM_OF_UNDYING)) {
                    if (!this.moveItemStackTo(itemStack2, 0, 1, false)) {

                        if (!this.moveItemStackTo(itemStack2, 1, 2, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }

                else {
                    if (!this.moveItemStackTo(itemStack2, 1, 2, false)) {

                        if (i < 30) {
                            if (!this.moveItemStackTo(itemStack2, 30, 39, false)) return ItemStack.EMPTY;
                        } else {
                            if (!this.moveItemStackTo(itemStack2, 3, 30, false)) return ItemStack.EMPTY;
                        }
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemStack2);
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return this.container.stillValid(player);
    }
}
