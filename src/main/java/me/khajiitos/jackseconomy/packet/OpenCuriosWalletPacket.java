package me.khajiitos.jackseconomy.packet;

import io.netty.buffer.ByteBuf;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record  OpenCuriosWalletPacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenCuriosWalletPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "open_curios_wallet"));

    public static final StreamCodec<ByteBuf, OpenCuriosWalletPacket> STREAM_CODEC = StreamCodec.unit(new OpenCuriosWalletPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
