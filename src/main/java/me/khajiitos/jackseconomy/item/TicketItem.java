package me.khajiitos.jackseconomy.item;

import me.khajiitos.jackseconomy.data.price.ItemDescription;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class TicketItem extends Item {
    public TicketItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public TicketItem(Item.Properties properties) {
        super(properties);
    }

    public static List<ItemDescription> getItems(ItemStack itemStack) {
        List<ItemDescription> list = new ArrayList<>();

        if (!(itemStack.getItem() instanceof TicketItem)) {
            return list;
        }

        CompoundTag nbtTag = itemStack.getTag();

        if (nbtTag == null) {
            return list;
        }

        ListTag listTag = nbtTag.getList("Items", Tag.TAG_COMPOUND);
        listTag.forEach(tag -> {
            if (tag instanceof CompoundTag compoundTag) {
                ItemDescription itemDescription = ItemDescription.fromNbt(compoundTag);
                if (itemDescription != null) {
                    list.add(itemDescription);
                }
            }
        });
        return list;
    }

    public static void setItems(ItemStack itemStack, List<ItemDescription> items) {
        ListTag tag = new ListTag();
        items.forEach(s -> tag.add(s.toNbt()));
        itemStack.getOrCreateTag().put("Items", tag);
    }

    public static int getMaxProcessCount(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof TicketItem)) {
            return 0;
        }

        CompoundTag nbtTag = itemStack.getTag();

        if (nbtTag == null || !nbtTag.contains("MaxProcessCount", Tag.TAG_INT)) {
            return itemStack.getItem() instanceof FluidTicketItem ? 1000 : 1;
        }

        return nbtTag.getInt("MaxProcessCount");
    }

    public static void setMaxProcessCount(ItemStack itemStack, int processCount) {
        if (!(itemStack.getItem() instanceof TicketItem)) {
            return;
        }

        CompoundTag nbtTag = itemStack.getOrCreateTag();
        nbtTag.putInt("MaxProcessCount", processCount);
    }

    public static void setMaxUsage(ItemStack itemStack, int useCount) {
        if (!(itemStack.getItem() instanceof TicketItem)) {
            return;
        }

        itemStack.setDamageValue(0);

        CompoundTag nbtTag = itemStack.getOrCreateTag();
        nbtTag.putInt("MaxUsage", useCount);
    }

    public static int getMaxUsage(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof TicketItem)) {
            return 0;
        }

        CompoundTag nbtTag = itemStack.getTag();

        if (nbtTag == null || !nbtTag.contains("MaxUsage", Tag.TAG_INT)) {
            return 1;
        }

        return nbtTag.getInt("MaxUsage");
    }

    public static void removeMaxUsage(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof TicketItem)) {
            return;
        }

        itemStack.setDamageValue(0);

        CompoundTag nbtTag = itemStack.getOrCreateTag();
        nbtTag.remove("MaxUsage");
    }

    public static boolean hasMaxUsage(ItemStack itemStack) {
        CompoundTag nbtTag = itemStack.getTag();
        return nbtTag != null && nbtTag.contains("MaxUsage", Tag.TAG_INT);
    }

    public static int getUsesLeft(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof TicketItem)) {
            return 0;
        }

        if (!hasMaxUsage(itemStack)) return Integer.MAX_VALUE;

        CompoundTag nbtTag = itemStack.getTag();
        int maxUsage = getMaxUsage(itemStack);

        if (nbtTag == null || !nbtTag.contains("Damage", Tag.TAG_INT)) return maxUsage;
        return maxUsage - nbtTag.getInt("Damage");
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
                    null, // Player - null means all nearby players hear it
                    blockPos,  // Position of the block entity
                    SoundEvents.ITEM_BREAK, // The sound event
                    SoundSource.BLOCKS,     // Sound category
                    1.0F,                   // Volume
                    1.0F                    // Pitch
            );
        }
        return hasBroke;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        List<ItemDescription> itemDescriptions = getItems(pStack);

        for (ItemDescription itemDescription : itemDescriptions) {
            if (itemDescription.item() != Items.AIR) {
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
        return TicketItem.getMaxUsage(stack);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return hasMaxUsage(stack);
    }
}
