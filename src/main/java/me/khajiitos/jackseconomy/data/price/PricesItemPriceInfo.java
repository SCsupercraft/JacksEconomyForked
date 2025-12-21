package me.khajiitos.jackseconomy.data.price;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PricesItemPriceInfo extends ItemPriceInfo {
    public double sellPrice;
    public double adminShopSellPrice;
    public double importerBuyPrice;
    public String adminShopSellStage;
    public @Nullable String adminShopName;

    public PricesItemPriceInfo(double sellPrice, double adminShopSellPrice, double importerBuyPrice, String adminShopSellStage, @Nullable String adminShopName) {
        this.sellPrice = sellPrice;
        this.adminShopSellPrice = adminShopSellPrice;
        this.importerBuyPrice = importerBuyPrice;
        this.adminShopSellStage = adminShopSellStage;
        this.adminShopName = adminShopName;
    }

    protected static @Nullable ItemPriceInfo fromNbtOrNull(CompoundTag compoundTag) {
        try {
            if (hasAny(compoundTag, List.of("sellPrice", "adminShopSellPrice", "importerBuyPrice"))) {

                double sellPrice = compoundTag.contains("sellPrice") ? compoundTag.getDouble("sellPrice") : -1;
                double adminShopSellPrice = compoundTag.contains("adminShopSellPrice") ? compoundTag.getDouble("adminShopSellPrice") : -1;
                double importerBuyPrice = compoundTag.contains("importerBuyPrice") ? compoundTag.getDouble("importerBuyPrice") : -1;
                String adminShopSellStage = compoundTag.contains("adminShopSellStage") ? compoundTag.getString("adminShopSellStage") : null;
                String adminShopName = compoundTag.contains("adminShopName") ? compoundTag.getString("adminShopName") : null;
                if (adminShopName != null && adminShopName.length() > 32) return null;

                return new PricesItemPriceInfo(sellPrice, adminShopSellPrice, importerBuyPrice, adminShopSellStage, adminShopName);
            }

        } catch (NullPointerException | ClassCastException ignored) {}
        return null;
    }

    public CompoundTag toNbt() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("type", "prices");

        if (this.adminShopSellPrice != -1) {
            compoundTag.putDouble("adminShopSellPrice", this.adminShopSellPrice);
        }

        if (this.sellPrice != -1) {
            compoundTag.putDouble("sellPrice", this.sellPrice);
        }

        if (this.importerBuyPrice != -1) {
            compoundTag.putDouble("importerBuyPrice", this.importerBuyPrice);
        }

        if (this.adminShopSellStage != null) {
            compoundTag.putString("adminShopSellStage", this.adminShopSellStage);
        }

        if (this.adminShopName != null) {
            compoundTag.putString("adminShopName", this.adminShopName);
        }

        return compoundTag;
    }
}
