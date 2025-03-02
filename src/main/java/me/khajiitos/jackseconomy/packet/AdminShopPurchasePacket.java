package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.packet.handler.AdminShopPurchaseHandler;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public record AdminShopPurchasePacket(Map<ShopItemDescription, Integer> shoppingCart, Map<ItemDescription, Integer> itemsToSell) {

    public static void encode(AdminShopPurchasePacket msg, FriendlyByteBuf friendlyByteBuf) {
        CompoundTag compoundTag = new CompoundTag();
        ListTag shoppingCartData = new ListTag();
        ListTag sellData = new ListTag();

        msg.shoppingCart().forEach((shopItem, amount) -> {
            CompoundTag itemTag = shopItem.itemDescription().toNbt();
            itemTag.putInt("amount", amount);
            itemTag.putInt("slot", shopItem.slot());
            itemTag.putString("category", shopItem.category());
            shoppingCartData.add(itemTag);
        });

        msg.itemsToSell().forEach((itemDescription, integer) -> {
            CompoundTag itemTag = itemDescription.toNbt();
            itemTag.putInt("amount", integer);
            sellData.add(itemTag);
        });

        compoundTag.put("shoppingCart", shoppingCartData);
        compoundTag.put("sellData", sellData);

        friendlyByteBuf.writeNbt(compoundTag);
    }

    public static AdminShopPurchasePacket decode(FriendlyByteBuf friendlyByteBuf) {
        CompoundTag compoundTag = friendlyByteBuf.readAnySizeNbt();

        Map<ShopItemDescription, Integer> shoppingCartMap = new HashMap<>();
        Map<ItemDescription, Integer> itemsToSellMap = new HashMap<>();

        if (compoundTag != null) {
            ListTag shoppingCartData = compoundTag.getList("shoppingCart", Tag.TAG_COMPOUND);
            ListTag itemsToSellData = compoundTag.getList("sellData", Tag.TAG_COMPOUND);

            shoppingCartData.forEach(tag -> {
                if (tag instanceof CompoundTag itemTag) {
                    ItemDescription itemDescription = ItemDescription.fromNbt(itemTag);

                    if (itemDescription != null) {
                        int amount = itemTag.getInt("amount");
                        String category = itemTag.getString("category");
                        int slot = itemTag.getInt("slot");

                        if (amount > 0) {
                            shoppingCartMap.put(new ShopItemDescription(itemDescription, slot, category), amount);
                        }
                    }
                }
            });

            itemsToSellData.forEach(tag -> {
                if (tag instanceof CompoundTag itemTag) {
                    ItemDescription itemDescription = ItemDescription.fromNbt(itemTag);

                    if (itemDescription != null) {
                        int amount = itemTag.getInt("amount");

                        if (amount > 0) {
                            itemsToSellMap.put(itemDescription, amount);
                        }
                    }
                }
            });
        }

        return new AdminShopPurchasePacket(shoppingCartMap, itemsToSellMap);
    }

    public static void handle(AdminShopPurchasePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> AdminShopPurchaseHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }

    public record ShopItemDescription(ItemDescription itemDescription, int slot, String category) { }
}
