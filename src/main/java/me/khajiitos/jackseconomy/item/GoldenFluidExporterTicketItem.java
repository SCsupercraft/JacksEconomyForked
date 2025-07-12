package me.khajiitos.jackseconomy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GoldenFluidExporterTicketItem extends FluidExporterTicketItem {

    public GoldenFluidExporterTicketItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("jackseconomy.golden_fluid_exporter_manifest_description").withStyle(ChatFormatting.GOLD));

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
