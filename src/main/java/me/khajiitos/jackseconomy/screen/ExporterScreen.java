package me.khajiitos.jackseconomy.screen;

import me.khajiitos.jackseconomy.blockentity.ExporterBlockEntity;
import me.khajiitos.jackseconomy.menu.ExporterMenu;
import me.khajiitos.jackseconomy.packet.ChangeSpeedPacket;
import me.khajiitos.jackseconomy.screen.widget.EnergyStatusWidget;
import me.khajiitos.jackseconomy.screen.widget.SpeedVerticalSlider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

public class ExporterScreen extends AbstractExporterScreen<ExporterBlockEntity, ExporterMenu> {
    private SpeedVerticalSlider slider;
    private EnergyStatusWidget energyStatus;

    public ExporterScreen(ExporterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, Component.empty());
        this.imageHeight = 177;
        this.inventoryLabelY = this.imageHeight - 96;
    }

    private ExporterBlockEntity getBlockEntity() {
        if (this.menu.getBlockEntity() instanceof ExporterBlockEntity blockEntity) {
            return blockEntity;
        }
        return null;
    }

    @Override
    protected void init() {
        super.init();

        ExporterBlockEntity blockEntity = this.getBlockEntity();

        if (blockEntity != null) {
            this.slider = this.addRenderableWidget(new SpeedVerticalSlider(this.width / 2 + 70, this.height / 2 - 79, 12, 65, blockEntity.getSpeed(), newValue -> {
                blockEntity.setSpeed(newValue);
                PacketDistributor.sendToServer(new ChangeSpeedPacket(newValue));
            }));

            this.energyStatus = this.addRenderableWidget(new EnergyStatusWidget(this.width / 2 + 50, this.height / 2 - 79, blockEntity.getEnergyStorage()));
        }

    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        super.mouseMoved(pMouseX, pMouseY);
        this.slider.mouseMoved(pMouseX, pMouseY);
    }

    @Override
    protected void renderTooltipsOrSomething(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ExporterBlockEntity blockEntity = this.getBlockEntity();
        if (blockEntity != null) {
            if (this.energyStatus.isHovered()) {
                IEnergyStorage energyStorage = blockEntity.getEnergyStorage();
                guiGraphics.renderTooltip(Minecraft.getInstance().font, Component.literal(energyStorage.getEnergyStored() + "FE/" + energyStorage.getMaxEnergyStored() + "FE"), mouseX, mouseY);
            } else if (this.slider.isHovered()) {
                int fePerTick = blockEntity.getEnergyUsagePerTick();
                String progressPerTickPercent = String.format("%.2f%%", blockEntity.getProgressPerTick() * 100.0);
                guiGraphics.renderTooltip(Minecraft.getInstance().font, List.of(
                        Component.translatable("jackseconomy.fe_per_tick", fePerTick).withStyle(ChatFormatting.GRAY),
                        Component.translatable("jackseconomy.progress_per_tick", progressPerTickPercent).withStyle(ChatFormatting.GRAY)
                ), Optional.empty(), mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        boolean b = super.mouseClicked(pMouseX, pMouseY, pButton);

        if (pMouseX >= this.slider.getX() && pMouseX <= this.slider.getX() + this.slider.getWidth() && pMouseY >= this.slider.getY() && pMouseY <= this.slider.getY() + this.slider.getHeight()) {
            this.slider.dragging = true;
        }

        return b;
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 0) {
            this.slider.dragging = false;
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }
}