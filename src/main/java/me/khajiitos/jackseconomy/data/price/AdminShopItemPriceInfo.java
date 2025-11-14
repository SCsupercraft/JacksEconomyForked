package me.khajiitos.jackseconomy.data.price;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AdminShopItemPriceInfo extends ItemPriceInfo {
    public double adminShopBuyPrice;
    public String category;
    public int adminShopSlot;
    public String customAdminShopName;
    public String adminShopStage;
    public @Nullable String adminShopName;

    public AdminShopItemPriceInfo(double adminShopBuyPrice, String category, int adminShopSlot, String customAdminShopName, String adminShopStage, @Nullable String adminShopName) {
        this.adminShopBuyPrice = adminShopBuyPrice;
        this.category = category;
        this.adminShopSlot = adminShopSlot;
        this.customAdminShopName = customAdminShopName;
        this.adminShopStage = adminShopStage;
        this.adminShopName = adminShopName;
    }

    protected static @Nullable ItemPriceInfo fromNbtOrNull(CompoundTag compoundTag) {
        try {
            if (hasAny(compoundTag, List.of("adminShopBuyPrice", "category", "adminShopSlot", "adminShopStage", "customAdminShopName"))) {
                double adminShopBuyPrice = compoundTag.contains("adminShopBuyPrice") ? compoundTag.getDouble("adminShopBuyPrice") : -1;
                String category = compoundTag.contains("category") ? compoundTag.getString("category") : null;
                int adminShopSlot = compoundTag.contains("adminShopSlot") ? compoundTag.getInt("adminShopSlot") : -1;
                String customAdminShopName = compoundTag.contains("customAdminShopName") ? compoundTag.getString("customAdminShopName") : null;
                String adminShopStage = compoundTag.contains("adminShopStage") ? compoundTag.getString("adminShopStage") : null;
                String adminShopName = compoundTag.contains("adminShopName") ? compoundTag.getString("adminShopName") : null;
                if (adminShopName != null && adminShopName.length() > 32) return null;

                return new AdminShopItemPriceInfo(adminShopBuyPrice, category, adminShopSlot, customAdminShopName, adminShopStage, adminShopName);
            }

        } catch (NullPointerException | ClassCastException ignored) {}
        return null;
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("type", "adminshop");

        if (this.adminShopBuyPrice != -1) {
            compoundTag.putDouble("adminShopBuyPrice", this.adminShopBuyPrice);
        }

        if (this.category != null) {
            compoundTag.putString("category", this.category);
        }

        if (this.adminShopSlot != -1) {
            compoundTag.putInt("adminShopSlot", this.adminShopSlot);
        }

        if (this.customAdminShopName != null) {
            compoundTag.putString("customAdminShopName", this.customAdminShopName);
        }

        if (this.adminShopStage != null) {
            compoundTag.putString("adminShopStage", this.adminShopStage);
        }

        if (this.adminShopName != null) {
            compoundTag.putString("adminShopName", this.adminShopName);
        }

        return compoundTag;
    }
}
