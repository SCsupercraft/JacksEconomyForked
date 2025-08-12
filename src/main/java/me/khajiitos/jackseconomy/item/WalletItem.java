package me.khajiitos.jackseconomy.item;

import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.init.ComponentReg;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import me.khajiitos.jackseconomy.util.IDisablable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;

public class WalletItem extends Item implements IDisablable {
	// to be edited with values from the config
	public Supplier<ModConfigSpec.ConfigValue<Double>> capacity;

	public WalletItem(Supplier<ModConfigSpec.ConfigValue<Double>> capacity) {
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
			return itemStack.getItem() instanceof GoldenWalletItem ? BigDecimal.valueOf(-1) : itemStack.getOrDefault(ComponentReg.BALANCE, BigDecimal.ZERO);
		} catch (NumberFormatException e) {
			return BigDecimal.ZERO;
		}
	}

	public static void setBalance(ItemStack itemStack, double balance) {
		setBalance(itemStack, BigDecimal.valueOf(balance));
	}

	public static void setBalance(ItemStack itemStack, BigDecimal balance) {
		itemStack.set(ComponentReg.BALANCE, balance);
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
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
		if (isDisabled()) {
			tooltipComponents.addAll(this.getDisabledTooltip());
		} else {
			BigDecimal balance = getBalance(stack);
			tooltipComponents.add(Component.translatable("jackseconomy.balance_out_of", Component.literal(CurrencyHelper.format(balance)).withStyle(ChatFormatting.YELLOW), Component.literal(CurrencyHelper.format(getCapacity())).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GOLD));
		}
	}

	@Override
	public boolean isDisabled() {
		return Config.oneItemCurrencyMode.get();
	}
}
