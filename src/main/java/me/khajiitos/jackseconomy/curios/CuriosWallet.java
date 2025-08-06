package me.khajiitos.jackseconomy.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CuriosWallet {
    public static ItemStack get(Player player) {
        if (CuriosCheck.isInstalled()) {
            return CuriosHandler.getWallet(player);
        }
        return ItemStack.EMPTY;
    }
}
