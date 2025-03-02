package me.khajiitos.jackseconomy.data.price;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AdminShopItemPriceInfo extends ItemPriceInfo {
    public double adminShopBuyPrice;
    public String category;
    public int adminShopSlot;
    public String customAdminShopName;
    public String adminShopStage;

    public AdminShopItemPriceInfo(double adminShopBuyPrice, String category, int adminShopSlot, String customAdminShopName, String adminShopStage) {
        this.adminShopBuyPrice = adminShopBuyPrice;
        this.category = category;
        this.adminShopSlot = adminShopSlot;
        this.customAdminShopName = customAdminShopName;
        this.adminShopStage = adminShopStage;
    }

    protected static @Nullable ItemPriceInfo fromJsonOrNull(JsonObject jsonObject) {
        try {
            if (hasAny(jsonObject, List.of("adminShopBuyPrice", "category", "adminShopSlot", "adminShopStage", "customAdminShopName"))) {
                double adminShopBuyPrice = jsonObject.has("adminShopBuyPrice") ? jsonObject.get("adminShopBuyPrice").getAsDouble() : -1;
                String category = jsonObject.has("category") ? jsonObject.get("category").getAsString() : null;
                int adminShopSlot = jsonObject.has("adminShopSlot") ? jsonObject.get("adminShopSlot").getAsInt() : -1;
                String customAdminShopName = jsonObject.has("customAdminShopName") ? jsonObject.get("customAdminShopName").getAsString() : null;
                String adminShopStage = jsonObject.has("adminShopStage") ? jsonObject.get("adminShopStage").getAsString() : null;

                return new AdminShopItemPriceInfo(adminShopBuyPrice, category, adminShopSlot, customAdminShopName, adminShopStage);
            }

        } catch (NullPointerException | ClassCastException ignored) {}
        return null;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "adminshop");

        if (this.adminShopBuyPrice != -1) {
            jsonObject.addProperty("adminShopBuyPrice", this.adminShopBuyPrice);
        }

        if (this.category != null) {
            jsonObject.addProperty("category", this.category);
        }

        if (this.adminShopSlot != -1) {
            jsonObject.addProperty("adminShopSlot", this.adminShopSlot);
        }

        if (this.customAdminShopName != null) {
            jsonObject.addProperty("customAdminShopName", this.customAdminShopName);
        }

        if (this.adminShopStage != null) {
            jsonObject.addProperty("adminShopStage", this.adminShopStage);
        }

        return jsonObject;
    }
}
