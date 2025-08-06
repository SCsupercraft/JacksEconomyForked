package me.khajiitos.jackseconomy.blockentity;

import me.khajiitos.jackseconomy.block.TransactionMachineBlock;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import me.khajiitos.jackseconomy.data.price.PriceManager;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import me.khajiitos.jackseconomy.item.CurrencyItem;
import me.khajiitos.jackseconomy.item.TicketItem;
import me.khajiitos.jackseconomy.menu.ImporterMenu;
import me.khajiitos.jackseconomy.util.RedstoneToggle;
import me.khajiitos.jackseconomy.util.SideConfig;
import me.khajiitos.jackseconomy.util.SlottedItemStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ImporterBlockEntity extends TransactionMachineBlockEntity implements IImporterBlockEntity {
    private static final int[] slotsInput = new int[]{0, 1, 2};
    private static final int[] slotsOutput = new int[]{3, 4, 5, 6, 7, 8};
    private static final int slotTicket = 9;
    protected SlottedItemStackHandler itemHandlerInput;
    protected SlottedItemStackHandler itemHandlerOutput;
    protected SlottedItemStackHandler itemHandlerRejectionOutput;

    public ItemDescription selectedItem;
    private float progress;

    public ImporterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityReg.IMPORTER.get(), pos, state);
        itemHandlerInput = new SlottedItemStackHandler(this.items, slotsInput, true, false);
        itemHandlerOutput = new SlottedItemStackHandler(this.items, slotsOutput, false, true);
        itemHandlerRejectionOutput = new SlottedItemStackHandler(this.items, slotsInput, false, true, this::isItemRejected);
    }

    @Override
    public BigDecimal getTotalBalance() {
        return getBalance();
    }

    protected boolean hitCapacityLimit() {
        return getTotalBalance().compareTo(BigDecimal.valueOf(Config.maxImporterBalance.get())) >= 0;
    }

    protected boolean isItemRejected(ItemStack itemStack) {
        return !(itemStack.getItem() instanceof CurrencyItem);
    }


    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.jackseconomy.importer");
    }

    @Override
    public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
        return true;
    }

    @Override
    public int getContainerSize() {
        return 10;
    }

    public float getProgress() {
        return progress;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new ImporterMenu(pContainerId, pPlayerInventory, this);
    }

    @Override
    public void saveMachineData(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveMachineData(tag, provider);
        tag.putFloat("Progress", this.progress);

        if (this.selectedItem != null) {
            tag.put("SelectedItem", this.selectedItem.toNbt());
        }
    }

    @Override
    public void loadMachineData(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadMachineData(tag, provider);

        // When items are loaded, the items array is a completely new array
        this.itemHandlerInput.changeItems(this.items);
        this.itemHandlerOutput.changeItems(this.items);
        this.itemHandlerRejectionOutput.changeItems(this.items);

        this.progress = tag.getFloat("Progress");

        if (tag.contains("SelectedItem")) {
            this.selectedItem = ItemDescription.fromNbt(tag.getCompound("SelectedItem"));
        } else {
            this.selectedItem = null;
        }
    }

    @Override
    public int[] getSlotsForFace(Direction pSide) {
        Direction facing = getBlockState().getValue(TransactionMachineBlock.FACING);

        switch (this.sideConfig.getValue(SideConfig.directionRelative(facing, pSide))) {
            case INPUT -> {
                return slotsInput;
            }
            case OUTPUT -> {
                return slotsOutput;
            }
            case REJECTION_OUTPUT -> {
                return Arrays.stream(slotsInput).filter(slot -> isItemRejected(this.items.get(slot))).toArray();
            }
        }
        return new int[]{};
    }

    @Override
    public IItemHandler getItemCapability(Direction direction) {
        Direction facing = this.getBlockState().getValue(TransactionMachineBlock.FACING);
        if (direction == null) return itemHandlerOutput;

        switch (sideConfig.getValue(SideConfig.directionRelative(facing, direction))) {
            case INPUT -> {
                return itemHandlerInput;
            }
            case OUTPUT -> {
                return itemHandlerOutput;
            }
            case REJECTION_OUTPUT -> {
                return itemHandlerRejectionOutput;
            }
        }
        return null;
    }

    public double getProgressPerTick() {
        double baseProgress = Config.baseImporterProgressPerTick.get();
        return baseProgress + this.speed * (baseProgress * 10);
    }

    public int getEnergyUsagePerTick() {
        return Config.baseImporterEnergyUsage.get() + (int)Math.ceil(Math.pow(this.speed * 64.0, 1.25));
    }

    // what a stupid name
    private boolean doesRedstoneSettingMatchWorld(Level level, BlockPos pos) {
        return (this.redstoneToggle == RedstoneToggle.SIGNAL_ON && level.hasNeighborSignal(pos)) || (this.redstoneToggle == RedstoneToggle.SIGNAL_OFF && !level.hasNeighborSignal(pos)) || this.redstoneToggle == RedstoneToggle.IGNORED;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ImporterBlockEntity importer) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (importer.getTotalBalance().compareTo(BigDecimal.valueOf(Config.maxImporterBalance.get())) < 0) {
            for (int i = 0; i < 3; i++) {
                ItemStack inputItem = importer.getItem(i);

                if (inputItem.getItem() instanceof CurrencyItem coin) {
                    importer.currency = importer.currency.add(coin.value.multiply(new BigDecimal(inputItem.getCount())));
                    inputItem.setCount(0);
                    //updated = true;
                }
            }
        }

        ItemStack ticketItemStack = importer.items.get(slotTicket);
        List<ItemDescription> items = TicketItem.getItems(ticketItemStack);

        ItemStack itemStackToAdd = ItemStack.EMPTY;

        if (!items.isEmpty() && importer.selectedItem == null) {
            importer.selectedItem = items.get(0);
        }

        if (importer.selectedItem != null) {
            for (ItemDescription itemDescription : items) {
                if (importer.selectedItem.equals(itemDescription)) {
                    itemStackToAdd = itemDescription.createItemStack();
                    break;
                }
            }
        }

        double price = importer.selectedItem == null ? -1 : PriceManager.getImporterBuyPrice(importer.selectedItem, 1);

        if (ticketItemStack.isEmpty() || !importer.canAddItem(itemStackToAdd, slotsOutput) || price < 0 || importer.currency.compareTo(new BigDecimal(price)) < 0) {
            if (importer.progress >= 0.f) {
                importer.progress = Math.max(0.f, importer.progress - 0.01f);
            }
        } else {
            double progressPerTick = importer.getProgressPerTick();
            int energyUsage = importer.getEnergyUsagePerTick();

            if (importer.getEnergyStored() < energyUsage || !importer.doesRedstoneSettingMatchWorld(level, pos)) {
                if (importer.progress >= 0.f) {
                    importer.progress = Math.max(0.f, importer.progress - 0.01f);
                }
            } else {
                importer.energyStorage.extractEnergy(energyUsage, false);
                importer.progress += progressPerTick;

                if (importer.progress >= 1.f) {
                    importer.buyItems(itemStackToAdd, price, ticketItemStack);
                    importer.progress = 0.f;

                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.5f);
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.25, pos.getZ() + 0.5, 3, 0.2, 0.15, 0.2, 0.25);
                }
            }
        }

        importer.markUpdated();
    }

    public void buyItems(ItemStack itemStackToBuy, double price, ItemStack ticketItem) {
        Supplier<Integer> availableSpaces = () -> {
            int spaces = 0;

            for (int slotNumber: slotsOutput) {
                ItemStack slot = getItem(slotNumber);

                if (slot.isEmpty()) {
                    spaces += itemStackToBuy.getMaxStackSize();
                } else if (slot.is(itemStackToBuy.getItem())) {
                    spaces += slot.getMaxStackSize() - slot.getCount();
                }
            }

            return spaces;
        };

        Supplier<BigDecimal> amountAffordable = () -> getBalance().divide(BigDecimal.valueOf(price), RoundingMode.FLOOR);

        int maxProcesses = TicketItem.getMaxProcessCount(ticketItem);
        int processCount = 0;

        while (processCount < maxProcesses && availableSpaces.get() > 0 && amountAffordable.get().intValue() > 0) {
            ItemStack stack = new ItemStack(
                    itemStackToBuy.getItem(),
                    amountAffordable.get()
                            .min(BigDecimal.valueOf(Math.min(availableSpaces.get(), Math.min(maxProcesses - processCount, itemStackToBuy.getMaxStackSize())))).intValue()
            );
            BigDecimal totalPrice = BigDecimal.valueOf(price).multiply(BigDecimal.valueOf(stack.getCount()));

            if (!canAddItem(stack, slotsOutput) || getBalance().compareTo(totalPrice) < 0) break;
            currency = currency.subtract(totalPrice);
            addItem(stack, slotsOutput);

            processCount += stack.getCount();
        }

        TicketItem.handleDamageWithSound(ticketItem, 1, level, worldPosition);
    }

    @Override
    public void selectItem(ItemDescription itemDescription) {
        this.selectedItem = itemDescription;
    }

    @Override
    public ItemDescription getSelectedItem() {
        return this.selectedItem;
    }
}
