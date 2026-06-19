package me.khajiitos.jackseconomy.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.glfw.GLFW;

public class FloatingEditBoxWidget extends EditBox {
    private final int minWidth;
    private final int midX;
    private final OnDone onDone;

    /**
     * @deprecated please use {@link #FloatingEditBoxWidget(Font, int, int, int, int, Type, OnDone)} instead.
     * Will be removed in 1.2.2-1.8.0
     */
    @Deprecated(since = "1.2.2-1.7.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "1.8.0")
    public FloatingEditBoxWidget(Font pFont, int midX, int pY, int minWidth, int height, boolean isDecimal, OnDone onDone) {
        this(pFont, midX, pY, minWidth, height, isDecimal ? Type.DECIMAL : Type.STRING, onDone);
    }

    public FloatingEditBoxWidget(Font pFont, int midX, int pY, int minWidth, int height, Type type, OnDone onDone) {
        super(pFont, midX, pY, minWidth, height, Component.empty());
        this.midX = midX;
        this.minWidth = minWidth;
        this.onDone = onDone;

        switch (type) {
            case INTEGER -> setFilter(newValue -> {
                for (int i = 0; i < newValue.length(); i++) {
                    char c = newValue.charAt(i);

                    if (c < '0' || c > '9') {
                        return false;
                    }
                }

                return true;
            });
            case DECIMAL -> setFilter(newValue -> {
                boolean hasDot = false;
                for (int i = 0; i < newValue.length(); i++) {
                    char c = newValue.charAt(i);

                    if (c == '.') {
                        if (hasDot) {
                            return false;
                        }
                        hasDot = true;
                    }

                    if (c != '.' && (c < '0' || c > '9')) {
                        return false;
                    }
                }

                return true;
            });
        }

        this.calculateWidthAndPos();
    }

    public FloatingEditBoxWidget(Font pFont, int midX, int pY, int minWidth, int height, OnDone onDone) {
        this(pFont, midX, pY, minWidth, height, Type.STRING, onDone);
    }

    public void calculateWidthAndPos() {
        Font font = Minecraft.getInstance().font;
        this.width = Math.max(this.minWidth, font.width(this.getValue()) + 16);
        this.setX(this.midX - this.width / 2);
    }

    @Override
    public void setValue(String pText) {
        super.setValue(pText);
        this.calculateWidthAndPos();
    }

    @Override
    public void insertText(String pTextToWrite) {
        super.insertText(pTextToWrite);
        this.calculateWidthAndPos();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == GLFW.GLFW_KEY_ENTER) {
            onDone.onDone(this.getValue());
        } else {
            super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
        return true;
    }

    @Override
    public void moveCursorTo(int pPos) {
        super.moveCursorTo(pPos);
        this.calculateWidthAndPos();
    }

    @FunctionalInterface
    public interface OnDone {
        void onDone(String value);
    }

    /**
     * Used to determine what characters can be entered.
     */
    public enum Type {
        /**
         * Allows for any character.
         */
        STRING,
        /**
         * Only allows for numbers.
         */
        INTEGER,
        /**
         * Only allows for numbers and a single decimal point.
         */
        DECIMAL
    }
}