package me.khajiitos.jackseconomy.item;

import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.util.IDisablable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

public class OIMWalletItem extends Item implements IDisablable {

    public OIMWalletItem() {
        super(new Properties().stacksTo(1));
    }

    public static long getDollars(ItemStack itemStack) {
        IItemHandler itemHandler = itemStack.getCapability(Capabilities.ItemHandler.ITEM);

        if (itemHandler != null) {
            long total = 0;

            for (int i = 0; i < itemHandler.getSlots(); i++) {
                ItemStack item = itemHandler.getStackInSlot(i);

                if (item.getItem() instanceof CurrencyItem currencyItem && !currencyItem.isDisabled()) {
                    total += currencyItem.value.multiply(BigDecimal.valueOf(item.getCount())).longValue();
                }
            }

            return total;
        }
        return 0;
    }

    public static long getTotalDollars(@Nullable ItemStack itemStack, @Nullable Player player) {
        long total = 0;

        if (itemStack != null) {
            if (itemStack.getItem() instanceof GoldenWalletItem) return -1;
            total += getDollars(itemStack);
        }

        if (player != null) {
            for (ItemStack item : player.getInventory().items) {
                if (item.getItem() instanceof CurrencyItem currencyItem && !currencyItem.isDisabled()) {
                    total += currencyItem.value.multiply(BigDecimal.valueOf(item.getCount())).longValue();
                }
            }
        }

        return total;
    }

    @Override
    public boolean isDisabled() {
        return !Config.oneItemCurrencyMode.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        if (isDisabled()) {
            tooltipComponents.addAll(this.getDisabledTooltip());
        }
    }
}
