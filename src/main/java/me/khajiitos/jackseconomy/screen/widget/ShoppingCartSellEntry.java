package me.khajiitos.jackseconomy.screen.widget;

import me.khajiitos.jackseconomy.data.price.ItemDescription;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.client.gui.widget.ScrollPanel;

import java.util.Map;

public class ShoppingCartSellEntry extends AbstractWidget {
    private final Map.Entry<ItemDescription, Integer> itemToSell;
    private final Runnable onChange;
    private final Runnable onRemoveClicked;
    private final double itemPrice;
    private final boolean oneItemCurrencyMode;
    private final ScrollPanel panel;

    public ShoppingCartSellEntry(int pX, int pY, int pWidth, int pHeight, boolean oneItemCurrencyMode, Map.Entry<ItemDescription, Integer> itemToSell, double itemPrice, Inventory inventory, Runnable onChange, Runnable onRemoveClicked, ScrollPanel panel) {
        super(pX, pY, pWidth, pHeight, Component.empty());
        this.itemToSell = itemToSell;
        this.onChange = onChange;
        this.onRemoveClicked = onRemoveClicked;
        this.itemPrice = itemPrice;
        this.oneItemCurrencyMode = oneItemCurrencyMode;
        this.panel = panel;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        guiGraphics.renderItem(this.itemToSell.getKey().createItemStack(), this.getX() + 3, this.getY());

        MutableComponent itemName = this.itemToSell.getKey().item().getDescription().copy();

        int width = Minecraft.getInstance().font.width(itemName);

        float scale = 1.f;
        int space = this.width - 100;
        if (width > space) {
            scale = (float) space / width;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(scale, scale, scale);
        }

        Component header = itemName.append(Component.literal(" (" + itemToSell.getValue() + ")").withStyle(ChatFormatting.GRAY));
        Component footer = Component.literal(oneItemCurrencyMode ? "$" + (long)(itemPrice * itemToSell.getValue()) : CurrencyHelper.format(itemPrice * itemToSell.getValue())).withStyle(ChatFormatting.DARK_GRAY);
        guiGraphics.drawString(Minecraft.getInstance().font, header, (int) ((this.getX() + 22) / scale), (int) ((this.getY()) / scale), 0xFFFFFFFF);
        guiGraphics.drawString(Minecraft.getInstance().font, footer, (int) ((this.getX() + 22) / scale), (int) ((this.getY() + 9) / scale), 0xFFFFFFFF);

        if (width > space) {
            guiGraphics.pose().popPose();
        }

        boolean panelHovered = panel.isMouseOver(pMouseX, pMouseY);
        boolean removeHovered = panelHovered && pMouseX >= this.getX() + 102 && pMouseX <= this.getX() + 117 && pMouseY >= this.getY() + 4 && pMouseY <= this.getY() + 18;
        boolean minusHovered = panelHovered && pMouseX >= this.getX() + 122 && pMouseX <= this.getX() + 137 && pMouseY >= this.getY() + 4 && pMouseY <= this.getY() + 18;
        boolean plusHovered = panelHovered && pMouseX >= this.getX() + 142 && pMouseX <= this.getX() + 157 && pMouseY >= this.getY() + 4 && pMouseY <= this.getY() + 18;

        guiGraphics.fill(this.getX() + 100, this.getY() + 3, this.getX() + 115, this.getY() + 18, removeHovered ? 0xFFFFFFFF : 0xFF000000);
        guiGraphics.fill(this.getX() + 101, this.getY() + 4, this.getX() + 114, this.getY() + 17, 0xFF666666);

        guiGraphics.fill(this.getX() + 120, this.getY() + 3, this.getX() + 135, this.getY() + 18, minusHovered ? 0xFFFFFFFF : 0xFF000000);
        guiGraphics.fill(this.getX() + 121, this.getY() + 4, this.getX() + 134, this.getY() + 17, 0xFF666666);

        guiGraphics.fill(this.getX() + 140, this.getY() + 3, this.getX() + 155, this.getY() + 18, plusHovered ? 0xFFFFFFFF : 0xFF000000);
        guiGraphics.fill(this.getX() + 141, this.getY() + 4, this.getX() + 154, this.getY() + 17, 0xFF666666);

        guiGraphics.drawString(Minecraft.getInstance().font, "-", (this.getX() + 125), (this.getY() + 7), 0xFFFFFFFF);
        guiGraphics.drawString(Minecraft.getInstance().font, "+", (this.getX() + 145), (this.getY() + 7), 0xFFFFFFFF);
        guiGraphics.drawString(Minecraft.getInstance().font, "x", (this.getX() + 105), (this.getY() + 6), 0xFFFF0000);
    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        if (pMouseX >= this.getX() + 122 && pMouseX <= this.getX() + 137 && pMouseY >= this.getY() + 4 && pMouseY <= this.getY() + 18) {
            if (itemToSell.getValue() > 1) {
                itemToSell.setValue(itemToSell.getValue() - 1);
                onChange.run();
            }
        } else if (pMouseX >= this.getX() + 142 && pMouseX <= this.getX() + 157 && pMouseY >= this.getY() + 4 && pMouseY <= this.getY() + 18) {
            itemToSell.setValue(itemToSell.getValue() + 1);
            onChange.run();
        } else if (pMouseX >= this.getX() + 102 && pMouseX <= this.getX() + 117 && pMouseY >= this.getY() + 4 && pMouseY <= this.getY() + 18) {
            onRemoveClicked.run();
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
