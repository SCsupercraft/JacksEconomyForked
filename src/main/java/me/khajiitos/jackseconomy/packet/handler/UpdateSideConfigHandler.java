package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.blockentity.CurrencyConverterBlockEntity;
import me.khajiitos.jackseconomy.blockentity.ISideConfigurable;
import me.khajiitos.jackseconomy.blockentity.ITransactionMachineBlockEntity;
import me.khajiitos.jackseconomy.menu.IBlockEntityContainer;
import me.khajiitos.jackseconomy.packet.UpdateSideConfigPacket;
import me.khajiitos.jackseconomy.util.SideConfig;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class UpdateSideConfigHandler {
    public static void handle(UpdateSideConfigPacket msg, final IPayloadContext context) {
        ServerPlayer sender = (ServerPlayer) context.player();

        if (sender.containerMenu instanceof IBlockEntityContainer<?> blockEntityContainer && blockEntityContainer.getBlockEntity() instanceof ISideConfigurable sideConfigurable) {
            SideConfig sideConfig = sideConfigurable.getSideConfig();

            for (int i = 0; i < msg.sideConfigInts().length; i++) {
                sideConfig.setValue(Direction.values()[i], SideConfig.Value.values()[msg.sideConfigInts()[i]]);
            }

            if (sideConfigurable instanceof ITransactionMachineBlockEntity transactionMachineBlockEntity) {
                transactionMachineBlockEntity.markUpdated();
            } else if (sideConfigurable instanceof CurrencyConverterBlockEntity currencyConverterBlockEntity) {
                currencyConverterBlockEntity.markUpdated();
            }
        }
    }
}
