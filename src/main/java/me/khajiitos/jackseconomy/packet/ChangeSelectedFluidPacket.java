package me.khajiitos.jackseconomy.packet;

import io.netty.buffer.ByteBuf;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.data.price.FluidDescription;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChangeSelectedFluidPacket(FluidDescription selectedFluid) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ChangeSelectedFluidPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "change_selected_fluid"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeSelectedFluidPacket> STREAM_CODEC = StreamCodec.composite(
            FluidDescription.STREAM_CODEC,
            ChangeSelectedFluidPacket::selectedFluid,
            ChangeSelectedFluidPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
