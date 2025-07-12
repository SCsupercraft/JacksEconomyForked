package me.khajiitos.jackseconomy.screen;

import me.khajiitos.jackseconomy.blockentity.MechanicalFluidExporterBlockEntity;
import me.khajiitos.jackseconomy.menu.MechanicalFluidExporterMenu;
import me.khajiitos.jackseconomy.screen.widget.FluidStorageWidget;
import me.khajiitos.jackseconomy.screen.widget.SpeedStatusWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class MechanicalFluidExporterScreen extends AbstractFluidExporterScreen<MechanicalFluidExporterBlockEntity, MechanicalFluidExporterMenu> {
    private FluidStorageWidget fluidStorage;
    public MechanicalFluidExporterScreen(MechanicalFluidExporterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    private MechanicalFluidExporterBlockEntity getBlockEntity() {
        if (this.menu.getBlockEntity() instanceof MechanicalFluidExporterBlockEntity blockEntity) {
            return blockEntity;
        }
        return null;
    }

    @Override
    protected void init() {
        super.init();
        MechanicalFluidExporterBlockEntity blockEntity = this.getBlockEntity();

        if (blockEntity != null) {
            this.addRenderableWidget(new SpeedStatusWidget(this.width / 2 + 50, this.height / 2 - 79, blockEntity::getSpeed, blockEntity::getProgressPerTick, tooltip -> this.tooltip = tooltip));
            this.fluidStorage = this.addRenderableWidget(new FluidStorageWidget(this.leftPos + 9, this.height / 2 - 79, blockEntity.getFluidStorage(), this.minecraft));
        }
    }

    @Override
    protected void renderTooltipsOrSomething(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        MechanicalFluidExporterBlockEntity blockEntity = this.getBlockEntity();
        if (blockEntity != null) {
            if (this.fluidStorage.isHovered()) {
                this.fluidStorage.appendTooltip(guiGraphics, mouseX, mouseY);
            }
        }
    }

    @Override
    protected Set<Direction> getAllowedDirections() {
        return Set.of(Direction.DOWN, Direction.WEST, Direction.EAST, Direction.UP);
    }
}
