package me.khajiitos.jackseconomy.item;

import me.khajiitos.jackseconomy.data.price.FluidDescription;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class FluidTicketItem extends TicketItem {
    public FluidTicketItem() {
        super(new Properties().stacksTo(1));
    }

    public FluidTicketItem(Properties properties) {
        super(properties);
    }

    public static List<FluidDescription> getFluids(ItemStack itemStack) {
        List<FluidDescription> list = new ArrayList<>();

        if (!(itemStack.getItem() instanceof FluidTicketItem)) {
            return list;
        }

        CompoundTag nbtTag = itemStack.getTag();

        if (nbtTag == null) {
            return list;
        }

        ListTag listTag = nbtTag.getList("Fluids", Tag.TAG_COMPOUND);
        listTag.forEach(tag -> {
            if (tag instanceof CompoundTag compoundTag) {
                FluidDescription fluidDescription = FluidDescription.fromNbt(compoundTag);
                if (fluidDescription != null) {
                    list.add(fluidDescription);
                }
            }
        });
        return list;
    }

    public static void setFluids(ItemStack itemStack, List<FluidDescription> items) {
        ListTag tag = new ListTag();
        items.forEach(s -> tag.add(s.toNbt()));
        itemStack.getOrCreateTag().put("Fluids", tag);
    }

    public static int getMaxProcessCount(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof FluidTicketItem)) {
            return 0;
        }

        CompoundTag nbtTag = itemStack.getTag();

        if (nbtTag == null || !nbtTag.contains("MaxProcessCount", Tag.TAG_INT)) {
            return 1000;
        }

        return nbtTag.getInt("MaxProcessCount");
    }

    public static void setMaxProcessCount(ItemStack itemStack, int processCount) {
        if (!(itemStack.getItem() instanceof FluidTicketItem)) {
            return;
        }

        CompoundTag nbtTag = itemStack.getOrCreateTag();
        nbtTag.putInt("MaxProcessCount", processCount);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        List<FluidDescription> fluidDescriptions = getFluids(pStack);

        for (FluidDescription fluidDescription : fluidDescriptions) {
            if (fluidDescription.fluid() != Fluids.EMPTY) {
                pTooltipComponents.add(Component.literal("- ").append(fluidDescription.fluid().getFluidType().getDescription().copy()).withStyle(ChatFormatting.AQUA));
            }
        }

        int processCount = getMaxProcessCount(pStack);
        if (processCount > 0) {
            pTooltipComponents.add(Component.empty());
            pTooltipComponents.add(Component.translatable("jackseconomy.fluid_ticket_process_count", processCount).withStyle(ChatFormatting.GREEN));
        }
    }
}
