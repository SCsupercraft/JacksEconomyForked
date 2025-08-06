package me.khajiitos.jackseconomy.packet;

import io.netty.buffer.ByteBuf;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record RequestAdminShopSchemaPacket(Optional<String> adminShopName) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestAdminShopSchemaPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "request_admin_shop_schema"));

    public static final StreamCodec<ByteBuf, RequestAdminShopSchemaPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            RequestAdminShopSchemaPacket::adminShopName,
            RequestAdminShopSchemaPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
