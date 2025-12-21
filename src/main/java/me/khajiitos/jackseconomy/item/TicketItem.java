package me.khajiitos.jackseconomy.item;

import me.khajiitos.jackseconomy.data.price.ItemDescription;
import me.khajiitos.jackseconomy.init.ComponentReg;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public abstract class TicketItem extends Item {
    public TicketItem() {
        super(new Properties().stacksTo(1));
    }

    public TicketItem(Properties properties) {
        super(properties);
    }

    public static List<ItemDescription> getItems(ItemStack itemStack) {
        return itemStack.getOrDefault(ComponentReg.TICKET_ITEMS, new ArrayList<>());
    }

    public static void setItems(ItemStack itemStack, List<ItemDescription> items) {
        itemStack.set(ComponentReg.TICKET_ITEMS, items);
    }

    public static int getMaxProcessCount(ItemStack itemStack) {
        return itemStack.getOrDefault(ComponentReg.MAX_PROCESS_COUNT, itemStack.getItem() instanceof FluidTicketItem ? 1000 : 1);
    }

    public static void setMaxProcessCount(ItemStack itemStack, int processCount) {
        itemStack.set(ComponentReg.MAX_PROCESS_COUNT, processCount);
    }

    public static void setMaxUsage(ItemStack itemStack, int useCount) {
        itemStack.setDamageValue(0);
        itemStack.set(DataComponents.MAX_DAMAGE, useCount);
    }

    public static int getMaxUsage(ItemStack itemStack) {
        return itemStack.getOrDefault(DataComponents.MAX_DAMAGE, 1);
    }

    public static void removeMaxUsage(ItemStack itemStack) {
        itemStack.setDamageValue(0);
        itemStack.remove(DataComponents.MAX_DAMAGE);
    }

    public static boolean hasMaxUsage(ItemStack itemStack) {
        return itemStack.has(DataComponents.MAX_DAMAGE);
    }

    public static int getUsesLeft(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof TicketItem) || !hasMaxUsage(itemStack)) {
            return 1;
        }

        return getMaxUsage(itemStack) - itemStack.getOrDefault(DataComponents.DAMAGE, 0);
    }

    public static boolean handleDamage(ItemStack stack, int damage) {
        if (!hasMaxUsage(stack) || !(stack.getItem() instanceof TicketItem)) return false;

        damage = stack.getDamageValue() + damage;
        stack.setDamageValue(damage);

        if (damage >= stack.getMaxDamage()) {
            stack.shrink(1);
            stack.setDamageValue(0);
            return true;
        }
        return false;
    }

    public static boolean handleDamageWithSound(ItemStack stack, int damage, Level level, BlockPos blockPos) {
        boolean hasBroke = handleDamage(stack, damage);
        if (hasBroke && !level.isClientSide) {
            level.playSound(
                    null,
                    blockPos,
                    SoundEvents.ITEM_BREAK,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );
        }
        return hasBroke;
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext tooltipContext, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        List<ItemDescription> itemDescriptions = getItems(pStack);

        for (ItemDescription itemDescription : itemDescriptions) {
            if (itemDescription.item().value() != Items.AIR) {
                pTooltipComponents.add(Component.literal("- ").append(itemDescription.createItemStack().getHoverName().copy()).withStyle(ChatFormatting.AQUA));
            }
        }

        int processCount = getMaxProcessCount(pStack);
        boolean showProcessCount = processCount > 0;
        boolean showMaxUsage = hasMaxUsage(pStack);

        if (!showProcessCount && !showMaxUsage) return;

        pTooltipComponents.add(Component.empty());
        if (showProcessCount) {
            pTooltipComponents.add(Component.translatable("jackseconomy.ticket_process_count", processCount).withStyle(ChatFormatting.GREEN));
        }
        if (showMaxUsage) {
            pTooltipComponents.add(Component.translatable("jackseconomy.ticket_uses_left", formatUsesLeft(pStack)).withStyle(ChatFormatting.GREEN));
        }
    }

    public String formatUsesLeft(ItemStack stack) {
        int uses = getUsesLeft(stack);

        if (uses >= 1000000) {
            return uses / 1000000 + "M";
        } else if (uses >= 1000) {
            return uses / 1000 + "K";
        }
        return Integer.toString(uses);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return getMaxUsage(stack);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return hasMaxUsage(stack);
    }
}
