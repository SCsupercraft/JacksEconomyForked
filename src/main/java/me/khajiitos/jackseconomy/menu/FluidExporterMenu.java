package me.khajiitos.jackseconomy.menu;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.blockentity.FluidExporterBlockEntity;
import me.khajiitos.jackseconomy.blockentity.FluidTransactionMachineBlockEntity;
import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy.item.FluidExporterTicketItem;
import me.khajiitos.jackseconomy.util.FilteredSlot;
import me.khajiitos.jackseconomy.util.OutputSlot;
import me.khajiitos.jackseconomy.util.RedstoneToggle;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FluidExporterMenu extends FluidTransactionMachineMenu {
    public final float machineSpeed;
    public final int energy;
    public final RedstoneToggle redstoneToggle;

    public FluidExporterMenu(int containerID, Inventory playerInv, FluidTransactionMachineBlockEntity blockEntity) {
        super(ContainerReg.FLUID_EXPORTER_MENU.get(), containerID, blockEntity);

        if (blockEntity instanceof FluidExporterBlockEntity exporterBlockEntity) {
            this.machineSpeed = exporterBlockEntity.getSpeed();
            this.energy = exporterBlockEntity.getEnergyStored();
            this.redstoneToggle = exporterBlockEntity.getRedstoneToggle();
        } else {
            this.machineSpeed = 0.0f;
            this.energy = 0;
            this.redstoneToggle = RedstoneToggle.IGNORED;
        }

        for (int i = 0; i < 6; i++) {
            this.addSlot(new OutputSlot(blockEntity,  i, 71 + (i % 2) * 18, 21 + (i / 2) * 18));
        }

        this.addSlot(new FilteredSlot(blockEntity, 6, 40, 48, ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "gui/ticket_slot"), itemStack -> itemStack.getItem() instanceof FluidExporterTicketItem));

        this.addPlayerInventory(playerInv, 95);
    }

    public FluidExporterMenu(int containerID, Inventory playerInv, BlockPos pos) {
        this(containerID, playerInv, (FluidTransactionMachineBlockEntity) playerInv.player.level().getBlockEntity(pos));
    }

    @Override
    public int getContainerSize() {
        return 7;
    }
}
