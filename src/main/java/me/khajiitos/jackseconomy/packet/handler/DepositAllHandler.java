package me.khajiitos.jackseconomy.packet.handler;

//TODO: imports and stuff

import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.item.CheckItem;
import me.khajiitos.jackseconomy.item.CurrencyItem;
import me.khajiitos.jackseconomy.item.InfiniteWalletItem;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.packet.DepositAllPacket;
import me.khajiitos.jackseconomy.packet.UpdateWalletBalancePacket;
import me.khajiitos.jackseconomy.packet.WalletBalanceDifPacket;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class DepositAllHandler {

    public static void handle(DepositAllPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();

        if (sender == null) {
            return;
        }

        ItemStack walletItemStack = CuriosWallet.get(sender);

        if (walletItemStack == null || !(walletItemStack.getItem() instanceof WalletItem walletItem)) {
            return;
        }

        List<ItemStack> currencyItems = sender.getInventory().items.stream().filter(itemStack -> itemStack.getItem() instanceof CurrencyItem).sorted(Comparator.comparingDouble((itemStack) -> ((CurrencyItem)itemStack.getItem()).value.doubleValue())).toList();

        BigDecimal startingBalance = WalletItem.getBalance(walletItemStack);

        if (walletItemStack.getItem() instanceof InfiniteWalletItem) {
            currencyItems.forEach(itemStack -> {
                BigDecimal value;
                if (itemStack.getItem() instanceof CurrencyItem currencyItem) {
                    value = currencyItem.value;
                } else if (itemStack.getItem() instanceof CheckItem) {
                    value = CheckItem.getBalance(itemStack);
                } else {
                    return;
                }

                int count = itemStack.getCount();

                BigDecimal oldBalance = WalletItem.getBalance(walletItemStack);
                BigDecimal dif = value.multiply(BigDecimal.valueOf(count));
                BigDecimal newBalance = oldBalance.add(dif);

                WalletItem.setBalance(walletItemStack, newBalance);

                //Packets.sendToClient(sender, new UpdateWalletBalancePacket(newBalance));
                //Packets.sendToClient(sender, new WalletBalanceDifPacket(dif));

                itemStack.setCount(0);
            });
        } else {
            currencyItems.forEach(itemStack -> {
                BigDecimal value;
                if (itemStack.getItem() instanceof CurrencyItem currencyItem) {
                    value = currencyItem.value;
                } else if (itemStack.getItem() instanceof CheckItem) {
                    value = CheckItem.getBalance(itemStack);
                } else {
                    return;
                }

                int count = itemStack.getCount();

                BigDecimal oldBalance = WalletItem.getBalance(walletItemStack);
                BigDecimal freeBalance = BigDecimal.valueOf(walletItem.getCapacity()).subtract(oldBalance);

                BigDecimal fraction = freeBalance.divide(value, RoundingMode.UP).setScale(0, RoundingMode.UP);

                int toConsume = (int)(value.compareTo(BigDecimal.ZERO) == 0 ? count : Math.min(fraction.longValue(), count));

                if (toConsume <= 0) {
                    return;
                }

                BigDecimal dif = value.multiply(BigDecimal.valueOf(toConsume));
                BigDecimal newBalance = oldBalance.add(dif);
                WalletItem.setBalance(walletItemStack, newBalance);

                //Packets.sendToClient(sender, new UpdateWalletBalancePacket(newBalance));
                //Packets.sendToClient(sender, new WalletBalanceDifPacket(dif));

                itemStack.setCount(count - toConsume);
            });
        }

        BigDecimal newBalance = WalletItem.getBalance(walletItemStack);
        if (walletItemStack.getItem() instanceof WalletItem) {
            newBalance = CurrencyHelper.giveChange(sender, walletItemStack);
        }

        if (startingBalance.compareTo(newBalance) != 0) {
            Packets.sendToClient(sender, new UpdateWalletBalancePacket(newBalance));
            Packets.sendToClient(sender, new WalletBalanceDifPacket(newBalance.subtract(startingBalance)));
        }
    }
}