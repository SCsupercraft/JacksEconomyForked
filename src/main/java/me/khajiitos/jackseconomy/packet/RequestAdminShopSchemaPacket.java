package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.packet.handler.RequestAdminShopSchemaHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record RequestAdminShopSchemaPacket(@Nullable String adminShopName) {
    public static void encode(RequestAdminShopSchemaPacket msg, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(msg.adminShopName != null);
        if (msg.adminShopName != null) friendlyByteBuf.writeUtf(msg.adminShopName);
    }

    public static RequestAdminShopSchemaPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new RequestAdminShopSchemaPacket(friendlyByteBuf.readBoolean() ? friendlyByteBuf.readUtf() : null);
    }

    public static void handle(RequestAdminShopSchemaPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> RequestAdminShopSchemaHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
