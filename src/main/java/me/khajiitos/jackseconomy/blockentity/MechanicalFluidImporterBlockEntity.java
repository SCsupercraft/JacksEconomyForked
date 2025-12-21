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
import me.khajiitos.jackseconomy.menu.MechanicalFluidImporterMenu;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

public class MechanicalFluidImporterBlockEntity extends FluidTransactionKineticMachineBlockEntity implements IFluidImporterBlockEntity {
    protected static final int[] slotsInput = new int[]{0, 1, 2};
    protected static final int slotTicket = 3;
    protected SlottedItemStackHandler itemHandlerInput;
    protected SlottedItemStackHandler itemHandlerRejectionOutput;
    public FluidDescription selectedFluid;
    private float progress;

    public MechanicalFluidImporterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityReg.MECHANICAL_FLUID_IMPORTER.get(), pos, state);
        itemHandlerInput = new SlottedItemStackHandler(this.items, slotsInput, true, false);
        itemHandlerRejectionOutput = new SlottedItemStackHandler(this.items, slotsInput, false, true, this::isItemRejected);

        fluidStorage.setCapacity(20000);
    }

    protected boolean hitCapacityLimit() {
        return getTotalBalance().compareTo(BigDecimal.valueOf(Config.maxImporterBalance.get())) >= 0;
    }

    @Override
    public BigDecimal getTotalBalance() {
        return getBalance();
    }

    protected boolean isItemRejected(ItemStack itemStack) {
        return !(itemStack.getItem() instanceof CurrencyItem);
    }

    @Override
    public boolean canPlaceItemThroughFace(int pIndex, @NotNull ItemStack pItemStack, @Nullable Direction pDirection) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int pIndex, @NotNull ItemStack pStack, @NotNull Direction pDirection) {
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
    public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
        return new MechanicalFluidImporterMenu(pContainerId, pPlayerInventory, this);
    }

    @Override
    public void saveMachineData(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveMachineData(tag, provider);
        tag.putFloat("Progress", this.progress);

        if (this.selectedFluid != null) {
            tag.put("SelectedFluid", this.selectedFluid.toNbt());
        }
    }

    @Override
    public void loadMachineData(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadMachineData(tag, provider);

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
    public int @NotNull [] getSlotsForFace(@NotNull Direction pSide) {
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
    public IItemHandler getItemCapability(Direction direction) {
        Direction facing = this.getBlockState().getValue(TransactionMachineBlock.FACING);
        if (direction == null) return null;
        switch (sideConfig.getValue(SideConfig.directionRelative(facing, direction))) {
            case INPUT -> {
                return itemHandlerInput;
            }
            case REJECTION_OUTPUT -> {
                return itemHandlerRejectionOutput;
            }
        }
        return null;
    }

    @Override
    public IFluidHandler getFluidCapability(Direction direction) {
        Direction facing = this.getBlockState().getValue(TransactionMachineBlock.FACING);
        return direction != null && (sideConfig.getValue(SideConfig.directionRelative(facing, direction)) == SideConfig.Value.OUTPUT) ? outputFluidStorage : null;
    }

    public double getProgressPerTick() {
        return Config.mechanicalImporterProgressPerSpeed.get() * Math.abs(this.getSpeed());
    }

    // what a stupid name
    // I agree
    private boolean doesRedstoneSettingMatchWorld(Level level, BlockPos pos) {
        return (this.redstoneToggle == RedstoneToggle.SIGNAL_ON && level.hasNeighborSignal(pos)) || (this.redstoneToggle == RedstoneToggle.SIGNAL_OFF && !level.hasNeighborSignal(pos)) || this.redstoneToggle == RedstoneToggle.IGNORED;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MechanicalFluidImporterBlockEntity importer) {
        importer.tick();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (importer.getTotalBalance().compareTo(BigDecimal.valueOf(Config.maxImporterBalance.get())) < 0) {
            for (int i = 0; i < 3; i++) {
                ItemStack inputItem = importer.getItem(i);

                if (inputItem.getItem() instanceof CurrencyItem coin) {
                    importer.currency = importer.currency.add(coin.value.multiply(new BigDecimal(inputItem.getCount())));
                    inputItem.setCount(0);
                }
            }
        }

        ItemStack ticketItemStack = importer.items.get(slotTicket);
        List<FluidDescription> fluids = FluidTicketItem.getFluids(ticketItemStack);

        FluidDescription selectedDescription = null;
        FluidStack fluidStackToBuy = FluidStack.EMPTY;

        if (!fluids.isEmpty() && importer.selectedFluid == null) {
            importer.selectedFluid = fluids.get(0);
        }

        if (importer.selectedFluid != null) {
            for (FluidDescription fluidDescription : fluids) {
                if (importer.selectedFluid.equals(fluidDescription)) {
                    fluidStackToBuy = fluidDescription.createFluidStack();
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

            if (!importer.doesRedstoneSettingMatchWorld(level, pos) || progressPerTick <= 0) {
                if (importer.progress >= 0.f) {
                    importer.progress = Math.max(0.f, importer.progress - 0.01f);
                }
            } else {
                importer.progress += progressPerTick;

                if (importer.progress >= 1.f) {
                    importer.buyFluid(fluidStackToBuy, selectedDescription, price, ticketItemStack);
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

        int maxProcesses = FluidTicketItem.getMaxProcessCount(ticketItem);

        FluidStack stack = fluidStackToBuy.copyWithAmount(
                amountAffordable
                        .min(BigDecimal.valueOf(Math.min(fluidStorage.getCapacity() - fluidStorage.getFluidAmount(), maxProcesses))).intValue()
        );
        BigDecimal totalPrice = BigDecimal.valueOf(price).multiply(BigDecimal.valueOf(stack.getAmount()));

        if ((!FluidStack.isSameFluidSameComponents(fluidStorage.getFluid(), stack) && !fluidStorage.isEmpty()) || getBalance().compareTo(totalPrice) < 0) return;
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
    public @NotNull Component getName() {
        return Component.translatable("block.jackseconomy.mechanical_fluid_importer");
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