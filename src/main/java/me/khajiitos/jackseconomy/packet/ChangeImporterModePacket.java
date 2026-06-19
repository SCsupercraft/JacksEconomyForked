package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ChangeImporterModePacket(boolean roundRobin) implements CustomPacketPayload {
    public static final Type<ChangeImporterModePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "change_importer_mode"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeImporterModePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ChangeImporterModePacket::roundRobin,
            ChangeImporterModePacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
