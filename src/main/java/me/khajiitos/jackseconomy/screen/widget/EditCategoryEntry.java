package me.khajiitos.jackseconomy.screen.widget;

import me.khajiitos.jackseconomy.screen.AdminShopScreen;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class EditCategoryEntry extends CategoryEntry {
    private final Runnable onHovered;

    public EditCategoryEntry(int pX, int pY, int pWidth, int pHeight, AdminShopScreen.InnerCategory category, BiConsumer<CategoryEntry, Integer> onClick, Supplier<Boolean> isSelectedSupplier, Runnable onHovered) {
        super(pX, pY, pWidth, pHeight, category, onClick, isSelectedSupplier, () -> false);
        this.onHovered = onHovered;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(guiGraphics, pMouseX, pMouseY, pPartialTick);

        if (this.isHovered) {
            this.onHovered.run();
        }
    }
}
