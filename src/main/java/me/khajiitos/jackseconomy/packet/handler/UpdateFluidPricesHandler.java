package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.data.price.FluidDescription;
import me.khajiitos.jackseconomy.data.price.PriceManager;
import me.khajiitos.jackseconomy.data.price.PricesFluidPriceInfo;
import me.khajiitos.jackseconomy.packet.UpdateFluidPricesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.lang3.tuple.Pair;

public class UpdateFluidPricesHandler {
	public static void handle(UpdateFluidPricesPacket msg, final IPayloadContext context) {
		if (!(context.player() instanceof ServerPlayer player && player.hasPermissions(4))) return;

		if (msg.pricesType() == UpdateFluidPricesPacket.PricesType.EXPORTER) {
			for (Pair<FluidDescription, Double> price: msg.prices()) {
				PricesFluidPriceInfo existingInfo = PriceManager.getPricesInfo(price.getLeft());

				if (existingInfo != null) {
					existingInfo.sellPrice = price.getRight();
				} else if (price.getRight() != -1) {
					PriceManager.addPriceInfo(price.getLeft(), new PricesFluidPriceInfo(price.getRight(), -1));
				}
			}
		} else {
			for (Pair<FluidDescription, Double> price: msg.prices()) {
				PricesFluidPriceInfo existingInfo = PriceManager.getPricesInfo(price.getLeft());

				if (existingInfo != null) {
					existingInfo.importerBuyPrice = price.getRight();
				} else if (price.getRight() != -1) {
					PriceManager.addPriceInfo(price.getLeft(), new PricesFluidPriceInfo(-1, price.getRight()));
				}
			}
		}

		PriceManager.save();
		PriceManager.sendDataToPlayers(false);
	}
}
