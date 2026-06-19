package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.JacksEconomyClient;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import me.khajiitos.jackseconomy.packet.AdminShopSchemaPacket;
import me.khajiitos.jackseconomy.screen.AdminShopScreen;
import me.khajiitos.jackseconomy.screen.BulkAdminShopScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class AdminShopSchemaHandler {
    public static void handle(AdminShopSchemaPacket msg, Supplier<NetworkEvent.Context> ctx) {
        onShopData(msg.data(), msg.adminShopName(), msg.oneItemCurrencyMode());

        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof AdminShopScreen adminShopScreen) {
            adminShopScreen.onShopData(msg.data(), msg.adminShopName());
        } else if (screen instanceof BulkAdminShopScreen bulkAdminShopScreen) {
            bulkAdminShopScreen.onShopData(msg.data(), msg.adminShopName());
        }
    }

    public static void onShopData(CompoundTag data, @Nullable String adminShopName, boolean oneItemCurrencyMode) {
        JacksEconomyClient.AdminShopData adminShopData = JacksEconomyClient.getOrComputeAdminShopData(adminShopName);

        adminShopData.shopItems().clear();
        adminShopData.sellPrices().clear();

        ListTag categoriesTag = data.getList("categories", Tag.TAG_COMPOUND);

        categoriesTag.forEach(tag -> {
            if (tag instanceof CompoundTag compoundTag) {
                String categoryName = compoundTag.getString("name");
                ItemDescription itemDescription = ItemDescription.fromNbt(compoundTag.getCompound("item"));

                if (itemDescription == null) {
                    return;
                }

                AdminShopScreen.Category category = new AdminShopScreen.Category(categoryName, itemDescription);
                LinkedHashMap<AdminShopScreen.InnerCategory, List<AdminShopScreen.ShopItem>> innerCategories = new LinkedHashMap<>();
                adminShopData.shopItems().put(category, innerCategories);
                ListTag categoriesInnerTag = compoundTag.getList("categories", Tag.TAG_COMPOUND);

                categoriesInnerTag.forEach(innerTag -> {
                    if (innerTag instanceof CompoundTag innerCompoundTag) {
                        String innerCategoryName = innerCompoundTag.getString("name");
                        ItemDescription innerItemDescription = ItemDescription.fromNbt(innerCompoundTag.getCompound("item"));

                        if (innerItemDescription == null) {
                            return;
                        }

                        AdminShopScreen.InnerCategory innerCategory = new AdminShopScreen.InnerCategory(innerCategoryName, innerItemDescription);
                        innerCategories.put(innerCategory, new ArrayList<>());
                    }
                });
            }
        });

        LinkedHashMap<String, List<AdminShopScreen.UnpreparedShopItem>> slotlessShopItems = new LinkedHashMap<>();

        ListTag itemsTag = data.getList("items", Tag.TAG_COMPOUND);

        itemsTag.forEach(tag -> {
            if (tag instanceof CompoundTag compoundTag) {
                ItemDescription itemDescription = ItemDescription.fromNbt(compoundTag);

                if (itemDescription == null) {
                    return;
                }

                if (compoundTag.contains("adminShopSellPrice")) {
                    double sellPrice = compoundTag.getDouble("adminShopSellPrice");
                    String sellStage = compoundTag.contains("adminShopSellStage") ? compoundTag.getString("adminShopSellStage") : null;
                    adminShopData.sellPrices().put(itemDescription, new AdminShopScreen.ItemSellabilityInfo(oneItemCurrencyMode ? Math.round(sellPrice) : sellPrice, sellStage));
                    return;
                }

                double buyPrice = compoundTag.getDouble("adminShopBuyPrice");
                int buyCount = compoundTag.getInt("adminShopBuyCount");
                int slot = compoundTag.contains("slot") ? compoundTag.getInt("slot") : -1;
                String customName = compoundTag.contains("customAdminShopName") ? compoundTag.getString("customAdminShopName") : null;
                String stage = compoundTag.contains("adminShopStage") ? compoundTag.getString("adminShopStage") : null;
                String category = compoundTag.getString("category");

                double price = oneItemCurrencyMode ? Math.round(buyPrice) : buyPrice;
                if (slot < 0) {
                    slotlessShopItems.computeIfAbsent(category, categoryName -> new ArrayList<>()).add(new AdminShopScreen.UnpreparedShopItem(itemDescription, price, buyCount, customName, stage));
                } else {
                    String[] categoryNamesInner = category.split(":", 2);
                    if (categoryNamesInner.length < 2) {
                        return;
                    }

                    String bigCategoryName = categoryNamesInner[0];
                    String innerCategoryName = categoryNamesInner[1];

                    for (Map.Entry<AdminShopScreen.Category, LinkedHashMap<AdminShopScreen.InnerCategory, List<AdminShopScreen.ShopItem>>> categoryEntry : adminShopData.shopItems().entrySet()) {
                        if (categoryEntry.getKey().getName().equals(bigCategoryName)) {
                            for (Map.Entry<AdminShopScreen.InnerCategory, List<AdminShopScreen.ShopItem>> entry : adminShopData.shopItems().get(categoryEntry.getKey()).entrySet()) {
                                if (entry.getKey().getName().equals(innerCategoryName)) {
                                    entry.getValue().add(new AdminShopScreen.ShopItem(itemDescription, price, buyCount, slot, customName, stage));
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }

            }
        });

        slotlessShopItems.forEach((categoryName, list) -> {
            String[] categoriesNames = categoryName.split(":", 2);

            if (categoriesNames.length < 2) {
                return;
            }

            String bigCategoryName = categoriesNames[0];
            String innerCategoryName = categoriesNames[1];

            for (Map.Entry<AdminShopScreen.Category, LinkedHashMap<AdminShopScreen.InnerCategory, List<AdminShopScreen.ShopItem>>> entry : adminShopData.shopItems().entrySet()) {
                if (entry.getKey().getName().equals(bigCategoryName)) {
                    for (AdminShopScreen.UnpreparedShopItem item : list) {
                        for (Map.Entry<AdminShopScreen.InnerCategory, List<AdminShopScreen.ShopItem>> entry1 : entry.getValue().entrySet()) {
                            if (entry1.getKey().getName().equals(innerCategoryName)) {
                                adminShopData.shopItems().get(entry.getKey()).get(entry1.getKey()).add(new AdminShopScreen.ShopItem(item.itemDescription(), item.price(), item.count(), findFirstAvailableSlot(adminShopData, entry.getKey(), entry1.getKey()), item.customName(), item.stage()));
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        });

        JacksEconomyClient.removeEmptyAdminShopData();
    }

    protected static int findFirstAvailableSlot(JacksEconomyClient.AdminShopData data, AdminShopScreen.Category category, AdminShopScreen.InnerCategory innerCategory) {
        for (int slot = 0;;slot++) {
            AdminShopScreen.ShopItem existingShopItem = getItemAtSlot(data, slot, category, innerCategory);

            if (existingShopItem == null) {
                return slot;
            }
        }
    }

    protected static @Nullable AdminShopScreen.ShopItem getItemAtSlot(JacksEconomyClient.AdminShopData data, int slot, AdminShopScreen.Category category, AdminShopScreen.InnerCategory innerCategory) {
        for (AdminShopScreen.ShopItem shopItem : data.shopItems().get(category).getOrDefault(innerCategory, List.of())) {
            if (shopItem.slot() == slot) {
                return shopItem;
            }
        }

        return null;
    }
}
