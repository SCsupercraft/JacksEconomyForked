package me.khajiitos.jackseconomy.data.price;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public abstract class FluidPriceInfo {

    public static @Nullable FluidPriceInfo fromJson(JsonObject jsonObject) {
        try {
            FluidPriceInfo info = null;

            if (jsonObject.get("type").getAsString().equals("prices")) {
                info = PricesFluidPriceInfo.fromJsonOrNull(jsonObject);
            }

            return info;
        } catch (NullPointerException e) {
            return null;
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