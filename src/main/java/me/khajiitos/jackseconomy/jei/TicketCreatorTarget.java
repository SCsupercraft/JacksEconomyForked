package me.khajiitos.jackseconomy.jei;

import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.packet.JeiInsertGhostItemPacket;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

record TicketCreatorTarget<I>(@NotNull AbstractContainerScreen<?> gui,
                              Slot slot) implements IGhostIngredientHandler.Target<I> {
    @Override
    public @NotNull Rect2i getArea() {
        return new Rect2i(gui.getGuiLeft() + slot.x, gui.getGuiTop() + slot.y, 16, 16);
    }

    @Override
    public void accept(@NotNull I ingredient) {
        if (!slot.hasItem()) {
            ItemStack stack = ((ItemStack) ingredient).copyWithCount(1);

            slot.set(stack);
            Packets.sendToServer(new JeiInsertGhostItemPacket(stack, slot.getSlotIndex()));
        }
    }
}
