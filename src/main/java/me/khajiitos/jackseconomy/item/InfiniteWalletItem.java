package me.khajiitos.jackseconomy.item;

import me.khajiitos.jackseconomy.util.CurrencyHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.math.BigDecimal;
import java.util.List;

public class InfiniteWalletItem extends WalletItem {
    public InfiniteWalletItem() { super(); }

    @Override
    public double getCapacity() {
        return Double.MAX_VALUE;
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext tooltipContext, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (isDisabled()) {
            pTooltipComponents.addAll(this.getDisabledTooltip());
        } else {
            BigDecimal balance = getBalance(pStack);
            pTooltipComponents.add(Component.translatable("jackseconomy.balance", Component.literal(CurrencyHelper.format(balance)).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GOLD));
        }
    }
}
