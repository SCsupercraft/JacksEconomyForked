package me.khajiitos.jackseconomy.menu;

import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy.util.IDisablable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class OIMWalletMenu extends AbstractContainerMenu {
    private final ItemStack itemStack;

    public OIMWalletMenu(int pContainerId, Inventory playerInv, ItemStack itemStack) {
        super(ContainerReg.OIM_WALLET_MENU.get(), pContainerId);

        this.itemStack = itemStack;

        IItemHandler itemHandler = itemStack.getCapability(Capabilities.ItemHandler.ITEM);
        if (itemHandler != null) {
            for (int i = 0; i < 15; i++) {
                int row = i / 3;
                int col = i % 3;
                this.addSlot(new SlotItemHandler(itemHandler, i, 8 + col * 18, 17 + row * 18));
            }
        }

        this.addPlayerInventory(playerInv, 116);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        int containerSize = 15;
        ItemStack clickedStackCopy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack clickedStack = slot.getItem();
            clickedStackCopy = clickedStack.copy();
            if (index < containerSize) {
                if (!this.moveItemStackTo(clickedStack, containerSize, containerSize + 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(clickedStack, 0, containerSize, false)) {
                return ItemStack.EMPTY;
            }

            if (clickedStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (clickedStack.getCount() == clickedStackCopy.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, clickedStack);
        }

        return clickedStackCopy;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        if (this.itemStack.getItem() instanceof IDisablable disablable && disablable.isDisabled()) {
            return false;
        }
        return !this.itemStack.isEmpty();
    }

    public void addPlayerInventory(Inventory playerInv, int yOffset) {
        for (int rowY = 0; rowY < 3; ++rowY) {
            for (int rowX = 0; rowX < 9; ++rowX) {
                this.addSlot(new Slot(playerInv, rowX + rowY * 9 + 9, 8 + rowX * 18, yOffset + rowY * 18));
            }
        }

        for (int rowX = 0; rowX < 9; ++rowX) {
            this.addSlot(new Slot(playerInv, rowX, 8 + rowX * 18, yOffset + 58));
        }
    }
}
