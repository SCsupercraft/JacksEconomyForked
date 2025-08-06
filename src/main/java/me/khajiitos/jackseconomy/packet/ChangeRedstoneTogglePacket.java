package me.khajiitos.jackseconomy.packet;

import io.netty.buffer.ByteBuf;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.util.RedstoneToggle;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChangeRedstoneTogglePacket(RedstoneToggle redstoneToggle) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ChangeRedstoneTogglePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "change_redstone_toggle"));

    public static final StreamCodec<ByteBuf, ChangeRedstoneTogglePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(
                    RedstoneToggle::valueOf,
                    RedstoneToggle::name
            ),
            ChangeRedstoneTogglePacket::redstoneToggle,
            ChangeRedstoneTogglePacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
