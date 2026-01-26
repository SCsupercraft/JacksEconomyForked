package me.khajiitos.jackseconomy.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AdminShopExitPromptScreen extends Screen {
    private final Screen parent;

    protected AdminShopExitPromptScreen(Screen parent) {
        super(Component.empty());
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int midX = this.width / 2;
        int midY = this.height / 2;

        this.addRenderableWidget(Button.builder(Component.translatable("jackseconomy.leave").withStyle(ChatFormatting.RED), button -> {
            this.onClose();
        }).bounds(midX - 80, midY + 20, 75, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("jackseconomy.stay"), button -> {
            assert this.minecraft != null;
            this.minecraft.screen = parent;
            this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
        }).bounds(midX + 5, midY + 20, 75, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);

        guiGraphics.drawCenteredString(this.font, Component.translatable("jackseconomy.admin_shop_exit_prompt").withStyle(ChatFormatting.YELLOW), this.width / 2, this.height / 2 - 5, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        super.onClose();

        // It always will be AdminShopScreen tbh
        if (this.parent instanceof AdminShopScreen adminShopScreen) {
            if (this.minecraft != null && this.minecraft.player != null)
                this.minecraft.player.closeContainer();
            adminShopScreen.sendShopUnlocksAcknowledgements();
        }
    }
}
