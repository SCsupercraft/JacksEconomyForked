package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.menu.CurrencyConverterMenu;
import me.khajiitos.jackseconomy.packet.ChangeCurrencyTypePacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ChangeCurrencyTypeHandler {
    public static void handle(ChangeCurrencyTypePacket msg, final IPayloadContext context) {
        ServerPlayer sender = (ServerPlayer) context.player();

        if (sender.containerMenu instanceof CurrencyConverterMenu converterMenu && converterMenu.blockEntity != null) {
            converterMenu.blockEntity.selectedCurrencyType = msg.currencyType();
            converterMenu.blockEntity.markUpdated();
        }
    }
}
