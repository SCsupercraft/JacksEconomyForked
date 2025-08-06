package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.blockentity.IFluidImporterBlockEntity;
import me.khajiitos.jackseconomy.menu.IBlockEntityContainer;
import me.khajiitos.jackseconomy.packet.ChangeSelectedFluidPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ChangeSelectedFluidHandler {
    public static void handle(ChangeSelectedFluidPacket msg, final IPayloadContext context) {
        ServerPlayer sender = (ServerPlayer) context.player();

        if (sender.containerMenu instanceof IBlockEntityContainer<?> blockEntityContainer && blockEntityContainer.getBlockEntity() instanceof IFluidImporterBlockEntity importerBlockEntity) {
            importerBlockEntity.selectFluid(msg.selectedFluid());
            importerBlockEntity.markUpdated();
        }
    }
}
