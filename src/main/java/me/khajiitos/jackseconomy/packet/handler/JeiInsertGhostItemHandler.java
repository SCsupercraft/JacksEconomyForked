package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.init.ComponentReg;
import me.khajiitos.jackseconomy.menu.FluidTicketCreatorMenu;
import me.khajiitos.jackseconomy.menu.TicketCreatorMenu;
import me.khajiitos.jackseconomy.packet.JeiInsertGhostItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class JeiInsertGhostItemHandler {
    public static void handle(JeiInsertGhostItemPacket msg, final IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sender))
            return;

        if (!sender.isCreative()) {
            return;
        }

        ItemStack stack = msg.item();
        ComponentReg.addGhostItem(stack);

        if (sender.containerMenu instanceof TicketCreatorMenu menu) {
            if (menu.container.getItem(msg.slot()).isEmpty())
                menu.container.setItem(msg.slot(), stack);
        } else if (sender.containerMenu instanceof FluidTicketCreatorMenu menu) {
            if (menu.container.getItem(msg.slot()).isEmpty() && FluidTicketCreatorMenu.mayPlace(stack))
                menu.container.setItem(msg.slot(), stack);
        }
    }
}
