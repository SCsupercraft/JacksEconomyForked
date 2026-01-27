package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.packet.handler.JeiInsertGhostItemHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record JeiInsertGhostItemPacket(ItemStack item, int slot){

    public static void encode(JeiInsertGhostItemPacket msg, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeItem(msg.item);
        friendlyByteBuf.writeInt(msg.slot);
    }

    public static JeiInsertGhostItemPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new JeiInsertGhostItemPacket(friendlyByteBuf.readItem(), friendlyByteBuf.readInt());
    }

    public static void handle(JeiInsertGhostItemPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> JeiInsertGhostItemHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
