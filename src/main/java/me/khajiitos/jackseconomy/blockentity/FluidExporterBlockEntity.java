package me.khajiitos.jackseconomy.blockentity;

import me.khajiitos.jackseconomy.block.TransactionMachineBlock;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.data.PurchaseManager;
import me.khajiitos.jackseconomy.data.price.FluidDescription;
import me.khajiitos.jackseconomy.data.price.PriceManager;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import me.khajiitos.jackseconomy.item.*;
import me.khajiitos.jackseconomy.menu.FluidExporterMenu;
import me.khajiitos.jackseconomy.util.*;
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
import java.util.List;

public class FluidExporterBlockEntity extends FluidTransactionMachineBlockEntity implements IFluidExporterBlockEntity {
    public static final int[] slotsOutput = new int[]{0, 1, 2, 3, 4, 5};
    public static final int slotTicket = 6;
    private float progress = 0.f;
    protected AdvancedFluidTank.SuppliedFluidTank rejectedFluidStorage = new AdvancedFluidTank.SuppliedFluidTank(this::getFluidStorage, false, true);
    protected SlottedItemStackHandler itemHandlerOutput = new SlottedItemStackHandler(this.items, slotsOutput, false, true, itemStack -> itemStack.getItem() instanceof CurrencyItem);

    public FluidExporterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityReg.FLUID_EXPORTER.get(), pos, state);
        rejectedFluidStorage.setValidator(this::isFluidRejected);
    }

    @Override
    public BigDecimal getTotalBalance() {
        BigDecimal balance = this.getBalance();

        for (int slot : slotsOutput) {
            ItemStack itemStack = this.items.get(slot);

            if (itemStack.getItem() instanceof CurrencyItem currencyItem) {
                balance = balance.add(currencyItem.value.multiply(BigDecimal.valueOf(itemStack.getCount())));
            }
        }

        return balance;
    }

    protected boolean isFluidRejected(FluidStack fluidStack) {
        ItemStack ticketItem = this.items.get(slotTicket);
        boolean isOnTicket = (ticketItem.getItem() instanceof GoldenFluidExporterTicketItem || (ticketItem.getItem() instanceof FluidExporterTicketItem && FluidExporterTicketItem.getFluids(ticketItem).contains(FluidDescription.ofFluid(fluidStack))));
        return !isOnTicket || PriceManager.getFluidExporterSellPrice(FluidDescription.ofFluid(fluidStack), 1) == -1;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.jackseconomy.fluid_exporter");
    }

    @Override
    public int getContainerSize() {
        return 7;
    }

    public float getProgress() {
        return progress;
    }

    public double getProgressPerTick() {
        double baseProgress = Config.baseExporterProgressPerTick.get();
        return baseProgress + this.speed * (baseProgress * 10);
    }

    public int getEnergyUsagePerTick() {
        return Config.baseExporterEnergyUsage.get() + (int)Math.ceil(Math.pow(this.speed * 64.0, 1.25));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FluidExporterBlockEntity exporter) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        exporter.updateCoinsOutput();

        boolean progress = false;
        boolean canProgress = false;

        FluidStack fluidStack = exporter.fluidStorage.getFluid();
        ItemStack ticketItem = exporter.items.get(slotTicket);

        if (ticketItem.getItem() instanceof FluidExporterTicketItem && exporter.getTotalBalance().compareTo(BigDecimal.valueOf(Config.maxExporterBalance.get())) < 0) {
            if ((exporter.redstoneToggle == RedstoneToggle.SIGNAL_ON && level.hasNeighborSignal(pos)) || (exporter.redstoneToggle == RedstoneToggle.SIGNAL_OFF && !level.hasNeighborSignal(pos)) || exporter.redstoneToggle == RedstoneToggle.IGNORED) {
                FluidDescription fluidDescription = FluidDescription.ofFluid(fluidStack);
                if (!fluidStack.isEmpty() && PriceManager.getFluidExporterSellPrice(fluidDescription, 1) != -1) {
                    if (ticketItem.getItem() instanceof GoldenFluidExporterTicketItem || FluidTicketItem.getFluids(ticketItem).stream().anyMatch(desc -> desc.equals(fluidDescription))) {
                        canProgress = true;
                    }
                }
            }
        }

        if (canProgress) {
            double progressPerTick = exporter.getProgressPerTick();
            int energyUsage = exporter.getEnergyUsagePerTick();

            if (exporter.getEnergyStored() >= energyUsage) {
                progress = true;
                exporter.energyStorage.extractEnergy(energyUsage, false);
                exporter.progress += progressPerTick;

                if (exporter.progress >= 1.f) {
                    exporter.progress = 0.f;

                    if (!exporter.sellFluid()) {
                        // This should technically never happen
                        throw new RuntimeException();
                    }

                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.5f);
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.25, pos.getZ() + 0.5, 3, 0.2, 0.15, 0.2, 0.25);
                }
            }
        }

        if (!progress) {
            exporter.progress = Math.max(0.f, exporter.progress - 0.005f);
        }

        exporter.markUpdated();
    }

    public void updateCoinsOutput() {
        BigDecimal worth = BigDecimal.ZERO;
        for (int slot : slotsOutput) {
            ItemStack itemStack = this.items.get(slot);

            if (itemStack.getItem() instanceof CurrencyItem coin) {
                worth = worth.add(coin.value.multiply(new BigDecimal(itemStack.getCount())));
            }

            itemStack.setCount(0);
        }

        this.currency = this.currency.add(worth);

        List<ItemStack> items = CurrencyHelper.getCurrencyItems(this.currency);

        for (int i = 0; i < Math.min(6, items.size()); i++) {
            ItemStack itemStack = items.get(i);
            int slot = slotsOutput[i];
            this.items.set(slot, itemStack);

            BigDecimal thisWorth = itemStack.getItem() instanceof CurrencyItem currencyItem ? currencyItem.value.multiply(new BigDecimal(itemStack.getCount())) : BigDecimal.ZERO;
            this.currency = this.currency.subtract(thisWorth);
        }
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction pSide) {
        Direction facing = getBlockState().getValue(TransactionMachineBlock.FACING);

        if (this.sideConfig.getValue(SideConfig.directionRelative(facing, pSide)) == SideConfig.Value.OUTPUT) {
            return slotsOutput;
        }
        return new int[]{};
    }

    @Override
    public IItemHandler getItemCapability(Direction direction) {
        Direction facing = this.getBlockState().getValue(TransactionMachineBlock.FACING);
        return direction == null || sideConfig.getValue(SideConfig.directionRelative(facing, direction)) == SideConfig.Value.OUTPUT ? itemHandlerOutput : null;
    }

    @Override
    public IFluidHandler getFluidCapability(Direction direction) {
        Direction facing = this.getBlockState().getValue(TransactionMachineBlock.FACING);
        switch (sideConfig.getValue(SideConfig.directionRelative(facing, direction))) {
            case INPUT -> {
                return inputFluidStorage;
            }
            case OUTPUT -> {
                return outputFluidStorage;
            }
            case REJECTION_OUTPUT -> {
                return rejectedFluidStorage;
            }
        }
        return null;
    }

    @Override
    public void saveMachineData(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveMachineData(tag, provider);

        tag.putFloat("Progress", this.progress);
    }

    @Override
    public void loadMachineData(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadMachineData(tag, provider);

        // When items are loaded, the items array is a completely new array
        this.itemHandlerOutput.changeItems(this.items);

        this.progress = tag.getFloat("Progress");
    }

    public boolean sellFluid() {
        FluidDescription description = FluidDescription.ofFluid(fluidStorage.getFluid());
        double sellPrice = PriceManager.getFluidExporterSellPrice(description, 1);

        if (sellPrice == -1.0) {
            return false;
        }

        ItemStack ticketItem = getItem(slotTicket);

        int amountSold = fluidStorage.drain(TicketItem.getMaxProcessCount(ticketItem), AdvancedFluidTank.FluidAction.EXECUTE).getAmount();
        this.currency = this.currency.add(BigDecimal.valueOf(sellPrice * amountSold));

        TicketItem.handleDamageWithSound(ticketItem, 1, level, worldPosition);

        PurchaseManager.addPurchase(PurchaseManager.Purchase.of(
                description,
                amountSold * -1,
                sellPrice * amountSold,
                PurchaseManager.timestamp(),
                PurchaseManager.PurchaseSource.FLUID_EXPORTER
        ), this.worldPosition, (ServerLevel) this.level);

        return true;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new FluidExporterMenu(containerId, inventory, this);
    }

    @Override
    public boolean canPlaceItemThroughFace(int pIndex, @NotNull ItemStack pItemStack, @Nullable Direction pDirection) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int pIndex, @NotNull ItemStack pStack, @NotNull Direction pDirection) {
        return true;
    }
}
