package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.data.price.PriceManager;
import me.khajiitos.jackseconomy.packet.AdminShopSchemaPacket;
import me.khajiitos.jackseconomy.packet.RequestAdminShopSchemaPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RequestAdminShopSchemaHandler {
    public static void handle(RequestAdminShopSchemaPacket msg, final IPayloadContext context) {
        ServerPlayer sender = (ServerPlayer) context.player();
        if (!sender.hasPermissions(4)) return;

        CompoundTag compoundTag = PriceManager.toAdminShopSchemaCompound(sender, msg.adminShopName().orElse(null));
        PacketDistributor.sendToPlayer(sender, new AdminShopSchemaPacket(compoundTag, msg.adminShopName()));
    }
}
