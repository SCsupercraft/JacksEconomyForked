package me.khajiitos.jackseconomy.data.price;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public abstract class ItemPriceInfo {

    public static @Nullable ItemPriceInfo fromNbt(CompoundTag compoundTag) {
        try {
            ItemPriceInfo info = null;

            switch (compoundTag.getString("type")) {
                case "adminshop" -> info = AdminShopItemPriceInfo.fromNbtOrNull(compoundTag);
                case "prices" -> info = PricesItemPriceInfo.fromNbtOrNull(compoundTag);
            }

            return info;
        } catch (NullPointerException e) {
            return null;
        }
    }

    protected static boolean hasAny(CompoundTag compoundTag, Iterable<String> strings) {
        for (String string : strings) {
            if (compoundTag.contains(string)) {
                return true;
            }
        }

        return false;
    }

    public abstract CompoundTag toNbt();
}