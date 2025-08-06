package me.khajiitos.jackseconomy.menu;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.blockentity.ExporterBlockEntity;
import me.khajiitos.jackseconomy.blockentity.TransactionMachineBlockEntity;
import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy.item.ExporterTicketItem;
import me.khajiitos.jackseconomy.util.FilteredSlot;
import me.khajiitos.jackseconomy.util.OutputSlot;
import me.khajiitos.jackseconomy.util.RedstoneToggle;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ExporterMenu extends TransactionMachineMenu {
    public final float machineSpeed;
    public final int energy;
    public final RedstoneToggle redstoneToggle;

    public ExporterMenu(int containerID, Inventory playerInv, TransactionMachineBlockEntity blockEntity) {
        super(ContainerReg.EXPORTER_MENU.get(), containerID, blockEntity);

        if (blockEntity instanceof ExporterBlockEntity exporterBlockEntity) {
            this.machineSpeed = exporterBlockEntity.getSpeed();
            this.energy = exporterBlockEntity.getEnergyStored();
            this.redstoneToggle = exporterBlockEntity.getRedstoneToggle();
        } else {
            this.machineSpeed = 0.0f;
            this.energy = 0;
            this.redstoneToggle = RedstoneToggle.IGNORED;
        }

        for (int row = 0; row < 3; row++) {
            this.addSlot(new FilteredSlot(blockEntity, row, 8, 21 + row * 18, null, (itemStack) -> (!(itemStack.getItem() instanceof ExporterTicketItem))));
        }

        for (int i = 0; i < 6; i++) {
            this.addSlot(new OutputSlot(blockEntity, 3 + i, 71 + (i % 2) * 18, 21 + (i / 2) * 18));
        }

        this.addSlot(new FilteredSlot(blockEntity, 9, 40, 48, ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "gui/ticket_slot"), itemStack -> itemStack.getItem() instanceof ExporterTicketItem));

        this.addPlayerInventory(playerInv, 95);
    }

    public ExporterMenu(int containerID, Inventory playerInv, BlockPos pos) {
        this(containerID, playerInv, (TransactionMachineBlockEntity) playerInv.player.level().getBlockEntity(pos));
    }

    @Override
    public int getContainerSize() {
        return 10;
    }
}
