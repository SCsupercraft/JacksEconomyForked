package me.khajiitos.jackseconomy.packet;

import io.netty.handler.codec.DecoderException;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.packet.handler.UpdateAdminShopHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record UpdateAdminShopPacket(CompoundTag data, @Nullable String adminShopName) {
    public static void encode(UpdateAdminShopPacket msg, FriendlyByteBuf friendlyByteBuf) {
        // Debug code used for testing earlier
        /*
        int slot = 0;

        ListTag items = msg.data.getList("items", ListTag.TAG_COMPOUND);
        ListTag categories = msg.data.getList("categories", ListTag.TAG_COMPOUND);

        CompoundTag categoryTag = new CompoundTag();
        categoryTag.putString("name", "notcrashingtoday");
        categoryTag.putString("item", "minecraft:dirt");
        ListTag innerCategories = new ListTag();
        CompoundTag innerCategoryTag = new CompoundTag();
        innerCategoryTag.putString("name", "notcrashingtoday");
        innerCategoryTag.putString("item", "minecraft:dirt");
        innerCategories.add(innerCategoryTag);
        categoryTag.put("categories", innerCategories);
        categories.add(categoryTag);

        RandomSource randomSource = RandomSource.create();
        for (Item item : ForgeRegistries.ITEMS) {
            ItemStack itemStack = EnchantmentHelper.enchantItem(randomSource, new ItemStack(item), 30, true);
            CompoundTag tag = ItemDescription.ofItem(itemStack).toNbt();
            tag.putString("category", "notcrashingtoday:notcrashingtoday");
            tag.putInt("slot", slot);
            tag.putDouble("adminShopBuyPrice", Math.random() * 100000);
            items.add(tag);
            slot++;
        }*/

        friendlyByteBuf.writeNbt(msg.data);
        friendlyByteBuf.writeBoolean(msg.adminShopName != null);
        if (msg.adminShopName != null) friendlyByteBuf.writeUtf(msg.adminShopName, 32);
    }

    public static UpdateAdminShopPacket decode(FriendlyByteBuf friendlyByteBuf) {
        CompoundTag data = friendlyByteBuf.readAnySizeNbt();
        String name = null;
        try {
            name = friendlyByteBuf.readBoolean() ? friendlyByteBuf.readUtf(32) : null;
        } catch (DecoderException e) {
            JacksEconomy.LOGGER.warn("Error decoding update admin shop packet", e);
        }
        return new UpdateAdminShopPacket(data, name);
    }

    public static void handle(UpdateAdminShopPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> UpdateAdminShopHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
