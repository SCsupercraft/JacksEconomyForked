package me.khajiitos.jackseconomy.data.price;

import com.mojang.serialization.Codec;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.util.ComponentUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;

public record ItemDescription(Holder<Item> item, @NotNull DataComponentPatch components) {
    public static final Codec<ItemDescription> CODEC = ItemStack.CODEC.xmap(ItemDescription::ofItem, ItemDescription::createItemStack);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemDescription> STREAM_CODEC = ItemStack.STREAM_CODEC.map(ItemDescription::ofItem, ItemDescription::createItemStack);

    public ItemDescription(Holder<Item> item, DataComponentPatch components) {
        if (item == null) throw new NullPointerException();
        this.item = item;

        if (components == null || components.isEmpty()) {
            DataComponentPatch.Builder builder = DataComponentPatch.builder();
            if (item.value().isDamageable(new ItemStack(item, 1, builder.build()))) {
                builder.set(DataComponents.DAMAGE, 0);
            }
            this.components = builder.build();
        } else {
            this.components = ComponentUtil.clone(components);
        }
    }

    public static ItemDescription ofItem(ItemStack itemStack) {
        return new ItemDescription(itemStack.getItemHolder(), itemStack.getComponentsPatch());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemDescription that = (ItemDescription) o;
        return Objects.equals(item, that.item) && Objects.equals(components, that.components);
    }

    public CompoundTag toNbt() {
        return (CompoundTag) createItemStack().save(Objects.requireNonNull(JacksEconomy.registryAccess()));
    }

    public static @Nullable ItemDescription fromNbt(Tag tag) {
        ItemStack stack = ItemStack.parse(Objects.requireNonNull(JacksEconomy.registryAccess()), tag).orElse(null);
        return stack != null ? ofItem(stack) : null;
    }

    public ItemStack createItemStack() {
        return new ItemStack(this.item, 1, this.components);
    }
}