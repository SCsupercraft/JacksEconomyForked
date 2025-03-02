package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.packet.handler.ChangeSelectedItemHandler;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ChangeSelectedItemPacket(ItemDescription selectedItem) {
    public static void encode(ChangeSelectedItemPacket msg, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeNbt(msg.selectedItem().toNbt());
    }

    public static ChangeSelectedItemPacket decode(FriendlyByteBuf friendlyByteBuf) {
        CompoundTag nbt = friendlyByteBuf.readAnySizeNbt();
        return new ChangeSelectedItemPacket(ItemDescription.fromNbt(nbt == null ? new CompoundTag() : nbt));
    }

    public static void handle(ChangeSelectedItemPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ChangeSelectedItemHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
