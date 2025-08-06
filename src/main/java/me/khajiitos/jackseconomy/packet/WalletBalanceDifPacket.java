package me.khajiitos.jackseconomy.packet;

import io.netty.buffer.ByteBuf;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.util.Utils;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.math.BigDecimal;

public record WalletBalanceDifPacket(BigDecimal delta) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WalletBalanceDifPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "wallet_balance_dif"));

    public static final StreamCodec<ByteBuf, WalletBalanceDifPacket> STREAM_CODEC = StreamCodec.composite(
            Utils.BIG_DECIMAL_STREAM_CODEC,
            WalletBalanceDifPacket::delta,
            WalletBalanceDifPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
