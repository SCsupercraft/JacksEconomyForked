package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.gamestages.GameStagesManager;
import me.khajiitos.jackseconomy.packet.AcknowledgeUnlocksPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class AcknowledgeUnlocksHandler {
    public static void handle(AcknowledgeUnlocksPacket msg, final IPayloadContext context) {
        ServerPlayer sender = (ServerPlayer) context.player();
        GameStagesManager.acknowledgeUnlocks(sender, msg.newShopUnlocks());
    }
}
