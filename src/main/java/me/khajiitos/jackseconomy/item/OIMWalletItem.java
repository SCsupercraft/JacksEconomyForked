package me.khajiitos.jackseconomy.item;

import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.util.IDisablable;
import me.khajiitos.jackseconomy.util.OIMWalletCapabilityWrapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class OIMWalletItem extends Item implements IDisablable {

    public OIMWalletItem() {
        super(new Properties().stacksTo(1));
    }

    public static long getDollars(ItemStack itemStack) {
        LazyOptional<IItemHandler> cap = itemStack.getCapability(ForgeCapabilities.ITEM_HANDLER);
        if (cap.isPresent()) {
            Optional<IItemHandler> capOptional = cap.resolve();

            if (capOptional.isPresent()) {
                IItemHandler itemHandler = capOptional.get();

                long total = 0;

                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    ItemStack item = itemHandler.getStackInSlot(i);

                    if (item.getItem() instanceof CurrencyItem currencyItem && !currencyItem.isDisabled()) {
                        total += currencyItem.value.multiply(BigDecimal.valueOf(item.getCount())).longValue();
                    }
                }

                return total;
            }
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
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (isDisabled()) {
            pTooltipComponents.addAll(this.getDisabledTooltip());
        }
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return OIMWalletCapabilityWrapper.create(stack);
    }
}
