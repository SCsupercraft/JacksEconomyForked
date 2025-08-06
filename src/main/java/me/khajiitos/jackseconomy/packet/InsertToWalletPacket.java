package me.khajiitos.jackseconomy.packet;

import io.netty.buffer.ByteBuf;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record InsertToWalletPacket(int slotId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<InsertToWalletPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "insert_to_wallet"));

    public static final StreamCodec<ByteBuf, InsertToWalletPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            InsertToWalletPacket::slotId,
            InsertToWalletPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
