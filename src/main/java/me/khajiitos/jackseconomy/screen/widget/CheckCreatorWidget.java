package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CheckCreatorWidget extends AbstractWidget {
    private static final ResourceLocation IMAGE = new ResourceLocation(JacksEconomy.MOD_ID, "textures/item/check.png");
    private boolean open;
    private final Consumer<BigDecimal> onCreateCheck;
    private final Consumer<List<Component>> onTooltip;

    private TextBox keypadTextbox;

    private final List<AbstractWidget> renderables = new ArrayList<>();

    public CheckCreatorWidget(int pX, int pY, Consumer<BigDecimal> onCreateCheck, Consumer<List<Component>> onTooltip) {
        super(pX, pY, 16, 16, Component.empty());
        this.onTooltip = onTooltip;
        this.onCreateCheck = onCreateCheck;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.open) {
            this.width = 81;
            this.height = 140;
        } else {
            this.width = 16;
            this.height = 16;
        }

        int buttonStartX = this.getX() + this.width - 16;
        int buttonStartY = this.getY();
        boolean buttonHovered = pMouseX >= buttonStartX && pMouseX <= buttonStartX + 16 && pMouseY >= buttonStartY && pMouseY <= buttonStartY + 16;

        guiGraphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, 0xFF666666);
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF333333);

        if (buttonHovered) {
            RenderSystem.setShaderColor(0.75f, 0.75f, 0.75f, 1.f);
        }

        guiGraphics.blit(IMAGE, buttonStartX, buttonStartY, 0/*this.getBlitOffset()*/, 0, 0, 16, 16, 16, 16);

        RenderSystem.setShaderColor(1.f, 1.f, 1.f, 1.f);

        if (open) {
            guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("jackseconomy.write_check").withStyle(ChatFormatting.YELLOW), this.getX() + 3, this.getY() + 5, 0xFFFFFFFF);

            for (AbstractWidget widget : this.renderables) {
                widget.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
            }
        }
    }

    protected void addRenderables() {
        String keypadText = keypadTextbox == null ? "" : keypadTextbox.getText();
        keypadTextbox = new TextBox(this.getX() + 13, this.getY() + 20, 55, 15, keypadText);

        this.renderables.add(keypadTextbox);

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                this.renderables.add(new SimpleButton(this.getX() + 13 + x * 20, this.getY() + 40 + y * 20, 15, 15, Component.literal(String.valueOf(1 + y * 3 + x)), (b) -> {
                    if (this.keypadTextbox.getText().length() < 8) {
                        int dotIndex = this.keypadTextbox.getText().indexOf('.');
                        if (dotIndex == -1 || dotIndex > this.keypadTextbox.getText().length() - 3) {
                            keypadTextbox.setText(keypadTextbox.getText() + b.getMessage().getString());
                        }
                    }
                }));
            }
        }

        this.renderables.add(new SimpleButton(this.getX() + 13, this.getY() + 100, 15, 15, Component.literal("0"), (b) -> {
            if (this.keypadTextbox.getText().length() < 8) {
                keypadTextbox.setText(keypadTextbox.getText() + b.getMessage().getString());
            }
        }));

        this.renderables.add(new SimpleButton(this.getX() + 33, this.getY() + 100, 15, 15, Component.literal("."), (b) -> {
            if (!this.keypadTextbox.getText().isEmpty() && !this.keypadTextbox.getText().contains(".") &&  this.keypadTextbox.getText().length() < 8) {
                keypadTextbox.setText(keypadTextbox.getText() + b.getMessage().getString());
            }
        }));

        this.renderables.add(new SimpleButton(this.getX() + 53, this.getY() + 100, 15, 15, Component.literal("C"), (b) -> {
            if (!this.keypadTextbox.getText().isEmpty()) {
                this.keypadTextbox.setText(this.keypadTextbox.getText().substring(0, this.keypadTextbox.getText().length() - 1));
            }
        }));

        this.renderables.add(new SimpleButton(this.getX() + 13, this.getY() + 120, 55, 15, Component.translatable("jackseconomy.write"), (b) -> {
            BigDecimal value;
            try {
                value = new BigDecimal(this.keypadTextbox.getText());
            } catch (NumberFormatException e) {
                return;
            }

            onCreateCheck.accept(value);

            //if (value.compareTo(BigDecimal.ZERO) > 0 && value.compareTo(WalletItem.getBalance(itemStack)) <= 0) {
            ///    Packets.sendToServer(new CreateCheckPacket(value));
            //}
        }));

        /*
        this.addRenderableWidget(new SimpleImageButton(this.leftPos + 70, this.topPos + 90, 15, 15, CHECK_ICON, (b) -> {
            BigDecimal value;
            try {
                value = new BigDecimal(this.keypadTextbox.getText());
            } catch (NumberFormatException e) {
                return;
            }

            onCreateCheck.accept(value);

            if (value.compareTo(BigDecimal.ZERO) > 0 && value.compareTo(WalletItem.getBalance(itemStack)) <= 0) {
                Packets.sendToServer(new CreateCheckPacket(value));
            }
        }, ((pButton, guiGraphics, pMouseX, pMouseY) -> this.tooltip = List.of(Component.translatable("jackseconomy.turn_into_check").withStyle(ChatFormatting.GRAY)))));
*/
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton != 0 && pButton != 1) {
            return super.mouseClicked(pMouseX, pMouseY, pButton);
        }

        int buttonStartX = this.getX() + this.width - 16;
        int buttonStartY = this.getY();

        if (pMouseX >= buttonStartX && pMouseX <= buttonStartX + 16 && pMouseY >= buttonStartY && pMouseY <= buttonStartY + 16) {
            this.renderables.clear();
            this.open = !this.open;

            if (this.open) {
                this.setX(this.getX() - 65);
                this.addRenderables();
            } else {
                this.setX(this.getX() + 65);
            }

            return true;
        }

        for (AbstractWidget widget : renderables) {
            if (widget.isHovered()   && widget.mouseClicked(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }
}
