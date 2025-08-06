package me.khajiitos.jackseconomy.packet;

import io.netty.buffer.ByteBuf;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.math.BigDecimal;

public record CreateCheckPacket(BigDecimal amount) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CreateCheckPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "create_check"));

    public static final StreamCodec<ByteBuf, CreateCheckPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            CreateCheckPacket::encode,
            CreateCheckPacket::decode
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public String encode() {
        return amount.toString();
    }

    public static CreateCheckPacket decode(String data) {
        return new CreateCheckPacket(new BigDecimal(data));
    }
}
