package me.khajiitos.jackseconomy.blockentity;

import me.khajiitos.jackseconomy.block.TransactionMachineBlock;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.data.PurchaseManager;
import me.khajiitos.jackseconomy.data.price.FluidDescription;
import me.khajiitos.jackseconomy.data.price.PriceManager;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import me.khajiitos.jackseconomy.item.CurrencyItem;
import me.khajiitos.jackseconomy.item.FluidTicketItem;
import me.khajiitos.jackseconomy.item.TicketItem;
import me.khajiitos.jackseconomy.menu.FluidImporterMenu;
import me.khajiitos.jackseconomy.util.RedstoneToggle;
import me.khajiitos.jackseconomy.util.SideConfig;
import me.khajiitos.jackseconomy.util.SlottedItemStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

public class FluidImporterBlockEntity extends FluidTransactionMachineBlockEntity implements IFluidImporterBlockEntity {
    public static final int[] slotsInput = new int[]{0, 1, 2};
    public static final int slotTicket = 3;
    protected SlottedItemStackHandler itemHandlerInput;
    protected SlottedItemStackHandler itemHandlerRejectionOutput;
    protected LazyOptional<IItemHandler> itemHandlerInputLazy = LazyOptional.of(() -> itemHandlerInput);
    protected LazyOptional<IItemHandler> itemHandlerRejectionOutputLazy = LazyOptional.of(() -> itemHandlerRejectionOutput);

    public FluidDescription selectedFluid;
    private float progress;

