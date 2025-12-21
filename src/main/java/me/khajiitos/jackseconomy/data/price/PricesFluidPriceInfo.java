package me.khajiitos.jackseconomy.data.price;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PricesFluidPriceInfo extends FluidPriceInfo {
    public double sellPrice;
    public double importerBuyPrice;

    public PricesFluidPriceInfo(double sellPrice, double importerBuyPrice) {
        this.sellPrice = sellPrice;
        this.importerBuyPrice = importerBuyPrice;
    }

    protected static @Nullable PricesFluidPriceInfo fromNbtOrNull(CompoundTag compoundTag) {
        try {
            if (hasAny(compoundTag, List.of("sellPrice", "importerBuyPrice"))) {

                double sellPrice = compoundTag.contains("sellPrice") ? compoundTag.getDouble("sellPrice") : -1;
                double importerBuyPrice = compoundTag.contains("importerBuyPrice") ? compoundTag.getDouble("importerBuyPrice") : -1;

                return new PricesFluidPriceInfo(sellPrice, importerBuyPrice);
            }

        } catch (NullPointerException | ClassCastException ignored) {}
        return null;
    }

    public CompoundTag toNbt() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("type", "prices");

        if (this.sellPrice != -1) {
            compoundTag.putDouble("sellPrice", this.sellPrice);
        }

        if (this.importerBuyPrice != -1) {
            compoundTag.putDouble("importerBuyPrice", this.importerBuyPrice);
        }

        return compoundTag;
    }
}
