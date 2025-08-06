package me.khajiitos.jackseconomy.packet;

import io.netty.buffer.ByteBuf;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChangeSpeedPacket(float speed) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ChangeSpeedPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "change_speed"));

    public static final StreamCodec<ByteBuf, ChangeSpeedPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            ChangeSpeedPacket::speed,
            ChangeSpeedPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
