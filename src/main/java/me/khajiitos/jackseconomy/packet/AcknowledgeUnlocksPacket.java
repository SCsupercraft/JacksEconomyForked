package me.khajiitos.jackseconomy.packet;

import io.netty.buffer.ByteBuf;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.util.NewShopUnlocks;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AcknowledgeUnlocksPacket(NewShopUnlocks newShopUnlocks) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AcknowledgeUnlocksPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "acknowledge_unlocks"));

    public static final StreamCodec<ByteBuf, AcknowledgeUnlocksPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG.map(
                    NewShopUnlocks::fromNbt,
                    NewShopUnlocks::toNbt
            ),
            AcknowledgeUnlocksPacket::newShopUnlocks,
            AcknowledgeUnlocksPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
