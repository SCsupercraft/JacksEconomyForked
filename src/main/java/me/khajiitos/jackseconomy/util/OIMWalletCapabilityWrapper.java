package me.khajiitos.jackseconomy.util;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.init.ComponentReg;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import me.khajiitos.jackseconomy.item.OIMWalletItem;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

public class OIMWalletCapabilityWrapper implements IItemHandlerModifiable {
    private final ItemStack walletStack;
    private final NonNullList<ItemStack> stacks;

    private OIMWalletCapabilityWrapper(ItemStack walletStack) {
        this.walletStack = walletStack;
        this.stacks = NonNullList.withSize(15, ItemStack.EMPTY);

        CompoundTag data = walletStack.get(ComponentReg.OIM_WALLET_BALANCE);
        if (data == null) return;

        ContainerHelper.loadAllItems(data, stacks, JacksEconomy.server.registryAccess());
    }

    public static OIMWalletCapabilityWrapper create(ItemStack itemStack) {
        if (itemStack.getItem() instanceof OIMWalletItem) {
            return new OIMWalletCapabilityWrapper(itemStack);
        }
        return null;
    }

    public void save() {
        CompoundTag data = new CompoundTag();
        ContainerHelper.saveAllItems(data, stacks, false, JacksEconomy.server.registryAccess());
        walletStack.set(ComponentReg.OIM_WALLET_BALANCE, data);
    }

    /**
     * Sets the ItemStack in the specified slot. Replaces any existing stack.
     *
     * @param slot The index of the target slot.
     * @param stack The ItemStack to insert into the slot.
     */
    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (!isItemValid(slot, stack)) return;

        stacks.set(slot, stack);
        save();
    }

    /**
     * @return The total number of available slots.
     */
    @Override
    public int getSlots() {
        return 15;
    }

    /**
     * Retrieves the current stack in the specified slot.
     *
     * @param slot The index of the slot to access.
     * @return The ItemStack in the given slot (may be empty).
     */
    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return stacks.get(slot);
    }

    /**
     * Attempts to insert an ItemStack into a specific slot.
     *
     * @param slot The target slot index.
     * @param stack The ItemStack to insert.
     * @param simulate If true, doesn't actually insert—just checks what would happen.
     * @return The remaining ItemStack that couldn't be inserted.
     */
    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || slot < 0 || slot >= getSlots() || !isItemValid(slot, stack))
            return stack;

        ItemStack existing = stacks.get(slot);
        int maxStackSize = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));

        if (existing.isEmpty()) {
            if (!simulate) {
                ItemStack toInsert = stack.copy();
                toInsert.setCount(Math.min(stack.getCount(), maxStackSize));
                stacks.set(slot, toInsert);
                save();
            }
            ItemStack remaining = stack.copy();
            remaining.setCount(Math.max(0, stack.getCount() - maxStackSize));
            return remaining;
        }

        if (!ItemStack.isSameItemSameComponents(stack, existing))
            return stack;

        int spaceAvailable = maxStackSize - existing.getCount();
        int toTransfer = Math.min(stack.getCount(), spaceAvailable);

        if (toTransfer <= 0)
            return stack;

        if (!simulate) {
            existing.grow(toTransfer);
            save();
        }

        ItemStack remainder = stack.copy();
        remainder.setCount(stack.getCount() - toTransfer);
        return remainder;
    }

    /**
     * Extracts items from a specific slot.
     *
     * @param slot The source slot index.
     * @param amount Maximum number of items to extract.
     * @param simulate If true, doesn't actually extract—just checks what would happen.
     * @return The extracted ItemStack.
     */
    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0 || slot < 0 || slot >= getSlots())
            return ItemStack.EMPTY;

        ItemStack existing = stacks.get(slot);
        if (existing.isEmpty())
            return ItemStack.EMPTY;

        int extractAmount = Math.min(amount, existing.getCount());
        ItemStack extracted = existing.copy();
        extracted.setCount(extractAmount);

        if (!simulate) {
            if (extractAmount == existing.getCount()) {
                stacks.set(slot, ItemStack.EMPTY);
            } else {
                existing.shrink(extractAmount);
            }
            save();
        }

        return extracted;
    }

    /**
     * @param slot The slot index.
     * @return The maximum number of items that can be stored in this slot.
     */
    @Override
    public int getSlotLimit(int slot) {
        return ItemBlockReg.DOLLAR_BILL_ITEM.get().getDefaultMaxStackSize();
    }

    /**
     * Checks if a stack can be inserted into a given slot.
     *
     * @param slot The target slot index.
     * @param stack The ItemStack to validate.
     * @return True if the item can be inserted, false otherwise.
     */
    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return stack.is(ItemBlockReg.DOLLAR_BILL_ITEM.get());
    }
}
