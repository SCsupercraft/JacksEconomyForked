package me.khajiitos.jackseconomy.util;

import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class ItemHelper {
    public static void dropItem(ItemStack itemStack, Level pLevel, BlockPos pPos) {
        ItemEntity dropItem = new ItemEntity(pLevel, pPos.getX() + 0.5, pPos.getY() + 1.0, pPos.getZ() + 0.5, itemStack);
        pLevel.addFreshEntity(dropItem);
    }

    public static @Nullable Item getItem(String name) {
        try {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(name);
            Item item = JacksEconomy.server.registryAccess().registryOrThrow(Registries.ITEM).get(resourceLocation);

            if (item == null) {
                JacksEconomy.LOGGER.info("Invalid item: " + name);
            }

            return item;
        } catch (ResourceLocationException e) {
            JacksEconomy.LOGGER.warn("Invalid resource location: " + name);
        }

        return null;
    }

    public static @Nullable ResourceLocation getItemResourceLocation(Item item) {
        return JacksEconomy.server.registryAccess().registryOrThrow(Registries.ITEM).getKey(item);
    }

    public static String getItemName(Item item) {
        ResourceLocation resourceLocation = getItemResourceLocation(item);
        return resourceLocation != null ? resourceLocation.toString() : null;
    }

    public static Holder<Item> getHolder(Item item) {
        return JacksEconomy.server.registryAccess().registryOrThrow(Registries.ITEM).getHolder(getItemResourceLocation(item)).orElseThrow();
    }
}
