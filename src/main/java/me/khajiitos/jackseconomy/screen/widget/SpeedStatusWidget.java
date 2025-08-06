package me.khajiitos.jackseconomy.screen.widget;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.create.CreateHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SpeedStatusWidget extends AbstractWidget {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "textures/gui/mechanical_speed_bar.png");
    private final Supplier<Float> speedSupplier;
    private final Consumer<List<Component>> onTooltip;
    private final Supplier<Double> progressPerTickSupplier;

    public SpeedStatusWidget(int pX, int pY, Supplier<Float> speedSupplier, Supplier<Double> progressPerTickSupplier, Consumer<List<Component>> onTooltip) {
        super(pX, pY, 32, 65, Component.literal(""));
        this.speedSupplier = speedSupplier;
        this.onTooltip = onTooltip;
        this.progressPerTickSupplier = progressPerTickSupplier;
    }

    public boolean isHovered() {
        return this.isHovered;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        guiGraphics.blit(BACKGROUND, this.getX(), this.getY(), 0, 0, this.width, this.height);

        CreateHelper.refresh();

        double speed = Math.abs(speedSupplier.get());
        double maxSpeed = CreateHelper.maxRotationSpeed;

        double progress = Math.min(speed / maxSpeed, 1.0);

        int progressHeight = (int)(progress * this.height);
        guiGraphics.blit(BACKGROUND, this.getX(), this.getY() + this.height - progressHeight, 32, this.height - progressHeight, 32, progressHeight, 256, 256);

        if (isHovered()) {
            onTooltip.accept(List.of(
                    Component.translatable("jackseconomy.speed", String.format("%.1f", speed)).withStyle(ChatFormatting.GRAY),
                    Component.translatable("jackseconomy.progress_per_tick", String.format("%.2f%%", progressPerTickSupplier.get() * 100.0)).withStyle(ChatFormatting.GRAY)
            ));
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}
}
