package me.khajiitos.jackseconomy.curios;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.item.GoldenWalletItem;
import me.khajiitos.jackseconomy.item.OIMWalletItem;
import me.khajiitos.jackseconomy.item.WalletItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Map;

public class CuriosHandler {
    public static @NotNull ItemStack getWallet(Player player) {
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(player).resolve().orElse(null);

        if (handler != null) {
            Map<String, ICurioStacksHandler> curios = handler.getCurios();
            if (curios != null && curios.containsKey("wallet")) {
                IDynamicStackHandler stacks = curios.get("wallet").getStacks();
                if (stacks != null) {
                    return stacks.getStackInSlot(0);
                }
            }
        }

        return ItemStack.EMPTY;
    }

    public static void init() {
        CuriosApi.registerCurioPredicate(new ResourceLocation(JacksEconomy.MOD_ID, "wallet"), (slotResult) -> {
            Item item = slotResult.stack().getItem();
            return item instanceof WalletItem || item instanceof OIMWalletItem || item instanceof GoldenWalletItem;
        });
    }
}
