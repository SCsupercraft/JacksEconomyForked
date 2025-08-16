package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.data.price.ItemDescription;
import me.khajiitos.jackseconomy.packet.handler.UpdateItemPricesHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record UpdateItemPricesPacket(Type type, List<Pair<ItemDescription, Double>> prices) {
	public static void encode(UpdateItemPricesPacket msg, FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(msg.type.toString());
		friendlyByteBuf.writeInt(msg.prices.size());
		for (Pair<ItemDescription, Double> pair : msg.prices) {
			friendlyByteBuf.writeNbt(pair.getLeft().toNbt());
			friendlyByteBuf.writeDouble(pair.getRight());
		}
	}

	public static UpdateItemPricesPacket decode(FriendlyByteBuf friendlyByteBuf) {
		Type type = Type.valueOf(friendlyByteBuf.readUtf());
		List<Pair<ItemDescription, Double>> prices = new ArrayList<>();
		int size = friendlyByteBuf.readInt();
		for (int i = 0; i < size; i++) {
			prices.add(Pair.of(ItemDescription.fromNbt(friendlyByteBuf.readNbt()), friendlyByteBuf.readDouble()));
		}

		return new UpdateItemPricesPacket(type, prices);
	}

	public static void handle(UpdateItemPricesPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> UpdateItemPricesHandler.handle(msg, ctx));
		ctx.get().setPacketHandled(true);
	}

	public enum Type {
		EXPORTER,
		IMPORTER
	}
}
