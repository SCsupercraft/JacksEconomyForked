package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.menu.WalletMenu;
import me.khajiitos.jackseconomy.packet.UpdateWalletBalancePacket;
import me.khajiitos.jackseconomy.packet.WalletBalanceDifPacket;
import me.khajiitos.jackseconomy.packet.WithdrawBalanceSpecificPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.math.BigDecimal;

public class WithdrawBalanceSpecificHandler {
    public static void handle(WithdrawBalanceSpecificPacket msg, final IPayloadContext context) {
        ServerPlayer sender = (ServerPlayer) context.player();

        if (!(sender.containerMenu instanceof WalletMenu walletMenu)) {
            return;
        }

        if (msg.items().compareTo(BigDecimal.ONE) < 0) {
            return;
        }

        BigDecimal amount = msg.currencyType().worth.multiply(msg.items());
        ItemStack walletStack = walletMenu.getItemStack();

        if (WalletItem.getBalance(walletStack).compareTo(amount) < 0) {
            return;
        }

        BigDecimal itemsLeft = msg.items();
        while (itemsLeft.compareTo(BigDecimal.ZERO) > 0) {
            int stackAmount = BigDecimal.valueOf(64).min(itemsLeft).intValue();
            ItemStack itemStack = new ItemStack(msg.currencyType().item, stackAmount);

            if (!sender.getInventory().add(itemStack)) {
                ItemEntity itemEntity = new ItemEntity(sender.level(), sender.getX(), sender.getY(), sender.getZ(), itemStack);
                sender.level().addFreshEntity(itemEntity);
            }

            itemsLeft = itemsLeft.subtract(BigDecimal.valueOf(stackAmount));
        }

        WalletItem.setBalance(walletStack, WalletItem.getBalance(walletStack).subtract(amount));
        PacketDistributor.sendToPlayer(sender, new UpdateWalletBalancePacket(WalletItem.getBalance(walletStack)));
        PacketDistributor.sendToPlayer(sender, new WalletBalanceDifPacket(amount.negate()));
    }
}
