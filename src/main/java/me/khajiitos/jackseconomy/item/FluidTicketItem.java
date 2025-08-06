package me.khajiitos.jackseconomy.item;

import me.khajiitos.jackseconomy.data.price.FluidDescription;
import me.khajiitos.jackseconomy.init.ComponentReg;
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
        return itemStack.getOrDefault(ComponentReg.TICKET_FLUIDS, new ArrayList<>());
    }

    public static void setFluids(ItemStack itemStack, List<FluidDescription> fluids) {
        itemStack.set(ComponentReg.TICKET_FLUIDS, fluids);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext tooltipContext, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        List<FluidDescription> fluidDescriptions = getFluids(pStack);

        for (FluidDescription fluidDescription : fluidDescriptions) {
            if (fluidDescription.fluid().value() != Fluids.EMPTY) {
                pTooltipComponents.add(Component.literal("- ").append(fluidDescription.fluid().value().getFluidType().getDescription().copy()).withStyle(ChatFormatting.AQUA));
            }
        }

        int processCount = getMaxProcessCount(pStack);
        boolean showProcessCount = processCount > 0;
        boolean showMaxUsage = hasMaxUsage(pStack);

        if (!showProcessCount && !showMaxUsage) return;

        pTooltipComponents.add(Component.empty());
        if (showProcessCount) {
            pTooltipComponents.add(Component.translatable("jackseconomy.fluid_ticket_process_count", processCount).withStyle(ChatFormatting.GREEN));
        }
        if (showMaxUsage) {
            pTooltipComponents.add(Component.translatable("jackseconomy.ticket_uses_left", formatUsesLeft(pStack)).withStyle(ChatFormatting.GREEN));
        }
    }
}
