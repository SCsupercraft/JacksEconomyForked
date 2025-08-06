package me.khajiitos.jackseconomy.packet;

import io.netty.buffer.ByteBuf;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AdminShopColorPacket(ListTag colors, int defaultColor) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AdminShopColorPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "admin_shop_color"));

    public static final StreamCodec<ByteBuf, AdminShopColorPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.TAG,
            AdminShopColorPacket::colors,
            ByteBufCodecs.INT,
            AdminShopColorPacket::defaultColor,
            AdminShopColorPacket::create
    );

    public static AdminShopColorPacket create(Tag colors, int defaultColor) {
        return new AdminShopColorPacket((ListTag) colors, defaultColor);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
