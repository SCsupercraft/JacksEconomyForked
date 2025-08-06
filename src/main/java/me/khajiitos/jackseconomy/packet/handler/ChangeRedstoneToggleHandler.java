package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.blockentity.ITransactionMachineBlockEntity;
import me.khajiitos.jackseconomy.menu.IBlockEntityContainer;
import me.khajiitos.jackseconomy.packet.ChangeRedstoneTogglePacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ChangeRedstoneToggleHandler {

    public static void handle(final ChangeRedstoneTogglePacket msg, final IPayloadContext context) {
        if (context.player() instanceof ServerPlayer sender && sender.containerMenu instanceof IBlockEntityContainer<?> blockEntityContainer && blockEntityContainer.getBlockEntity() instanceof ITransactionMachineBlockEntity blockEntity) {
            blockEntity.setRedstoneToggle(msg.redstoneToggle());
            blockEntity.markUpdated();
        }
    }
}
