package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.data.price.FluidDescription;
import me.khajiitos.jackseconomy.packet.handler.ChangeSelectedFluidHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ChangeSelectedFluidPacket(FluidDescription selectedFluid) {
    public static void encode(ChangeSelectedFluidPacket msg, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeNbt(msg.selectedFluid.toNbt());
    }

    public static ChangeSelectedFluidPacket decode(FriendlyByteBuf friendlyByteBuf) {
        CompoundTag nbt = friendlyByteBuf.readAnySizeNbt();
        return new ChangeSelectedFluidPacket(FluidDescription.fromNbt(nbt == null ? new CompoundTag() : nbt));
    }

    public static void handle(ChangeSelectedFluidPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ChangeSelectedFluidHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
