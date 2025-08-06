package me.khajiitos.jackseconomy.menu;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.blockentity.FluidImporterBlockEntity;
import me.khajiitos.jackseconomy.blockentity.FluidTransactionMachineBlockEntity;
import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy.item.FluidImporterTicketItem;
import me.khajiitos.jackseconomy.item.ImporterTicketItem;
import me.khajiitos.jackseconomy.util.FilteredSlot;
import me.khajiitos.jackseconomy.util.RedstoneToggle;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FluidImporterMenu extends FluidTransactionMachineMenu {
    public final float machineSpeed;
    public final int energy;
    public final RedstoneToggle redstoneToggle;

    public FluidImporterMenu(int containerID, Inventory playerInv, FluidTransactionMachineBlockEntity blockEntity) {
        super(ContainerReg.FLUID_IMPORTER_MENU.get(), containerID, blockEntity);
        if (blockEntity instanceof FluidImporterBlockEntity importerBlockEntity) {
            this.machineSpeed = importerBlockEntity.getSpeed();
            this.energy = importerBlockEntity.getEnergyStored();
            this.redstoneToggle = importerBlockEntity.getRedstoneToggle();
        } else {
            this.machineSpeed = 0.0f;
            this.energy = 0;
            this.redstoneToggle = RedstoneToggle.IGNORED;
        }

        for (int row = 0; row < 3; row++) {
            this.addSlot(new FilteredSlot(blockEntity, row, 8, 21 + row * 18, null, itemStack -> !(itemStack.getItem() instanceof ImporterTicketItem)));
        }

        this.addSlot(new FilteredSlot(blockEntity, 3, 40, 48, ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "gui/ticket_slot"), itemStack -> itemStack.getItem() instanceof FluidImporterTicketItem));

        this.addPlayerInventory(playerInv, 95);
    }

    public FluidImporterMenu(int containerID, Inventory playerInv, BlockPos pos) {
        this(containerID, playerInv, (FluidTransactionMachineBlockEntity) playerInv.player.level().getBlockEntity(pos));
    }

    @Override
    public int getContainerSize() {
        return 4;
    }
}
