package me.khajiitos.jackseconomy.menu;

import me.khajiitos.jackseconomy.blockentity.FluidTransactionKineticMachineBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class KineticFluidTransactionMachineMenu extends AbstractContainerMenu implements IBlockEntityContainer<FluidTransactionKineticMachineBlockEntity> {
    private final FluidTransactionKineticMachineBlockEntity blockEntity;

    protected KineticFluidTransactionMachineMenu(@Nullable MenuType<?> pMenuType, int pContainerId, FluidTransactionKineticMachineBlockEntity blockEntity) {
        super(pMenuType, pContainerId);
        this.blockEntity = blockEntity;
    }

    public abstract int getContainerSize();

    public void addPlayerInventory(Inventory playerInv, int yOffset) {
        for(int rowY = 0; rowY < 3; ++rowY) {
            for(int rowX = 0; rowX < 9; ++rowX) {
                this.addSlot(new Slot(playerInv, rowX + rowY * 9 + 9, 8 + rowX * 18, yOffset + rowY * 18));
            }
        }

        for(int rowX = 0; rowX < 9; ++rowX) {
            this.addSlot(new Slot(playerInv, rowX, 8 + rowX * 18, yOffset + 58));
        }

    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        int containerSize = this.getContainerSize();
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

    public FluidTransactionKineticMachineBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public boolean stillValid(Player player) {
        return this.getBlockEntity().stillValid(player);
    }
}
