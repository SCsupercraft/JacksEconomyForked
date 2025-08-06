package me.khajiitos.jackseconomy.menu;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.blockentity.FluidTransactionKineticMachineBlockEntity;
import me.khajiitos.jackseconomy.blockentity.MechanicalFluidExporterBlockEntity;
import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy.item.FluidExporterTicketItem;
import me.khajiitos.jackseconomy.util.FilteredSlot;
import me.khajiitos.jackseconomy.util.OutputSlot;
import me.khajiitos.jackseconomy.util.RedstoneToggle;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MechanicalFluidExporterMenu extends KineticFluidTransactionMachineMenu {
    public final RedstoneToggle redstoneToggle;

    public MechanicalFluidExporterMenu(int containerID, Inventory playerInv, FluidTransactionKineticMachineBlockEntity blockEntity) {
        super(ContainerReg.MECHANICAL_FLUID_EXPORTER_MENU.get(), containerID, blockEntity);

        if (blockEntity instanceof MechanicalFluidExporterBlockEntity exporterBlockEntity) {
            this.redstoneToggle = exporterBlockEntity.getRedstoneToggle();
        } else {
            this.redstoneToggle = RedstoneToggle.IGNORED;
        }

        for (int i = 0; i < 6; i++) {
            this.addSlot(new OutputSlot(blockEntity, i, 71 + (i % 2) * 18, 21 + (i / 2) * 18));
        }

        this.addSlot(new FilteredSlot(blockEntity, 6, 40, 48, ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "gui/ticket_slot"), itemStack -> itemStack.getItem() instanceof FluidExporterTicketItem));

        this.addPlayerInventory(playerInv, 95);
    }

    public MechanicalFluidExporterMenu(int containerID, Inventory playerInv, BlockPos pos) {
        this(containerID, playerInv, (FluidTransactionKineticMachineBlockEntity) playerInv.player.level().getBlockEntity(pos));
    }

    @Override
    public int getContainerSize() {
        return 7;
    }
}
