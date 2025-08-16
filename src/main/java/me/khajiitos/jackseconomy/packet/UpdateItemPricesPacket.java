package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public record UpdateItemPricesPacket(PricesType pricesType, List<Pair<ItemDescription, Double>> prices) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateItemPricesPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "update_item_prices"));

	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateItemPricesPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8.map(
					UpdateItemPricesPacket.PricesType::valueOf,
					UpdateItemPricesPacket.PricesType::name
			),
			UpdateItemPricesPacket::pricesType,
			StreamCodec.of(
					(buf, list) -> {
						buf.writeInt(list.size());
						for (Pair<ItemDescription, Double> pair : list) {
							buf.writeNbt(pair.getLeft().toNbt());
							buf.writeDouble(pair.getRight());
						}
					},
					buf -> {
						List<Pair<ItemDescription, Double>> list = new ArrayList<>();
						int size = buf.readInt();
						for (int i = 0; i < size; i++) {
							list.add(Pair.of(ItemDescription.fromNbt(buf.readNbt()), buf.readDouble()));
						}
						return list;
					}
			),
			UpdateItemPricesPacket::prices,
			UpdateItemPricesPacket::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public enum PricesType {
		EXPORTER,
		IMPORTER
	}
}
