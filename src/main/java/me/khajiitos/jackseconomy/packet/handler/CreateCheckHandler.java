package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.init.ItemBlockReg;
import me.khajiitos.jackseconomy.item.CheckItem;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.menu.WalletMenu;
import me.khajiitos.jackseconomy.packet.CreateCheckPacket;
import me.khajiitos.jackseconomy.packet.UpdateWalletBalancePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.math.BigDecimal;

public class CreateCheckHandler {
    public static void handle(CreateCheckPacket msg, final IPayloadContext context) {
        ServerPlayer sender = (ServerPlayer) context.player();

        if (!(sender.containerMenu instanceof WalletMenu walletMenu)) {
            return;
        }

        ItemStack walletStack = walletMenu.getItemStack();

        if (WalletItem.getBalance(walletStack).compareTo(msg.amount()) < 0) {
            return;
        }

        if (msg.amount().compareTo(BigDecimal.ONE) < 0) {
            return;
        }

        ItemStack checkItem = new ItemStack(ItemBlockReg.CHECK_ITEM.get());
        CheckItem.setBalance(checkItem, msg.amount());
        WalletItem.setBalance(walletStack, WalletItem.getBalance(walletStack).subtract(msg.amount()));
        PacketDistributor.sendToPlayer(sender, new UpdateWalletBalancePacket(WalletItem.getBalance(walletStack)));
        if (!sender.getInventory().add(checkItem)) {
            ItemEntity itemEntity = new ItemEntity(sender.level(), sender.getX(), sender.getY(), sender.getZ(), checkItem);
            sender.level().addFreshEntity(itemEntity);
        }
    }
}
