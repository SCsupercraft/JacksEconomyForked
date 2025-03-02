package me.khajiitos.jackseconomy.blockentity;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import me.khajiitos.jackseconomy.util.AdvancedFluidTank;
import me.khajiitos.jackseconomy.util.RedstoneToggle;
import me.khajiitos.jackseconomy.util.SideConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

public abstract class FluidTransactionKineticMachineBlockEntity extends KineticBlockEntity implements WorldlyContainer, Container, MenuProvider, Nameable {
    protected AdvancedFluidTank fluidStorage = new AdvancedFluidTank(
            10000
    ) {
        @Override
        protected void onContentsChanged() {
            FluidTransactionKineticMachineBlockEntity.this.sendUpdate();
        }
    };
    protected AdvancedFluidTank.SuppliedFluidTank inputFluidStorage = new AdvancedFluidTank.SuppliedFluidTank(this::getFluidStorage, true, false);
    protected AdvancedFluidTank.SuppliedFluidTank outputFluidStorage = new AdvancedFluidTank.SuppliedFluidTank(this::getFluidStorage, false, true);
    protected final LazyOptional<IFluidHandler> lazyInputFluidStorage = LazyOptional.of(() -> inputFluidStorage);
    protected final LazyOptional<IFluidHandler> lazyOutputFluidStorage = LazyOptional.of(() -> outputFluidStorage);

    protected BigDecimal currency = BigDecimal.ZERO;
    protected RedstoneToggle redstoneToggle = RedstoneToggle.IGNORED;
    protected SideConfig sideConfig = new SideConfig();

    public NonNullList<ItemStack> items;

    public FluidTransactionKineticMachineBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    }

    private void sendUpdate() {
        if (this.level != null)
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public Component getDisplayName() {
        return this.getName();
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

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        ContainerHelper.saveAllItems(compound, this.items);
        this.saveMachineData(compound);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyInputFluidStorage.invalidate();
        lazyOutputFluidStorage.invalidate();
    }

    public AdvancedFluidTank getFluidStorage() {
        return this.fluidStorage;
    }

    public void saveMachineData(CompoundTag tag) {
        tag.put("Fluid", this.fluidStorage.writeToNBT(new CompoundTag()));
        tag.putFloat("Speed", this.speed);
        tag.putString("Currency", this.currency.toString());
        tag.putInt("RedstoneToggle", this.redstoneToggle.ordinal());
        tag.put("SideConfig", this.sideConfig.toNbt());
        ContainerHelper.saveAllItems(tag, this.items);
    }

    public void loadMachineData(CompoundTag tag) {
        this.fluidStorage.readFromNBT(tag.getCompound("Fluid"));
        this.speed = tag.getFloat("Speed");
        try {
            this.currency = new BigDecimal(tag.getString("Currency"));
        } catch (NumberFormatException e) {
            this.currency = BigDecimal.ZERO;
        }
        int redstoneToggleNum = tag.getInt("RedstoneToggle");
        if (redstoneToggleNum >= 0 && redstoneToggleNum < RedstoneToggle.values().length) {
            this.redstoneToggle = RedstoneToggle.values()[redstoneToggleNum];
        } else {
            this.redstoneToggle = RedstoneToggle.IGNORED;
        }

        this.sideConfig = SideConfig.fromIntArray(tag.getIntArray("SideConfig"));

        ContainerHelper.loadAllItems(tag, this.items);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        //ContainerHelper.loadAllItems(tag, this.items);
        this.loadMachineData(compound);
    }

    public SideConfig getSideConfig() {
        return this.sideConfig;
    }

    public RedstoneToggle getRedstoneToggle() {
        return this.redstoneToggle;
    }

    public BigDecimal getBalance() {
        return this.currency;
    }

    public void setRedstoneToggle(RedstoneToggle redstoneToggle) {
        this.redstoneToggle = redstoneToggle;
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

    public @Nullable boolean canAddItem(ItemStack itemStack, int[] slots) {
        for (int i : slots) {
            ItemStack stackInSlot = this.items.get(i);

            if (stackInSlot.isEmpty()) {
                return true;
            }

            if (ItemStack.isSameItemSameTags(itemStack, stackInSlot)) {
                if (stackInSlot.getCount() + itemStack.getCount() <= stackInSlot.getMaxStackSize()) {
                    return true;
                }
            }
        }

        return false;
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
        this.load(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }
}
