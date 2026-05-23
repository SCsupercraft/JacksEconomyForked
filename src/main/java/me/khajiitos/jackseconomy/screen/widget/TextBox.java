package me.khajiitos.jackseconomy.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class TextBox extends AbstractWidget {
    private String text;
    private final int backgroundColor;

    public TextBox(int pX, int pY, int pWidth, int pHeight, String text, int backgroundColor) {
        super(pX, pY, pWidth, pHeight, Component.empty());
        this.text = text;
        this.backgroundColor = backgroundColor;
    }

    public TextBox(int pX, int pY, int pWidth, int pHeight, String text) {
        this(pX, pY, pWidth, pHeight, text, 0xFF888888);
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF000000);
        guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, this.backgroundColor);
        guiGraphics.drawString(Minecraft.getInstance().font, this.text, this.getX() + 3, (int) (this.getY() + (this.height - 9) / 2.f), 0xFFFFFFFF, false);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
