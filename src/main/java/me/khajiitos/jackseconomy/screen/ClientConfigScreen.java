package me.khajiitos.jackseconomy.screen;

import me.khajiitos.jackseconomy.config.ClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfigScreen extends Screen {
    public final Screen parent;

    public ClientConfigScreen(Screen parent) {
        super(Component.literal("Jack's Economy Config"));
        this.parent = parent;
    }

    public Component getBooleanComponent(boolean bool) {
        return bool ? Component.translatable("jackseconomy.true").withStyle(ChatFormatting.GREEN) : Component.translatable("jackseconomy.false").withStyle(ChatFormatting.RED);
    }

    public Component getBooleanButtonComponent(String translationName, boolean value) {
        return Component.translatable(translationName, getBooleanComponent(value));
    }

    private Button configBooleanButton(int y, ModConfigSpec.ConfigValue<Boolean> configValue, String translationName) {
        return Button.builder(getBooleanButtonComponent(translationName, configValue.get()), button -> {
            configValue.set(!configValue.get());
            button.setMessage(getBooleanButtonComponent(translationName, configValue.get()));
        }).bounds(this.width / 2 - 100, y, 200, 20).build();
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(configBooleanButton(20, ClientConfig.hidePriceTooltips, "jackseconomy.config.hide_price_tooltips"));
        this.addRenderableWidget(configBooleanButton(50, ClientConfig.alternativeTooltipFormat, "jackseconomy.config.alternative_tooltip_format"));
        this.addRenderableWidget(configBooleanButton(80, ClientConfig.walletHudPositionRight, "jackseconomy.config.wallet_hud_position_right"));


        this.addRenderableWidget(new AbstractSliderButton(this.width / 2 - 100, 110, 200, 20, Component.translatable("jackseconomy.config.wallet_hud_position_y_offset", Component.literal(String.valueOf(ClientConfig.walletHudPositionYOffset.get()))), (double) ClientConfig.walletHudPositionYOffset.get() / height) {

            @Override
            protected void updateMessage() {
                this.setMessage(Component.translatable("jackseconomy.config.wallet_hud_position_y_offset", Component.literal(String.valueOf((int)(this.value * ClientConfigScreen.this.height)))));
            }

            @Override
            protected void applyValue() {
                ClientConfig.walletHudPositionYOffset.set((int)(this.value * ClientConfigScreen.this.height));
            }
        });
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(guiGraphics, pMouseX, pMouseY, pPartialTick);
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
    }
}
