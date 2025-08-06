package me.khajiitos.jackseconomy.blockentity;

import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

public abstract class TransactionMachineBlockEntity extends BlockEntity implements WorldlyContainer, Container, MenuProvider, Nameable, IItemCapable, IEnergyCapable {
    protected JacksEnergyStorage energyStorage = new JacksEnergyStorage(
            this instanceof ExporterBlockEntity ? Config.maxExporterEnergy.get() : Config.maxImporterEnergy.get(),
            this instanceof ExporterBlockEntity ? Config.maxExporterEnergyReceive.get() : Config.maxImporterEnergyReceive.get(),
            256
    );

    protected BigDecimal currency = BigDecimal.ZERO;
    protected float speed;
    protected RedstoneToggle redstoneToggle = RedstoneToggle.IGNORED;
    protected SideConfig sideConfig = new SideConfig();

    public NonNullList<ItemStack> items;

    public TransactionMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    }

    protected abstract Component getDefaultName();

    public SideConfig getSideConfig() {
        return sideConfig;
    }

    public Component getName() {
        return this.getDefaultName();
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

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        this.saveMachineData(tag, provider);
    }

    public int getEnergyStored() {
        return this.energyStorage.getEnergyStored();
    }
    public IEnergyStorage getEnergyStorage() {
        return this.energyStorage;
    }

    @Override
    public IEnergyStorage getEnergyStorage(Direction direction) {
        return this.energyStorage;
    }

    public void saveMachineData(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt("Energy", this.energyStorage.getEnergyStored());
        tag.putFloat("Speed", this.speed);
        tag.putString("Currency", this.currency.toString());
        tag.putInt("RedstoneToggle", this.redstoneToggle.ordinal());
        tag.put("SideConfig", this.sideConfig.toNbt());
        ContainerHelper.saveAllItems(tag, this.items, provider);
    }

    public void loadMachineData(CompoundTag tag, HolderLookup.Provider provider) {
        this.energyStorage.setEnergy(tag.getInt("Energy"));
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

        ContainerHelper.loadAllItems(tag, this.items, provider);
    }


    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        this.loadMachineData(tag, provider);
    }

    public RedstoneToggle getRedstoneToggle() {
        return this.redstoneToggle;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return this.speed;
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
            } else if (ItemStack.isSameItemSameComponents(slotStack, remainingItems)) {
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

            if (ItemStack.isSameItemSameComponents(itemStack, stackInSlot)) {
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

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider provider) {
        this.loadAdditional(pkt.getTag(), provider);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        this.loadAdditional(tag, provider);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, provider);
        return tag;
    }
}
