package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.JacksEconomyClient;
import me.khajiitos.jackseconomy.data.price.FluidDescription;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import me.khajiitos.jackseconomy.data.price.PricesFluidPriceInfo;
import me.khajiitos.jackseconomy.data.price.PricesItemPriceInfo;
import me.khajiitos.jackseconomy.packet.PricesInfoPacket;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PricesInfoHandler {

    public static void handle(PricesInfoPacket msg, final IPayloadContext context) {
        JacksEconomyClient.priceInfos.clear();
        JacksEconomyClient.fluidPriceInfos.clear();

        msg.data().forEach(tag -> {
            if (tag instanceof CompoundTag itemTag) {
                ItemDescription itemDescription = ItemDescription.fromNbt(itemTag);

                if (itemDescription != null) {
                    double sellPrice = itemTag.getDouble("sellPrice");
                    double adminShopSellPrice = itemTag.getDouble("adminShopSellPrice");
                    double importerBuyPrice = itemTag.getDouble("importerBuyPrice");
                    String adminShopSellStage = itemTag.contains("adminShopSellStage") ? itemTag.getString("adminShopSellStage") : null;
                    String adminShopName = itemTag.contains("adminShopName") ? itemTag.getString("adminShopName") : null;

                    JacksEconomyClient.priceInfos.put(itemDescription, new PricesItemPriceInfo(sellPrice, adminShopSellPrice, importerBuyPrice, adminShopSellStage, adminShopName));
                }
            }
        });
        msg.secondaryData().forEach(tag -> {
            if (tag instanceof CompoundTag fluidTag) {
                FluidDescription fluidDescription = FluidDescription.fromNbt(fluidTag);

                if (fluidDescription != null) {
                    double sellPrice = fluidTag.getDouble("sellPrice");
                    double importerBuyPrice = fluidTag.getDouble("importerBuyPrice");

                    JacksEconomyClient.fluidPriceInfos.put(fluidDescription, new PricesFluidPriceInfo(sellPrice, importerBuyPrice));
                }
            }
        });
    }
}
