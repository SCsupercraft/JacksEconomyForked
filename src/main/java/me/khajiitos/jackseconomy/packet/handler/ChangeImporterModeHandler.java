package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.blockentity.IFluidImporterBlockEntity;
import me.khajiitos.jackseconomy.blockentity.IImporterBlockEntity;
import me.khajiitos.jackseconomy.menu.IBlockEntityContainer;
import me.khajiitos.jackseconomy.packet.ChangeImporterModePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChangeImporterModeHandler {
    public static void handle(ChangeImporterModePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();

        if (sender == null) {
            return;
        }

        if (sender.containerMenu instanceof IBlockEntityContainer<?> blockEntityContainer
                && blockEntityContainer.getBlockEntity() instanceof IImporterBlockEntity be) {
            be.setRoundRobin(msg.roundRobin());
            be.markUpdated();
        } else if (sender.containerMenu instanceof IBlockEntityContainer<?> blockEntityContainer
                && blockEntityContainer.getBlockEntity() instanceof IFluidImporterBlockEntity be) {
            be.setRoundRobin(msg.roundRobin());
            be.markUpdated();
        }
    }
}
