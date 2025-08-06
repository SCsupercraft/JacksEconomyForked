package me.khajiitos.jackseconomy.packet;

import io.netty.buffer.ByteBuf;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.util.CurrencyType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChangeCurrencyTypePacket(CurrencyType currencyType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ChangeCurrencyTypePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "change_currency_type"));

    public static final StreamCodec<ByteBuf, ChangeCurrencyTypePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(
                    CurrencyType::valueOf,
                    CurrencyType::name
            ),
            ChangeCurrencyTypePacket::currencyType,
            ChangeCurrencyTypePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
