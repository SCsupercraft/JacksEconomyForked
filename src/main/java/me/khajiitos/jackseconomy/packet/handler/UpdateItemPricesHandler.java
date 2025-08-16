package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.data.price.ItemDescription;
import me.khajiitos.jackseconomy.data.price.PriceManager;
import me.khajiitos.jackseconomy.data.price.PricesItemPriceInfo;
import me.khajiitos.jackseconomy.packet.UpdateItemPricesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Supplier;

public class UpdateItemPricesHandler {
	public static void handle(UpdateItemPricesPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		if (player == null || !player.hasPermissions(4)) return;

		if (msg.type() == UpdateItemPricesPacket.Type.EXPORTER) {
			for (Pair<ItemDescription, Double> price: msg.prices()) {
				PricesItemPriceInfo existingInfo = PriceManager.getPricesInfo(price.getLeft());

				if (existingInfo != null) {
					existingInfo.sellPrice = price.getRight();
				} else if (price.getRight() != -1) {
					PriceManager.addPriceInfo(price.getLeft(), new PricesItemPriceInfo(price.getRight(), -1, -1, null, null));
				}
			}
		} else {
			for (Pair<ItemDescription, Double> price: msg.prices()) {
				PricesItemPriceInfo existingInfo = PriceManager.getPricesInfo(price.getLeft());

				if (existingInfo != null) {
					existingInfo.importerBuyPrice = price.getRight();
				} else if (price.getRight() != -1) {
					PriceManager.addPriceInfo(price.getLeft(), new PricesItemPriceInfo(-1, -1, price.getRight(), null, null));
				}
			}
		}

		PriceManager.save();
		PriceManager.sendDataToPlayers(false);
	}
}
