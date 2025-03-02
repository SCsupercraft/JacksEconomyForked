package me.khajiitos.jackseconomy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GoldenExporterTicketItem extends ExporterTicketItem {

    public GoldenExporterTicketItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("jackseconomy.golden_exporter_manifest_description").withStyle(ChatFormatting.GOLD));

        int processCount = getMaxProcessCount(pStack);
        if (processCount > 0) {
            pTooltipComponents.add(Component.empty());
            pTooltipComponents.add(Component.translatable("jackseconomy.ticket_process_count", processCount).withStyle(ChatFormatting.GREEN));
        }
    }
}
