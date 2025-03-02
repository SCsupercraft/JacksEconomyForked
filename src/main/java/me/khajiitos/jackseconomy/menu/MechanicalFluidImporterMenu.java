package me.khajiitos.jackseconomy.menu;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.blockentity.FluidTransactionKineticMachineBlockEntity;
import me.khajiitos.jackseconomy.blockentity.MechanicalFluidImporterBlockEntity;
import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy.item.FluidImporterTicketItem;
import me.khajiitos.jackseconomy.item.ImporterTicketItem;
import me.khajiitos.jackseconomy.util.FilteredSlot;
import me.khajiitos.jackseconomy.util.RedstoneToggle;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MechanicalFluidImporterMenu extends KineticFluidTransactionMachineMenu {
    public final RedstoneToggle redstoneToggle;

    public MechanicalFluidImporterMenu(int containerID, Inventory playerInv, FluidTransactionKineticMachineBlockEntity blockEntity) {
        super(ContainerReg.MECHANICAL_FLUID_IMPORTER_MENU.get(), containerID, blockEntity);
        if (blockEntity instanceof MechanicalFluidImporterBlockEntity importerBlockEntity) {
            this.redstoneToggle = importerBlockEntity.getRedstoneToggle();
        } else {
            this.redstoneToggle = RedstoneToggle.IGNORED;
        }

        for (int row = 0; row < 3; row++) {
            this.addSlot(new FilteredSlot(blockEntity, row, 8, 21 + row * 18, null, itemStack -> !(itemStack.getItem() instanceof ImporterTicketItem)));
        }

        this.addSlot(new FilteredSlot(blockEntity, 3, 40, 48, new ResourceLocation(JacksEconomy.MOD_ID, "gui/ticket_slot"), itemStack -> itemStack.getItem() instanceof FluidImporterTicketItem));

        this.addPlayerInventory(playerInv, 95);
    }

    public MechanicalFluidImporterMenu(int containerID, Inventory playerInv, BlockPos pos) {
        this(containerID, playerInv, (FluidTransactionKineticMachineBlockEntity) playerInv.player.level().getBlockEntity(pos));
    }

    @Override
    public int getContainerSize() {
        return 4;
    }
}
