package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record JeiInsertGhostItemPacket(ItemStack item, int slot) implements CustomPacketPayload {
    public static final Type<JeiInsertGhostItemPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "jei_insert_ghost_item"));

    public static final StreamCodec<RegistryFriendlyByteBuf, JeiInsertGhostItemPacket> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC,
            JeiInsertGhostItemPacket::item,
            ByteBufCodecs.INT,
            JeiInsertGhostItemPacket::slot,
            JeiInsertGhostItemPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
