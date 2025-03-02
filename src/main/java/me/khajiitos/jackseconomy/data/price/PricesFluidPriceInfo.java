package me.khajiitos.jackseconomy.data.price;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PricesFluidPriceInfo extends FluidPriceInfo {
    public double sellPrice;
    public double importerBuyPrice;

    public PricesFluidPriceInfo(double sellPrice, double importerBuyPrice) {
        this.sellPrice = sellPrice;
        this.importerBuyPrice = importerBuyPrice;
    }

    protected static @Nullable PricesFluidPriceInfo fromJsonOrNull(JsonObject jsonObject) {
        try {
            if (hasAny(jsonObject, List.of("sellPrice", "importerBuyPrice"))) {

                double sellPrice = jsonObject.has("sellPrice") ? jsonObject.get("sellPrice").getAsDouble() : -1;
                double importerBuyPrice = jsonObject.has("importerBuyPrice") ? jsonObject.get("importerBuyPrice").getAsDouble() : -1;

                return new PricesFluidPriceInfo(sellPrice, importerBuyPrice);
            }

        } catch (NullPointerException | ClassCastException ignored) {}
        return null;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "prices");

        if (this.sellPrice != -1) {
            jsonObject.addProperty("sellPrice", this.sellPrice);
        }

        if (this.importerBuyPrice != -1) {
            jsonObject.addProperty("importerBuyPrice", this.importerBuyPrice);
        }

        return jsonObject;
    }
}
