package me.khajiitos.jackseconomy.item;

import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import me.khajiitos.jackseconomy.init.Sounds;
import me.khajiitos.jackseconomy.packet.WalletBalanceDifPacket;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import me.khajiitos.jackseconomy.util.IDisablable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CurrencyItem extends Item implements IDisablable {
    public final BigDecimal value;
    public final boolean bill;

    public CurrencyItem(BigDecimal value, boolean bill) {
        super(new Properties());
        this.value = value;
        this.bill = bill;
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext tooltipContext, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (Config.oneItemCurrencyMode.get() && this == ItemBlockReg.DOLLAR_BILL_ITEM.get()) {
            pTooltipComponents.add(Component.translatable("jackseconomy.value", Component.literal("$" + value.longValue()).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GOLD));
        } else {
            pTooltipComponents.add(Component.translatable("jackseconomy.value", Component.literal(CurrencyHelper.format(value)).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GOLD));
        }

        if (this.isDisabled()) {
            pTooltipComponents.addAll(this.getDisabledTooltip());
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);

        if (pLevel.isClientSide) {
            return InteractionResultHolder.fail(itemStack);
        }

        if (this.isDisabled()) {
            return InteractionResultHolder.fail(itemStack);
        }

        ItemStack wallet = CuriosWallet.get(pPlayer);

        if (wallet.isEmpty()) {
            return super.use(pLevel, pPlayer, pUsedHand);
        }

        if (wallet.getItem() instanceof InfiniteWalletItem) {
            long insertCount = itemStack.getCount();

            if (insertCount <= 0) {
                return super.use(pLevel, pPlayer, pUsedHand);
            }

            if (!pPlayer.isCrouching()) {
                insertCount = 1;
            }

            BigDecimal oldBalance = WalletItem.getBalance(wallet);
            BigDecimal balanceAdded = this.value.multiply(new BigDecimal(insertCount));
            BigDecimal newBalance = CurrencyHelper.addMoney(balanceAdded, wallet, pPlayer);
            itemStack.shrink((int)insertCount);

            if (pPlayer instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new WalletBalanceDifPacket(newBalance.subtract(oldBalance)));
            }
        } else if (wallet.getItem() instanceof WalletItem walletItem) {
            BigDecimal oldBalance = WalletItem.getBalance(wallet);
            BigDecimal left = BigDecimal.valueOf(walletItem.getCapacity()).subtract(oldBalance);
            BigDecimal fraction = left.divide(value, RoundingMode.UP).setScale(0, RoundingMode.UP);

            long insertCount = Math.min(fraction.longValue(), itemStack.getCount());

            if (insertCount <= 0) {
                return super.use(pLevel, pPlayer, pUsedHand);
            }

            if (!pPlayer.isCrouching()) {
                insertCount = 1;
            }

            BigDecimal balanceAdded = this.value.multiply(new BigDecimal(insertCount));
            BigDecimal newBalance = CurrencyHelper.addMoney(balanceAdded, wallet, pPlayer);
            itemStack.shrink((int)insertCount);

            if (pPlayer instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new WalletBalanceDifPacket(newBalance.subtract(oldBalance)));
            }
        } else if (wallet.getItem() instanceof OIMWalletItem walletItem) {
            IItemHandler itemHandler = wallet.getCapability(Capabilities.ItemHandler.ITEM);
            if (itemHandler != null) {
                int insertCount = pPlayer.isCrouching() ? itemStack.getCount() : 1;

                ItemStack stack = itemStack.copyWithCount(insertCount);
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    stack = itemHandler.insertItem(i, stack, false);

                    if (stack.isEmpty()) {
                        break;
                    }
                }
                int used = insertCount - stack.getCount();

                if (used == 0) {
                    return super.use(pLevel, pPlayer, pUsedHand);
                }

                itemStack.shrink(used);
            }
        } else {
            return super.use(pLevel, pPlayer, pUsedHand);
        }

        if (this.bill) {
            pPlayer.level().playSound(null, pPlayer.blockPosition(), Sounds.CASH.get(), SoundSource.PLAYERS, 1.f, 1.f);
        } else {
            pPlayer.level().playSound(null, pPlayer.blockPosition(), Sounds.COIN.get(), SoundSource.PLAYERS, 1.f, 1.f);
        }

        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public boolean isDisabled() {
        return this != ItemBlockReg.DOLLAR_BILL_ITEM.get() && Config.oneItemCurrencyMode.get();
    }
}
