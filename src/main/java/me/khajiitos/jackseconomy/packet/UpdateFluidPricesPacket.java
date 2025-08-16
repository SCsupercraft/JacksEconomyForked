package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.data.price.FluidDescription;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public record UpdateFluidPricesPacket(PricesType pricesType, List<Pair<FluidDescription, Double>> prices) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateFluidPricesPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "update_fluid_prices"));

	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateFluidPricesPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8.map(
					PricesType::valueOf,
					PricesType::name
			),
			UpdateFluidPricesPacket::pricesType,
			StreamCodec.of(
					(buf, list) -> {
						buf.writeInt(list.size());
						for (Pair<FluidDescription, Double> pair : list) {
							buf.writeNbt(pair.getLeft().toNbt());
							buf.writeDouble(pair.getRight());
						}
					},
					buf -> {
						List<Pair<FluidDescription, Double>> list = new ArrayList<>();
						int size = buf.readInt();
						for (int i = 0; i < size; i++) {
							list.add(Pair.of(FluidDescription.fromNbt(buf.readNbt()), buf.readDouble()));
						}
						return list;
					}
			),
			UpdateFluidPricesPacket::prices,
			UpdateFluidPricesPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public enum PricesType {
		EXPORTER,
		IMPORTER
	}
}
