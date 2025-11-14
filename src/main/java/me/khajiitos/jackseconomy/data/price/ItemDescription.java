package me.khajiitos.jackseconomy.data.price;

import com.mojang.serialization.Codec;
import me.khajiitos.jackseconomy.util.ItemHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Objects;

public record ItemDescription(Item item, CompoundTag compoundTag) {
    public static final Codec<ItemDescription> CODEC = ItemStack.CODEC.xmap(ItemDescription::ofItem, ItemDescription::createItemStack);

    public ItemDescription(Item item, @Nullable CompoundTag compoundTag) {
        if (item == null) throw new NullPointerException();
        this.item = item;

        if (compoundTag == null) {
            CompoundTag tag = new CompoundTag();
            if (item.canBeDepleted()) {
                tag.putInt("Damage", 0);
            }
            this.compoundTag = tag;
        } else {
            this.compoundTag = compoundTag.copy();
        }
    }

    public static ItemDescription ofItem(ItemStack itemStack) {
        return new ItemDescription(itemStack.getItem(), itemStack.getTag());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemDescription that = (ItemDescription) o;
        return Objects.equals(item, that.item) && Objects.equals(compoundTag, that.compoundTag);
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        String itemName = ItemHelper.getItemName(this.item);

        tag.putString("item", itemName != null ? itemName : "");

        if (!this.compoundTag.isEmpty()) {
            tag.put("nbt", this.compoundTag.copy());
        }

        return tag;
    }

    public static @Nullable ItemDescription fromNbt(CompoundTag compoundTag) {
        String itemName = compoundTag.getString("item");

        if (itemName.isEmpty()) {
            return null;
        }

        Item item = ItemHelper.getItem(itemName);

        if (item == null) {
            return null;
        }

        CompoundTag tag = compoundTag.getCompound("nbt");

        return new ItemDescription(item, tag);
    }

    public ItemStack createItemStack() {
        ItemStack itemStack = new ItemStack(this.item);

        CompoundTag tag = this.compoundTag();

        if (!tag.isEmpty()) {
            itemStack.setTag(tag.copy());
        }

        return itemStack;
    }
}