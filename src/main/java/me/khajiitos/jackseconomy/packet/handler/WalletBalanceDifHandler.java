package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.JacksEconomyClient;
import me.khajiitos.jackseconomy.packet.WalletBalanceDifPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class WalletBalanceDifHandler {
    public static void handle(WalletBalanceDifPacket msg, final IPayloadContext context) {
        JacksEconomyClient.balanceDifPopup = msg.delta();
        JacksEconomyClient.balanceDifPopupStartMillis = System.currentTimeMillis();
    }
}
