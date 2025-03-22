package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.data.price.AdminShopItemPriceInfo;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import me.khajiitos.jackseconomy.data.price.PriceManager;
import me.khajiitos.jackseconomy.data.price.PricesItemPriceInfo;
import me.khajiitos.jackseconomy.packet.UpdateAdminShopPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class UpdateAdminShopHandler {
    public static void handle(UpdateAdminShopPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();

        if (sender == null) {
            return;
        }

        if (!sender.hasPermissions(4) || !sender.isCreative()) {
            return;
        }

        CompoundTag data = msg.data();

        ListTag categoriesTag = data.getList("categories", Tag.TAG_COMPOUND);
        ListTag itemsTag = data.getList("items", Tag.TAG_COMPOUND);

        // <AdminShopItemPriceInfo> itemPriceInfos = PriceManager.getItemPriceInfos().stream().filter(itemPriceEntry -> itemPriceEntry.itemPriceInfo() instanceof AdminShopItemPriceInfo).map(itemPriceEntry -> (AdminShopItemPriceInfo)itemPriceEntry.itemPriceInfo()).toList();
        LinkedHashMap<PriceManager.Category, List<PriceManager.Category>> categories = PriceManager.getCategories();
        List<PriceManager.Category> toRemove = new ArrayList<>();

        categories.forEach((category, unused) -> {
            if (Objects.equals(category.adminShopName(), msg.adminShopName())) toRemove.add(category);
        });
        toRemove.forEach(categories::remove);

        /*
        // Remove all properties related to the shop
        // if the item wasn't removed they will be restored
        for (AdminShopItemPriceInfo entry : itemPriceInfos) {
            entry.adminShopBuyPrice = -1;
            entry.category = null;
            entry.customAdminShopName = null;
            entry.adminShopStage = null;
        }*/
        // Whatever, let's just remove them all... what's the worst that could happen?
        PriceManager.getItemPriceInfos().removeIf(itemPriceEntry -> itemPriceEntry.itemPriceInfo() instanceof AdminShopItemPriceInfo info && Objects.equals(info.adminShopName, msg.adminShopName()));

        categoriesTag.forEach(tag -> {
            if (tag instanceof CompoundTag compoundTag) {
                String name = compoundTag.getString("name");
                ItemDescription itemDescription = ItemDescription.fromNbt(compoundTag.getCompound("item"));

                PriceManager.Category category = new PriceManager.Category(name, itemDescription, msg.adminShopName());
                ArrayList<PriceManager.Category> innerCategories = new ArrayList<>();

                ListTag innerCategoriesTag = compoundTag.getList("categories", Tag.TAG_COMPOUND);

                innerCategoriesTag.forEach(tag1 -> {
                    if (tag1 instanceof CompoundTag innerCategoryTag) {
                        String innerName = innerCategoryTag.getString("name");
                        ItemDescription innerItemDescription = ItemDescription.fromNbt(innerCategoryTag.getCompound("item"));

                        PriceManager.Category innerCategory = new PriceManager.Category(innerName, innerItemDescription, null);
                        innerCategories.add(innerCategory);
                    }
                });

                categories.put(category, innerCategories);
            }
        });

        PriceManager.getItemPriceInfos().forEach(itemPriceEntry -> {
            if (itemPriceEntry.itemPriceInfo() instanceof PricesItemPriceInfo priceInfo && Objects.equals(priceInfo.adminShopName, msg.adminShopName())) {
                // Will be brought back later if not removed
                priceInfo.adminShopSellPrice = -1.0;
                priceInfo.adminShopSellStage = null;
            }
        });

        itemsTag.forEach(tag -> {
            if (tag instanceof CompoundTag compoundTag) {
                ItemDescription itemDescription = ItemDescription.fromNbt(compoundTag);

                if (itemDescription == null) {
                    return;
                }

                double sellPrice = compoundTag.contains("adminShopSellPrice") ?  compoundTag.getDouble("adminShopSellPrice") : -1.0;
                String sellStage = compoundTag.contains("adminShopSellStage") ? compoundTag.getString("adminShopSellStage") : null;

                if (sellPrice > 0) {
                    PricesItemPriceInfo pricesItemPriceInfo = PriceManager.getPricesInfo(itemDescription, msg.adminShopName());

                    if (pricesItemPriceInfo != null) {
                        pricesItemPriceInfo.adminShopSellPrice = sellPrice;
                        pricesItemPriceInfo.adminShopSellStage = sellStage;
                    } else {
                        PriceManager.addPriceInfo(itemDescription, new PricesItemPriceInfo(-1, sellPrice, -1, sellStage, msg.adminShopName()));
                    }

                    // Sell price/stage entries and admin shop entries are separate
                    return;
                }

                String category = compoundTag.getString("category");
                double buyPrice = compoundTag.getDouble("adminShopBuyPrice");
                int slot = compoundTag.contains("slot") ? compoundTag.getInt("slot") : -1;
                String customName = compoundTag.contains("customAdminShopName") ? compoundTag.getString("customAdminShopName") : null;
                String stage = compoundTag.contains("adminShopStage") ? compoundTag.getString("adminShopStage") : null;

                PriceManager.addPriceInfo(itemDescription, new AdminShopItemPriceInfo(buyPrice, category, slot, customName, stage, msg.adminShopName()));
            }
        });

        PriceManager.save();
        PriceManager.sendDataToPlayers();

        sender.sendSystemMessage(Component.translatable("jackseconomy.admin_shop_saved").withStyle(ChatFormatting.GREEN));
    }
}
