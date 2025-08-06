package me.khajiitos.jackseconomy.screen.widget;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BalanceProgressWidget extends AbstractWidget {
    protected static final ResourceLocation BALANCE_PROGRESS = ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "textures/gui/balance_progress.png");

    private final Supplier<BigDecimal> balanceSupplier;
    private final Supplier<BigDecimal> maxBalanceSupplier;
    private final Consumer<List<Component>> onTooltip;

    public BalanceProgressWidget(int pX, int pY, Supplier<BigDecimal> balanceSupplier, Supplier<BigDecimal> maxBalanceSupplier, Consumer<List<Component>> onTooltip) {
        super(pX, pY, 5, 65, Component.literal(""));
        this.balanceSupplier = balanceSupplier;
        this.maxBalanceSupplier = maxBalanceSupplier;
        this.onTooltip = onTooltip;
    }

    public boolean isHovered() {
        return this.isHovered;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        BigDecimal balance = balanceSupplier.get();
        BigDecimal capacity = maxBalanceSupplier.get();
        double progress = balance.divide(capacity, RoundingMode.DOWN).min(BigDecimal.ONE).doubleValue();

        int startX = this.getX();
        int startY = this.getY();
        int pixels = (int)(65 * progress);

        guiGraphics.blit(BALANCE_PROGRESS, startX, startY, 0/*this.getBlitOffset()*/, 56, 0, 5, 65, 256, 256);
        guiGraphics.blit(BALANCE_PROGRESS, startX, startY + (65 - pixels), 0/*this.getBlitOffset()*/, 51, 65 - pixels, 5, pixels, 256, 256);

        if (isHovered()) {
            onTooltip.accept(List.of(
                    Component.translatable("jackseconomy.balance", Component.literal(Config.oneItemCurrencyMode.get() ? "$" + balance.longValue() : CurrencyHelper.format(balance)).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.YELLOW),
                    Component.translatable("jackseconomy.max_storage", Component.literal(Config.oneItemCurrencyMode.get() ? "$" + capacity.longValue() :CurrencyHelper.format(capacity)).withStyle(ChatFormatting.GOLD), Component.literal((int)(progress * 100) + "%").withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.YELLOW)
            ));
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}
}
