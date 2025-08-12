package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.packet.handler.AdminShopSchemaHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record AdminShopSchemaPacket(CompoundTag data, @Nullable String adminShopName, boolean oneItemCurrencyMode) {
    public static void encode(AdminShopSchemaPacket msg, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeNbt(msg.data);
        friendlyByteBuf.writeBoolean(msg.adminShopName != null);
        if (msg.adminShopName != null) friendlyByteBuf.writeUtf(msg.adminShopName);
        friendlyByteBuf.writeBoolean(msg.oneItemCurrencyMode());
    }

    public static AdminShopSchemaPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new AdminShopSchemaPacket(friendlyByteBuf.readAnySizeNbt(), friendlyByteBuf.readBoolean() ? friendlyByteBuf.readUtf() : null, friendlyByteBuf.readBoolean());
    }

    public static void handle(AdminShopSchemaPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> AdminShopSchemaHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
