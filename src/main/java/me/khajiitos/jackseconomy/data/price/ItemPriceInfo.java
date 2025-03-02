package me.khajiitos.jackseconomy.data.price;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public abstract class ItemPriceInfo {

    public static @NotNull ItemPriceInfo[] fromJson(JsonObject jsonObject) {
        try {
            ArrayList<ItemPriceInfo> list = new ArrayList<>();

            ItemPriceInfo admin = AdminShopItemPriceInfo.fromJsonOrNull(jsonObject);
            ItemPriceInfo prices = PricesItemPriceInfo.fromJsonOrNull(jsonObject);

            if (admin != null) {
                list.add(admin);
            }

            if (prices != null) {
                list.add(prices);
            }

            return list.toArray(new ItemPriceInfo[0]);
        } catch (NullPointerException | ClassCastException e) {
            return new ItemPriceInfo[0];
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