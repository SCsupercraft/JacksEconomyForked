package me.khajiitos.jackseconomy.packet;

import io.netty.buffer.ByteBuf;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record UpdateAdminShopPacket(CompoundTag data, Optional<String> adminShopName) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateAdminShopPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "update_admin_shop"));

    public static final StreamCodec<ByteBuf, UpdateAdminShopPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            UpdateAdminShopPacket::data,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            UpdateAdminShopPacket::adminShopName,
            UpdateAdminShopPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
