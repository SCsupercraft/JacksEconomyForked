package me.khajiitos.jackseconomy.packet;

import io.netty.buffer.ByteBuf;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record AdminShopSchemaPacket(CompoundTag data, Optional<String> adminShopName, boolean oneItemCurrencyMode) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AdminShopSchemaPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "admin_shop_schema"));

    public static final StreamCodec<ByteBuf, AdminShopSchemaPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            AdminShopSchemaPacket::data,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            AdminShopSchemaPacket::adminShopName,
            ByteBufCodecs.BOOL,
            AdminShopSchemaPacket::oneItemCurrencyMode,
            AdminShopSchemaPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
