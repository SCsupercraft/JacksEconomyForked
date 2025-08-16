package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.data.price.FluidDescription;
import me.khajiitos.jackseconomy.packet.handler.UpdateFluidPricesHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record UpdateFluidPricesPacket(Type type, List<Pair<FluidDescription, Double>> prices) {
	public static void encode(UpdateFluidPricesPacket msg, FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(msg.type.toString());
		friendlyByteBuf.writeInt(msg.prices.size());
		for (Pair<FluidDescription, Double> pair : msg.prices) {
			friendlyByteBuf.writeNbt(pair.getLeft().toNbt());
			friendlyByteBuf.writeDouble(pair.getRight());
		}
	}

	public static UpdateFluidPricesPacket decode(FriendlyByteBuf friendlyByteBuf) {
		Type type = Type.valueOf(friendlyByteBuf.readUtf());
		List<Pair<FluidDescription, Double>> prices = new ArrayList<>();
		int size = friendlyByteBuf.readInt();
		for (int i = 0; i < size; i++) {
			prices.add(Pair.of(FluidDescription.fromNbt(friendlyByteBuf.readNbt()), friendlyByteBuf.readDouble()));
		}

		return new UpdateFluidPricesPacket(type, prices);
	}

	public static void handle(UpdateFluidPricesPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> UpdateFluidPricesHandler.handle(msg, ctx));
		ctx.get().setPacketHandled(true);
	}

	public enum Type {
		EXPORTER,
		IMPORTER
	}
}
