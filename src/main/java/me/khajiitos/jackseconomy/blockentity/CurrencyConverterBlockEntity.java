package me.khajiitos.jackseconomy.blockentity;

import me.khajiitos.jackseconomy.block.CurrencyConverterBlock;
import me.khajiitos.jackseconomy.block.TransactionMachineBlock;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import me.khajiitos.jackseconomy.item.CurrencyItem;
import me.khajiitos.jackseconomy.menu.CurrencyConverterMenu;
import me.khajiitos.jackseconomy.util.CurrencyType;
import me.khajiitos.jackseconomy.util.IDisablable;
import me.khajiitos.jackseconomy.util.SideConfig;
import me.khajiitos.jackseconomy.util.SlottedItemStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CurrencyConverterBlockEntity extends BlockEntity implements WorldlyContainer, Container, MenuProvider, ISideConfigurable, IDisablable {
    public NonNullList<ItemStack> items;
    public CurrencyType selectedCurrencyType = CurrencyType.PENNY;
    protected BigDecimal currency = BigDecimal.ZERO;

    private static final int[] slotsInput = new int[]{0, 1, 2};
    private static final int[] slotsOutput = new int[]{3, 4, 5, 6, 7, 8, 9, 10, 11};

    protected SlottedItemStackHandler itemHandlerInput;
    protected SlottedItemStackHandler itemHandlerOutput;
    protected SlottedItemStackHandler itemHandlerRejectionOutput;
    protected LazyOptional<IItemHandler> itemHandlerInputLazy = LazyOptional.of(() -> itemHandlerInput);
    protected LazyOptional<IItemHandler> itemHandlerOutputLazy = LazyOptional.of(() -> itemHandlerOutput);
    protected LazyOptional<IItemHandler> itemHandlerRejectionOutputLazy = LazyOptional.of(() -> itemHandlerRejectionOutput);
    protected SideConfig sideConfig = new SideConfig();

    public CurrencyConverterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityReg.CURRENCY_CONVERTER.get(), pPos, pBlockState);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        itemHandlerInput = new SlottedItemStackHandler(this.items, slotsInput, true, false);
        itemHandlerOutput = new SlottedItemStackHandler(this.items, slotsOutput, false, true);
        itemHandlerRejectionOutput = new SlottedItemStackHandler(this.items, slotsInput, false, true, this::isItemRejected);
    }

    protected boolean hitCapacityLimit() {
        return getTotalBalance().compareTo(BigDecimal.valueOf(Config.maxCurrencyConverterBalance.get())) >= 0;
    }

    protected boolean isItemRejected(ItemStack itemStack) {
        return !(itemStack.getItem() instanceof CurrencyItem) || hitCapacityLimit();
    }

    public BigDecimal getTotalBalance() {
        BigDecimal balance = this.getCurrency();

        for (int slot : slotsOutput) {
            ItemStack itemStack = this.items.get(slot);

            if (itemStack.getItem() instanceof CurrencyItem currencyItem) {
                balance = balance.add(currencyItem.value.multiply(BigDecimal.valueOf(itemStack.getCount())));
            }
        }

        return balance;
    }

    public BigDecimal getCurrency() {
        return currency;
    }

    @Override
    public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
        return false;
    }

    @Override
    public int getContainerSize() {
        return 12;
    }


    public Component getDisplayName() {
        return Component.translatable("block.jackseconomy.currency_converter");
    }

    public ItemStack getItem(int index) {
        return this.items.get(index);
    }

    public ItemStack removeItem(int index, int count) {
        ItemStack stack = ContainerHelper.removeItem(this.items, index, count);
        if (!stack.isEmpty()) {
            this.setChanged();
        }

        return stack;
    }

    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(this.items, index);
    }

    public void setItem(int index, ItemStack stack) {
        this.items.set(index, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }

        this.setChanged();
    }

    public boolean stillValid(Player player) {
        return this.level.getBlockEntity(this.worldPosition) == this;
    }

    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    public void clearContent() {
        this.items.clear();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new CurrencyConverterMenu(pContainerId, pPlayerInventory, this);
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, CurrencyConverterBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (blockEntity.isDisabled()) {
            return;
        }

        boolean updated = false;

        if (blockEntity.getTotalBalance().compareTo(BigDecimal.valueOf(Config.maxCurrencyConverterBalance.get())) < 0) {
            for (int slotInput : slotsInput) {
                ItemStack itemStack = blockEntity.items.get(slotInput);

                if (!itemStack.isEmpty() && itemStack.getItem() instanceof CurrencyItem currencyItem) {
                    blockEntity.currency = blockEntity.currency.add(currencyItem.value.multiply(new BigDecimal(itemStack.getCount())));
                    itemStack.setCount(0);
                    updated = true;
                }
            }
        }

        for (int slotOutput : slotsOutput) {
            ItemStack itemStack = blockEntity.items.get(slotOutput);

            if (!itemStack.isEmpty() && itemStack.getItem() instanceof CurrencyItem currencyItem && blockEntity.selectedCurrencyType.item != currencyItem) {
                blockEntity.currency = blockEntity.currency.add(currencyItem.value.multiply(new BigDecimal(itemStack.getCount())));
                itemStack.setCount(0);
                updated = true;
            }
        }

        int toAdd = Math.min(blockEntity.selectedCurrencyType.item.getMaxStackSize(), (blockEntity.currency.divide(blockEntity.selectedCurrencyType.worth, 0, RoundingMode.DOWN).intValue()));

        if (toAdd > 0) {
            ItemStack stack = new ItemStack(blockEntity.selectedCurrencyType.item, toAdd);
            ItemStack left = blockEntity.addItem(stack, slotsOutput);

            BigDecimal worth = blockEntity.selectedCurrencyType.worth;

            if (left != null && !left.isEmpty()) {
                blockEntity.currency = blockEntity.currency.subtract(worth.multiply(new BigDecimal(toAdd - left.getCount())));
                //blockEntity.currency = blockEntity.currency.add(blockEntity.selectedCurrencyType.worth.multiply(new BigDecimal(toAdd - left.getCount())));
            } else {
                blockEntity.currency = blockEntity.currency.subtract(worth.multiply(new BigDecimal(toAdd)));
            }

            updated = true;
        }

        if (updated) {
            blockEntity.markUpdated();
        }
    }

    public void markUpdated() {
        if (this.getLevel() != null) {
            this.setChanged();
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }

    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (pkt.getTag() != null) {
            this.load(pkt.getTag());
        }
    }

    public void handleUpdateTag(CompoundTag tag) {
        this.loadAdditional(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putString("Currency", this.currency.toString());
        pTag.putDouble("CurrencyType", this.selectedCurrencyType.ordinal());
        pTag.put("SideConfig", this.sideConfig.toNbt());
        ContainerHelper.saveAllItems(pTag, this.items);
    }

    public void loadAdditional(CompoundTag pTag) {
        try {
            this.currency = new BigDecimal(pTag.getString("Currency"));
        } catch (NumberFormatException e) {
            this.currency = BigDecimal.ZERO;
        }
        int currencyType = pTag.getInt("CurrencyType");

        if (currencyType < CurrencyType.values().length) {
            this.selectedCurrencyType = CurrencyType.values()[currencyType];
        } else {
            this.selectedCurrencyType = CurrencyType.PENNY;
        }

        this.sideConfig = SideConfig.fromIntArray(pTag.getIntArray("SideConfig"));

    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        ContainerHelper.loadAllItems(pTag, this.items);
        this.loadAdditional(pTag);
    }

    public @Nullable ItemStack addItem(ItemStack itemStack, int[] slots) {
        ItemStack remainingItems = itemStack.copy();

        for (int i : slots) {
            ItemStack slotStack = items.get(i);
            if (slotStack.isEmpty()) {
                int stackSize = Math.min(remainingItems.getCount(), itemStack.getMaxStackSize());
                ItemStack stackToAdd = remainingItems.split(stackSize);
                items.set(i, stackToAdd);
            } else if (ItemStack.isSameItemSameTags(slotStack, remainingItems)) {
                int spaceAvailable = itemStack.getMaxStackSize() - slotStack.getCount();
                int stackSize = Math.min(remainingItems.getCount(), spaceAvailable);
                slotStack.grow(stackSize);
                remainingItems.shrink(stackSize);
            }

            if (remainingItems.isEmpty()) {
                return null; // The whole ItemStack fits in the inventory
            }
        }

        return remainingItems; // Return any leftover items
    }

    @Override
    public int[] getSlotsForFace(Direction pSide) {
        Direction facing = getBlockState().getValue(TransactionMachineBlock.FACING);

        switch (this.sideConfig.getValue(SideConfig.directionRelative(facing, pSide))) {
            case INPUT -> {
                return slotsInput;
            } case OUTPUT -> {
                return slotsOutput;
            }
        }
        return new int[]{};
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        Direction facing = this.getBlockState().getValue(CurrencyConverterBlock.FACING);

        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) {
                return itemHandlerOutputLazy.cast();
            }
            switch (sideConfig.getValue(SideConfig.directionRelative(facing, side))) {
                case INPUT -> {
                    return itemHandlerInputLazy.cast();
                }
                case OUTPUT -> {
                    return itemHandlerOutputLazy.cast();
                }
                case REJECTION_OUTPUT -> {
                    return itemHandlerRejectionOutputLazy.cast();
                }
            }
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandlerInputLazy.invalidate();
        itemHandlerOutputLazy.invalidate();
        itemHandlerRejectionOutputLazy.invalidate();
    }

    @Override
    public SideConfig getSideConfig() {
        return this.sideConfig;
    }

    @Override
    public boolean isDisabled() {
        return Config.oneItemCurrencyMode.get();
    }
}
