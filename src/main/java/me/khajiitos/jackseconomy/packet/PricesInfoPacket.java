package me.khajiitos.jackseconomy.packet;

import io.netty.buffer.ByteBuf;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PricesInfoPacket(ListTag data, ListTag secondaryData) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PricesInfoPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "prices_info"));

    public static final StreamCodec<ByteBuf, PricesInfoPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.TRUSTED_COMPOUND_TAG,
            PricesInfoPacket::encode,
            PricesInfoPacket::decode
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public CompoundTag encode() {
        CompoundTag dataCompound = new CompoundTag();
        dataCompound.put("Data", this.data);
        dataCompound.put("SecondaryData", this.secondaryData);
        return dataCompound;
    }

    public static PricesInfoPacket decode(CompoundTag tag) {
        ListTag listTag = tag.getList("Data", Tag.TAG_COMPOUND);
        ListTag listTag2 = tag.getList("SecondaryData", Tag.TAG_COMPOUND);
        return new PricesInfoPacket(listTag, listTag2);
    }
}
