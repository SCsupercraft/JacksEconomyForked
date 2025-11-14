package me.khajiitos.jackseconomy.data.price;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public abstract class FluidPriceInfo {

    public static @Nullable FluidPriceInfo fromNbt(CompoundTag compoundTag) {
        try {
            FluidPriceInfo info = null;

            if (compoundTag.getString("type").equals("prices")) {
                info = PricesFluidPriceInfo.fromNbtOrNull(compoundTag);
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