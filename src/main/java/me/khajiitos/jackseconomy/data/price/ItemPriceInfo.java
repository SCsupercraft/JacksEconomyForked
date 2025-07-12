package me.khajiitos.jackseconomy.data.price;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public abstract class ItemPriceInfo {

    public static @Nullable ItemPriceInfo fromJson(JsonObject jsonObject) {
        try {
            ItemPriceInfo info = null;

            switch (jsonObject.get("type").getAsString()) {
                case "adminshop" -> info = AdminShopItemPriceInfo.fromJsonOrNull(jsonObject);
                case "prices" -> info = PricesItemPriceInfo.fromJsonOrNull(jsonObject);
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