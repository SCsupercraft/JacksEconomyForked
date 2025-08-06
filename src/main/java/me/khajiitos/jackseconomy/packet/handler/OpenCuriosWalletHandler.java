package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy.item.OIMWalletItem;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.menu.OIMWalletMenu;
import me.khajiitos.jackseconomy.menu.WalletMenu;
import me.khajiitos.jackseconomy.packet.OpenCuriosWalletPacket;
import me.khajiitos.jackseconomy.util.IDisablable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class OpenCuriosWalletHandler {
    public static void handle(OpenCuriosWalletPacket msg, final IPayloadContext context) {
        ServerPlayer sender = (ServerPlayer) context.player();

        ItemStack walletStack = CuriosWallet.get(sender);

        if (walletStack.isEmpty()) {
            return;
        }

        if (walletStack.getItem() instanceof IDisablable disablable && disablable.isDisabled()) {
            return;
        }

        if (walletStack.getItem() instanceof WalletItem) {
            sender.openMenu(
                    new SimpleMenuProvider(
                            (pContainerId, pPlayerInventory, pPlayer1) -> new WalletMenu(pContainerId, pPlayerInventory, walletStack),
                            walletStack.getItem().getDescription()
                    ), buf -> ContainerReg.createBufferForItemMenu(buf, walletStack)
            );
        } else if (walletStack.getItem() instanceof OIMWalletItem) {
            sender.openMenu(
                    new SimpleMenuProvider(
                            (pContainerId, pPlayerInventory, pPlayer1) -> new OIMWalletMenu(pContainerId, pPlayerInventory, walletStack),
                            walletStack.getItem().getDescription()
                    ), buf -> ContainerReg.createBufferForItemMenu(buf, walletStack)
            );
        }
    }
}
