package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.data.price.PriceManager;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.packet.AdminShopSchemaPacket;
import me.khajiitos.jackseconomy.packet.RequestAdminShopSchemaPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestAdminShopSchemaHandler {
    public static void handle(RequestAdminShopSchemaPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        if (sender == null || !sender.hasPermissions(4)) return;

        CompoundTag compoundTag = PriceManager.toAdminShopSchemaCompound(sender, msg.adminShopName());
        Packets.sendToClient(sender, new AdminShopSchemaPacket(compoundTag, msg.adminShopName()));
    }
}
