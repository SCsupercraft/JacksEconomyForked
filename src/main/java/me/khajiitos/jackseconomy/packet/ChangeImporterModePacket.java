package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.packet.handler.ChangeImporterModeHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ChangeImporterModePacket(boolean roundRobin) {
    public static void encode(ChangeImporterModePacket msg, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(msg.roundRobin());
    }

    public static ChangeImporterModePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new ChangeImporterModePacket(friendlyByteBuf.readBoolean());
    }

    public static void handle(ChangeImporterModePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ChangeImporterModeHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
