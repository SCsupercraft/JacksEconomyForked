package me.khajiitos.jackseconomy.menu;

import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy.item.EmptyTicketItem;
import net.minecraft.world.entity.player.Inventory;

public class FluidExporterTicketCreatorMenu extends FluidTicketCreatorMenu {
    public FluidExporterTicketCreatorMenu(int pContainerId, Inventory inventory) {
        super(ContainerReg.FLUID_EXPORTER_TICKET_CREATOR_MENU.get(), pContainerId, inventory);
    }

    @Override
    protected EmptyTicketItem.Type getTicketType() {
        return EmptyTicketItem.Type.FLUID_EXPORTER;
    }
}
