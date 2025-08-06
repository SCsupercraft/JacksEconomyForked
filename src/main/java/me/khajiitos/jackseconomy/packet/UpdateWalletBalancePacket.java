package me.khajiitos.jackseconomy.packet;

import io.netty.buffer.ByteBuf;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.util.Utils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.math.BigDecimal;

public record UpdateWalletBalancePacket(BigDecimal balance) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateWalletBalancePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "update_wallet_balance"));

    public static final StreamCodec<ByteBuf, UpdateWalletBalancePacket> STREAM_CODEC = StreamCodec.composite(
            Utils.BIG_DECIMAL_STREAM_CODEC,
            UpdateWalletBalancePacket::balance,
            UpdateWalletBalancePacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
