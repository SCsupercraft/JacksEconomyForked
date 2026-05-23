package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.menu.FluidTicketCreatorMenu;
import me.khajiitos.jackseconomy.menu.TicketCreatorMenu;
import me.khajiitos.jackseconomy.packet.JeiInsertGhostItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class JeiInsertGhostItemHandler {
    public static void handle(JeiInsertGhostItemPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();

        if (sender == null || !sender.isCreative()) {
            return;
        }

        ItemStack stack = msg.item();
        stack.getOrCreateTag().putBoolean("jackseconomy_ghost", true);

        if (sender.containerMenu instanceof TicketCreatorMenu menu) {
            if (menu.container.getItem(msg.slot()).isEmpty())
                menu.container.setItem(msg.slot(), stack);
        } else if (sender.containerMenu instanceof FluidTicketCreatorMenu menu) {
            if (menu.container.getItem(msg.slot()).isEmpty() && FluidTicketCreatorMenu.mayPlace(stack))
                menu.container.setItem(msg.slot(), stack);
        }
    }
}
