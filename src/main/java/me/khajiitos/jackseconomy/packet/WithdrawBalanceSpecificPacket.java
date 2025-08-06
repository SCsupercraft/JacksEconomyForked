package me.khajiitos.jackseconomy.packet;

import io.netty.buffer.ByteBuf;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.util.CurrencyType;
import me.khajiitos.jackseconomy.util.Utils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.math.BigDecimal;

public record WithdrawBalanceSpecificPacket(BigDecimal items, CurrencyType currencyType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WithdrawBalanceSpecificPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "withdraw_balance_specific"));

    public static final StreamCodec<ByteBuf, WithdrawBalanceSpecificPacket> STREAM_CODEC = StreamCodec.composite(
            Utils.BIG_DECIMAL_STREAM_CODEC,
            WithdrawBalanceSpecificPacket::items,
            ByteBufCodecs.STRING_UTF8.map(
                    CurrencyType::valueOf,
                    CurrencyType::name
            ),
            WithdrawBalanceSpecificPacket::currencyType,
            WithdrawBalanceSpecificPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}