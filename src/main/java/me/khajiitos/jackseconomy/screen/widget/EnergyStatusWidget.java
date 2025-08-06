package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnergyStatusWidget extends AbstractWidget {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "textures/gui/energy_bar.png");
    private final IEnergyStorage energyStorage;
    public EnergyStatusWidget(int pX, int pY, IEnergyStorage energyStorage) {
        super(pX, pY, 15, 65, Component.literal(""));
        this.energyStorage = energyStorage;
    }

    public boolean isHovered() {
        return this.isHovered;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.setShaderTexture(0, BACKGROUND);

        guiGraphics.blit(BACKGROUND, this.getX(), this.getY(), 0, 0, this.width, this.height);

        int energy = energyStorage.getEnergyStored();
        int maxEnergy = energyStorage.getMaxEnergyStored();
        
        int progressHeight = (int) (((float)energy / (float)maxEnergy) * this.height);
        guiGraphics.blit(BACKGROUND, this.getX(), this.getY() + this.height - progressHeight, 15, this.height - progressHeight, 15, progressHeight, 256, 256);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}
}
