package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.item.CheckItem;
import me.khajiitos.jackseconomy.item.CurrencyItem;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.packet.InsertToWalletPacket;
import me.khajiitos.jackseconomy.packet.UpdateWalletBalancePacket;
import me.khajiitos.jackseconomy.packet.WalletBalanceDifPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InsertToWalletHandler {
    public static void handle(InsertToWalletPacket msg, final IPayloadContext context) {
        ServerPlayer sender = (ServerPlayer) context.player();

        if (msg.slotId() >= 0 && msg.slotId() < sender.containerMenu.slots.size()) {
            ItemStack walletItemStack = CuriosWallet.get(sender);

            if (walletItemStack == null || !(walletItemStack.getItem() instanceof WalletItem walletItem)) {
                return;
            }

            ItemStack clickedItem = sender.containerMenu.slots.get(msg.slotId()).getItem();

            BigDecimal value;
            if (clickedItem.getItem() instanceof CurrencyItem currencyItem) {
                value = currencyItem.value;
            } else if (clickedItem.getItem() instanceof CheckItem) {
                value = CheckItem.getBalance(clickedItem);
            } else {
                return;
            }

            int count = clickedItem.getCount();

            BigDecimal oldBalance = WalletItem.getBalance(walletItemStack);
            BigDecimal freeBalance = BigDecimal.valueOf(walletItem.getCapacity()).subtract(oldBalance);

            BigDecimal fraction = freeBalance.divide(value, RoundingMode.UP).setScale(0, RoundingMode.UP);

            int toConsume = value.compareTo(BigDecimal.ZERO) == 0 ? count : Math.min(fraction.intValue(), count);

            if (toConsume <= 0) {
                return;
            }

            BigDecimal dif = value.multiply(BigDecimal.valueOf(toConsume));
            BigDecimal newBalance = oldBalance.add(dif);
            WalletItem.setBalance(walletItemStack, newBalance);

            PacketDistributor.sendToPlayer(sender, new UpdateWalletBalancePacket(newBalance));
            PacketDistributor.sendToPlayer(sender, new WalletBalanceDifPacket(dif));

            clickedItem.setCount(count - toConsume);
        }
    }
}
