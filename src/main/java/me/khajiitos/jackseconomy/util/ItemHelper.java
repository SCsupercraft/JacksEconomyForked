package me.khajiitos.jackseconomy.util;

import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class ItemHelper {
    public static void dropItem(ItemStack itemStack, Level pLevel, BlockPos pPos) {
        ItemEntity dropItem = new ItemEntity(pLevel, pPos.getX() + 0.5, pPos.getY() + 1.0, pPos.getZ() + 0.5, itemStack);
        pLevel.addFreshEntity(dropItem);
    }

    public static @Nullable Item getItem(String name) {
        try {
            ResourceLocation resourceLocation = new ResourceLocation(name);
            Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);

            if (item == null) {
                JacksEconomy.LOGGER.info("Invalid item: {}", name);
            }

            return item;
        } catch (ResourceLocationException e) {
            JacksEconomy.LOGGER.warn("Invalid resource location: {}", name);
        }

        return null;
    }

    public static String getItemName(Item item) {
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey(item);

        if (resourceLocation != null) {
            return resourceLocation.toString();
        }

        return null;
    }
}
