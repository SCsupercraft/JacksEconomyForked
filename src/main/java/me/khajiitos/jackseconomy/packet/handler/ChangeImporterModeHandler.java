package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.blockentity.IFluidImporterBlockEntity;
import me.khajiitos.jackseconomy.blockentity.IImporterBlockEntity;
import me.khajiitos.jackseconomy.menu.IBlockEntityContainer;
import me.khajiitos.jackseconomy.packet.ChangeImporterModePacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ChangeImporterModeHandler {
    public static void handle(ChangeImporterModePacket msg, final IPayloadContext context) {
        ServerPlayer sender = (ServerPlayer) context.player();

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
