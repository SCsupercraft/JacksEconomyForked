package me.khajiitos.jackseconomy.packet;

import io.netty.buffer.ByteBuf;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChangeSelectedItemPacket(ItemDescription selectedItem) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ChangeSelectedItemPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "change_selected_item"));

    public static final StreamCodec<ByteBuf, ChangeSelectedItemPacket> STREAM_CODEC = StreamCodec.composite(
            ItemDescription.STREAM_CODEC,
            ChangeSelectedItemPacket::selectedItem,
            ChangeSelectedItemPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
