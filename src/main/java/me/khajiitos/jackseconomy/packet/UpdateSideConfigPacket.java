package me.khajiitos.jackseconomy.packet;

import io.netty.buffer.ByteBuf;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.util.Utils;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UpdateSideConfigPacket(int[] sideConfigInts) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateSideConfigPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "update_side_config"));

    public static final StreamCodec<ByteBuf, UpdateSideConfigPacket> STREAM_CODEC = StreamCodec.composite(
            Utils.INT_ARRAY_STREAM_CODEC,
            UpdateSideConfigPacket::sideConfigInts,
            UpdateSideConfigPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
