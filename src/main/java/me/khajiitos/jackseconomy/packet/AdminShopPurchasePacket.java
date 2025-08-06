package me.khajiitos.jackseconomy.packet;

import io.netty.buffer.ByteBuf;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record AdminShopPurchasePacket(Map<ShopItemDescription, Integer> shoppingCart, Map<ItemDescription, Integer> itemsToSell, Optional<String> adminShopName) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AdminShopPurchasePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "admin_shop_purchase"));

    public static final StreamCodec<ByteBuf, AdminShopPurchasePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            AdminShopPurchasePacket::encode,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            AdminShopPurchasePacket::adminShopName,
            AdminShopPurchasePacket::decode
    );

    public static CompoundTag encode(AdminShopPurchasePacket msg) {
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

        return compoundTag;
    }

    public static AdminShopPurchasePacket decode(CompoundTag compoundTag, Optional<String> adminShopName) {
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

        return new AdminShopPurchasePacket(shoppingCartMap, itemsToSellMap, adminShopName);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record ShopItemDescription(ItemDescription itemDescription, int slot, String category) { }
}
