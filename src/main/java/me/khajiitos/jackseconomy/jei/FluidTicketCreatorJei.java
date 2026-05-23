package me.khajiitos.jackseconomy.jei;

import me.khajiitos.jackseconomy.menu.FluidTicketCreatorMenu;
import me.khajiitos.jackseconomy.screen.FluidTicketCreatorScreen;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FluidTicketCreatorJei implements IGhostIngredientHandler<FluidTicketCreatorScreen> {
    @Override
    public <I> @NotNull List<Target<I>> getTargetsTyped(@NotNull FluidTicketCreatorScreen gui, ITypedIngredient<I> ingredient, boolean doStart) {
        if (ingredient.getType() != VanillaTypes.ITEM_STACK
                || gui.getMinecraft().player == null
                || !gui.getMinecraft().player.isCreative()
                || !FluidTicketCreatorMenu.mayPlace(ingredient.getIngredient(VanillaTypes.ITEM_STACK)
                    .orElseThrow())
        ) return List.of();

        ArrayList<Target<I>> targets = new ArrayList<>();

        for (Slot slot : gui.getMenu().slots) {
            if (slot.hasItem() || slot.container != gui.getMenu().container) continue;

            targets.add(new TicketCreatorTarget<>(gui, slot));
        }

        return targets;
    }

    @Override
    public void onComplete() {

    }
}
