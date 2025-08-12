package me.khajiitos.jackseconomy.curios;

import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Map;

// FIXME: Fix before release of 1.2.2-1.6.0
//  Refer to our 1.21.1 branch and Curios' documentation
public class CuriosHandler {

    // FIXME: EVERYTHING is deprecated...
    // but I don't feel like fixing it

    @SubscribeEvent
    public static void onImc(InterModEnqueueEvent e) {
        InterModComms.sendTo("curios",
                "register_type",
                () -> new SlotTypeMessage.Builder("wallet")
                        .icon(new ResourceLocation(JacksEconomy.MOD_ID, "gui/wallet_slot"))
                        .build());
    }

    public static ItemStack getWallet(Player player) {
        ICuriosItemHandler handler = CuriosApi.getCuriosHelper().getCuriosHandler(player).resolve().orElse(null);

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
}
