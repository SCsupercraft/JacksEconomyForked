package me.khajiitos.jackseconomy.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CuriosWallet {
    public static @NotNull ItemStack get(Player player) {
        if (CuriosCheck.isInstalled()) {
            return CuriosHandler.getWallet(player);
        }
        return ItemStack.EMPTY;
    }
}
