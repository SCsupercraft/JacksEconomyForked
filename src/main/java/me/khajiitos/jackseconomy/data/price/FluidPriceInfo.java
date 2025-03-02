package me.khajiitos.jackseconomy.data.price;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public abstract class FluidPriceInfo {

    public static @NotNull FluidPriceInfo[] fromJson(JsonObject jsonObject) {
        try {
            ArrayList<FluidPriceInfo> list = new ArrayList<>();

            FluidPriceInfo prices = PricesFluidPriceInfo.fromJsonOrNull(jsonObject);

            if (prices != null) {
                list.add(prices);
            }

            return list.toArray(new FluidPriceInfo[0]);
        } catch (NullPointerException | ClassCastException e) {
            return new FluidPriceInfo[0];
        }
    }

    protected static boolean hasAny(JsonObject object, Iterable<String> strings) {
        for (String string : strings) {
            if (object.has(string)) {
                return true;
            }
        }

        return false;
    }

    public abstract JsonObject toJson();
}