    public FluidImporterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityReg.FLUID_IMPORTER.get(), pos, state);
        itemHandlerInput = new SlottedItemStackHandler(this.items, slotsInput, true, false);
        itemHandlerRejectionOutput = new SlottedItemStackHandler(this.items, slotsInput, false, true, this::isItemRejected);

        fluidStorage.setCapacity(20000);
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
        return Component.translatable("block.jackseconomy.fluid_importer");
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
        return 4;
    }

    public float getProgress() {
        return progress;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new FluidImporterMenu(pContainerId, pPlayerInventory, this);
    }

    @Override
    public void saveMachineData(CompoundTag tag) {
        super.saveMachineData(tag);
        tag.putFloat("Progress", this.progress);

        if (this.selectedFluid != null) {
            tag.put("SelectedFluid", this.selectedFluid.toNbt());
        }
    }

    @Override
    public void loadMachineData(CompoundTag tag) {
        super.loadMachineData(tag);

        // When items are loaded, the items array is a completely new array
        this.itemHandlerInput.changeItems(this.items);
        this.itemHandlerRejectionOutput.changeItems(this.items);

        this.progress = tag.getFloat("Progress");

        if (tag.contains("SelectedFluid")) {
            this.selectedFluid = FluidDescription.fromNbt(tag.getCompound("SelectedFluid"));
        } else {
            this.selectedFluid = null;
        }
    }

    @Override
    public int[] getSlotsForFace(Direction pSide) {
        Direction facing = getBlockState().getValue(TransactionMachineBlock.FACING);

        switch (this.sideConfig.getValue(SideConfig.directionRelative(facing, pSide))) {
            case INPUT -> {
                return slotsInput;
            }
            case REJECTION_OUTPUT -> {
                return Arrays.stream(slotsInput).filter(slot -> isItemRejected(this.items.get(slot))).toArray();
            }
        }
        return new int[]{};
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        Direction facing = this.getBlockState().getValue(TransactionMachineBlock.FACING);

        if (cap == ForgeCapabilities.ITEM_HANDLER && side != null) {
            switch (sideConfig.getValue(SideConfig.directionRelative(facing, side))) {
                case INPUT -> {
                    return itemHandlerInputLazy.cast();
                }
                case REJECTION_OUTPUT -> {
                    return itemHandlerRejectionOutputLazy.cast();
                }
            }
        } else if (cap == ForgeCapabilities.FLUID_HANDLER && side != null && (sideConfig.getValue(SideConfig.directionRelative(facing, side)) == SideConfig.Value.OUTPUT)) {
            return lazyOutputFluidStorage.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandlerInputLazy.invalidate();
        itemHandlerRejectionOutputLazy.invalidate();
    }

    public double getProgressPerTick() {
        double baseProgress = Config.baseImporterProgressPerTick.get();
        return baseProgress + this.speed * (baseProgress * 10);
    }

    public int getEnergyUsagePerTick() {
        return Config.baseImporterEnergyUsage.get() + (int)Math.ceil(Math.pow(this.speed * 64.0, 1.25));
    }

    // what a stupid name
    // I agree
    private boolean doesRedstoneSettingMatchWorld(Level level, BlockPos pos) {
        return (this.redstoneToggle == RedstoneToggle.SIGNAL_ON && level.hasNeighborSignal(pos)) || (this.redstoneToggle == RedstoneToggle.SIGNAL_OFF && !level.hasNeighborSignal(pos)) || this.redstoneToggle == RedstoneToggle.IGNORED;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FluidImporterBlockEntity importer) {
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
        List<FluidDescription> fluids = FluidTicketItem.getFluids(ticketItemStack);

        FluidDescription selectedDescription = null;
        FluidStack fluidToBuy = FluidStack.EMPTY;

        if (!fluids.isEmpty() && importer.selectedFluid == null) {
            importer.selectedFluid = fluids.get(0);
        }

        if (importer.selectedFluid != null) {
            for (FluidDescription fluidDescription : fluids) {
                if (importer.selectedFluid.equals(fluidDescription)) {
                    fluidToBuy = fluidDescription.createFluidStack();
                    selectedDescription = fluidDescription;
                    break;
                }
            }
        }

        int capacity = importer.fluidStorage.getCapacity();
        int amount = importer.fluidStorage.getFluidAmount();
        int left = capacity - amount;

        double price = PriceManager.getFluidImporterBuyPrice(importer.selectedFluid, 1);

        if (ticketItemStack.isEmpty() || price < 0 || left < 1 || importer.currency.compareTo(new BigDecimal(price)) < 0) {
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
                    importer.buyFluid(fluidToBuy, selectedDescription, price, ticketItemStack);
                    importer.progress = 0.f;

                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.5f);
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.25, pos.getZ() + 0.5, 3, 0.2, 0.15, 0.2, 0.25);
                }
            }
        }

        importer.markUpdated();
    }

    public void buyFluid(FluidStack fluidStackToBuy, FluidDescription selectedDescription, double price, ItemStack ticketItem) {
        BigDecimal amountAffordable = getBalance().divide(BigDecimal.valueOf(price), RoundingMode.FLOOR);

        int maxProcesses = TicketItem.getMaxProcessCount(ticketItem);

        FluidStack stack = fluidStackToBuy.copy();
        stack.setAmount(
                amountAffordable
                        .min(BigDecimal.valueOf(Math.min(fluidStorage.getCapacity() - fluidStorage.getFluidAmount(), maxProcesses))).intValue()
        );
        BigDecimal totalPrice = BigDecimal.valueOf(price).multiply(BigDecimal.valueOf(stack.getAmount()));

        if ((!fluidStorage.getFluid().isFluidEqual(stack) && !fluidStorage.isEmpty()) || getBalance().compareTo(totalPrice) < 0) return;
        currency = currency.subtract(totalPrice);
        fluidStorage.fill(stack, IFluidHandler.FluidAction.EXECUTE);

        TicketItem.handleDamageWithSound(ticketItem, 1, level, worldPosition);

        PurchaseManager.addPurchase(PurchaseManager.Purchase.of(
                selectedDescription,
                stack.getAmount(),
                totalPrice.doubleValue(),
                PurchaseManager.timestamp(),
                PurchaseManager.PurchaseSource.FLUID_IMPORTER
        ), this.worldPosition, (ServerLevel) this.level);
    }

    @Override
    public void selectFluid(FluidDescription fluidDescription) {
        this.selectedFluid = fluidDescription;
    }

    @Override
    public FluidDescription getSelectedFluid() {
        return this.selectedFluid;
    }
}
