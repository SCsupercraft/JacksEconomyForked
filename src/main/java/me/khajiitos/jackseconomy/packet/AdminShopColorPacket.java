package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.packet.handler.AdminShopColorHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AdminShopColorPacket(ListTag colors, int defaultColor) {
    public static void encode(AdminShopColorPacket msg, FriendlyByteBuf friendlyByteBuf) {
        CompoundTag dataCompound = new CompoundTag();
        dataCompound.put("Colors", msg.colors);
        dataCompound.putInt("DefaultColor", msg.defaultColor);
        friendlyByteBuf.writeNbt(dataCompound);
    }

    public static AdminShopColorPacket decode(FriendlyByteBuf friendlyByteBuf) {
        CompoundTag dataCompound = friendlyByteBuf.readAnySizeNbt();

        if (dataCompound == null) {
            return new AdminShopColorPacket(new ListTag(), -1);
        }

        ListTag colors = dataCompound.getList("Colors", Tag.TAG_COMPOUND);
        int defaultColor = dataCompound.getInt("DefaultColor");
        return new AdminShopColorPacket(colors, defaultColor);
    }

    public static void handle(AdminShopColorPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> AdminShopColorHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
