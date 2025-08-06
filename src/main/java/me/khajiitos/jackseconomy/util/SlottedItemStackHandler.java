package me.khajiitos.jackseconomy.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class SlottedItemStackHandler extends ItemStackHandler {
    protected final int[] slots;
    protected final boolean allowInput;
    protected final boolean allowOutput;
    protected final Predicate<ItemStack> itemPredicate;

    public SlottedItemStackHandler(NonNullList<ItemStack> stacks, int[] slots, boolean allowInput, boolean allowOutput, Predicate<ItemStack> itemPredicate) {
        this.stacks = stacks;
        this.slots = slots;
        this.allowInput = allowInput;
        this.allowOutput = allowOutput;
        this.itemPredicate = itemPredicate;
    }

    public SlottedItemStackHandler(NonNullList<ItemStack> stacks, int[] slots, boolean allowInput, boolean allowOutput) {
        this(stacks, slots, allowInput, allowOutput, null);
    }

    public SlottedItemStackHandler(NonNullList<ItemStack> stacks, int[] slots) {
        this(stacks, slots, true, true, null);
    }

    public boolean isItemAllowed(ItemStack stack) {
        if (itemPredicate != null) {
            return itemPredicate.test(stack);
        }
        return true;
    }

    public boolean isSlotValid(int slot) {
        for (int i : slots) {
            if (slot == i) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!allowInput || !isSlotValid(slot) || !isItemAllowed(stack)) {
            return stack;
        }

        return super.insertItem(slot, stack, simulate);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!allowOutput || !isSlotValid(slot)) {
            return ItemStack.EMPTY;
        }

        ItemStack itemStack = this.stacks.get(slot);

        if (!isItemAllowed(itemStack)) {
            return ItemStack.EMPTY;
        }

        return super.extractItem(slot, amount, simulate);
    }

    public void changeItems(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }
}
