package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.blockentity.IFluidImporterBlockEntity;
import me.khajiitos.jackseconomy.menu.IBlockEntityContainer;
import me.khajiitos.jackseconomy.packet.ChangeSelectedFluidPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChangeSelectedFluidHandler {
    public static void handle(ChangeSelectedFluidPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();

        if (sender == null) {
            return;
        }

        if (sender.containerMenu instanceof IBlockEntityContainer<?> blockEntityContainer && blockEntityContainer.getBlockEntity() instanceof IFluidImporterBlockEntity importerBlockEntity) {
            importerBlockEntity.selectFluid(msg.selectedFluid());
            importerBlockEntity.markUpdated();
        }
    }
}
