package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.platform.InputConstants;
import me.khajiitos.jackseconomy.screen.AdminShopScreen;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.client.gui.widget.ScrollPanel;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public class ShoppingCartEntry extends AbstractWidget {
    private final Map.Entry<AdminShopScreen.CategorizedShopItem, Integer> shoppingCartItem;
    private final Runnable onChange;
    private final Runnable onRemoveClicked;
    private final boolean oneItemCurrencyMode;
    private final ScrollPanel panel;

    public ShoppingCartEntry(int pX, int pY, int pWidth, int pHeight, boolean oneItemCurrencyMode, Map.Entry<AdminShopScreen.CategorizedShopItem, Integer> shoppingCartItem, Runnable onChange, Runnable onRemoveClicked, ScrollPanel panel) {
        super(pX, pY, pWidth, pHeight, Component.empty());
        this.shoppingCartItem = shoppingCartItem;
        this.onChange = onChange;
        this.onRemoveClicked = onRemoveClicked;
        this.oneItemCurrencyMode = oneItemCurrencyMode;
        this.panel = panel;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        guiGraphics.renderItem(this.shoppingCartItem.getKey().itemDescription().createItemStack(), this.getX() + 3, this.getY());

        MutableComponent itemName = (this.shoppingCartItem.getKey().customName() != null ? Component.literal(this.shoppingCartItem.getKey().customName()) : this.shoppingCartItem.getKey().itemDescription().createItemStack().getHoverName().copy());

        int width = Minecraft.getInstance().font.width(itemName);

        float scale = 1.f;
        int space = this.width - 100;
        if (width > space) {
            scale = (float) space / width;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(scale, scale, scale);
        }
        Component header = itemName.append(Component.literal(" (" + shoppingCartItem.getValue() + ")").withStyle(ChatFormatting.GRAY));
        Component footer = Component.literal(oneItemCurrencyMode ? "$" + (long)(shoppingCartItem.getKey().price() * shoppingCartItem.getValue()) : CurrencyHelper.format(shoppingCartItem.getKey().price() * shoppingCartItem.getValue())).withStyle(ChatFormatting.DARK_GRAY);
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
        int count;
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
            count = 64;
        } else if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)) {
            count = 10;
        } else {
            count = 1;
        }

        if (pMouseX >= this.getX() + 122 && pMouseX <= this.getX() + 137 && pMouseY >= this.getY() + 4 && pMouseY <= this.getY() + 18) {
            if (shoppingCartItem.getValue() > 1) {
                shoppingCartItem.setValue(Math.max(shoppingCartItem.getValue() - count, 1));
                onChange.run();
            }
        } else if (pMouseX >= this.getX() + 142 && pMouseX <= this.getX() + 157 && pMouseY >= this.getY() + 4 && pMouseY <= this.getY() + 18) {
            shoppingCartItem.setValue(shoppingCartItem.getValue() + count);
            onChange.run();
        } else if (pMouseX >= this.getX() + 102 && pMouseX <= this.getX() + 117 && pMouseY >= this.getY() + 4 && pMouseY <= this.getY() + 18) {
            onRemoveClicked.run();
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
