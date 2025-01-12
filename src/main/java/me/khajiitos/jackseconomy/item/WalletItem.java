package me.khajiitos.jackseconomy.item;

import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import me.khajiitos.jackseconomy.util.IDisablable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;

public class WalletItem extends Item implements IDisablable {
    // to be edited with values from the config
    public Supplier<ForgeConfigSpec.ConfigValue<Double>> capacity;

    public WalletItem(Supplier<ForgeConfigSpec.ConfigValue<Double>> capacity) {
        super(new Properties().stacksTo(1));
        this.capacity = capacity;
    }
    public WalletItem() {
        super(new Properties().stacksTo(1));
        this.capacity = null;
    }

    public double getCapacity() {
        return capacity.get().get();
    }

    public static BigDecimal getBalance(ItemStack itemStack) {
        try {
            return new BigDecimal(itemStack.getOrCreateTag().getString("Balance"));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    public static void setBalance(ItemStack itemStack, double balance) {
        setBalance(itemStack, BigDecimal.valueOf(balance));
    }

    public static void setBalance(ItemStack itemStack, BigDecimal balance) {
        itemStack.getOrCreateTag().putString("Balance", balance.toString());
    }

    /*
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);

        if (pPlayer instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider(((pContainerId, pPlayerInventory, pPlayer1) ->
                            new WalletMenu(pContainerId, pPlayerInventory, itemStack)),
                            itemStack.getItem().getDescription()), friendlyByteBuf -> friendlyByteBuf.writeItem(itemStack));
        }

        return InteractionResultHolder.success(itemStack);
    }*/

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (isDisabled()) {
            pTooltipComponents.addAll(this.getDisabledTooltip());
        } else {
            BigDecimal balance = getBalance(pStack);
            pTooltipComponents.add(Component.translatable("jackseconomy.balance_out_of", Component.literal(CurrencyHelper.format(balance)).withStyle(ChatFormatting.YELLOW), Component.literal(CurrencyHelper.format(getCapacity())).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    public boolean isDisabled() {
        return Config.oneItemCurrencyMode.get();
    }
}
