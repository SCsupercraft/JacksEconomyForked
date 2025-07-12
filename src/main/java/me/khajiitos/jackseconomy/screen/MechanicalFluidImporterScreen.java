package me.khajiitos.jackseconomy.screen;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.blockentity.MechanicalFluidImporterBlockEntity;
import me.khajiitos.jackseconomy.menu.MechanicalFluidImporterMenu;
import me.khajiitos.jackseconomy.screen.widget.FluidStorageWidget;
import me.khajiitos.jackseconomy.screen.widget.SpeedStatusWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class MechanicalFluidImporterScreen extends AbstractFluidImporterScreen<MechanicalFluidImporterBlockEntity, MechanicalFluidImporterMenu> {
    private FluidStorageWidget fluidStorage;
    public MechanicalFluidImporterScreen(MechanicalFluidImporterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, Component.empty());
        this.imageHeight = 177;
        this.inventoryLabelY = this.imageHeight - 96;
    }

    @Override
    protected void init() {
        super.init();

        MechanicalFluidImporterBlockEntity blockEntity = this.getBlockEntity();

        if (blockEntity != null) {
            this.addRenderableWidget(new SpeedStatusWidget(this.width / 2 + 50, this.height / 2 - 79, blockEntity::getSpeed, blockEntity::getProgressPerTick, tooltip -> this.tooltip = tooltip));

            FluidStorageWidget fluidStorageWidget = new FluidStorageWidget(this.leftPos + 80, this.height / 2 - 79, blockEntity.getFluidStorage(), this.minecraft);
            fluidStorageWidget.setWidth(fluidStorageWidget.getWidth() * 2);
            fluidStorageWidget.BACKGROUND = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/fluid_bar_2.png");
            this.fluidStorage = this.addRenderableWidget(fluidStorageWidget);
        }
    }

    @Override
    protected void renderTooltipsOrSomething(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        MechanicalFluidImporterBlockEntity blockEntity = this.getBlockEntity();
        if (blockEntity != null) {
            boolean hoveredTicketPreview = this.ticketPreview != null && mouseX >= this.ticketPreview.getX() && mouseX <= this.ticketPreview.getX() + this.ticketPreview.getWidth() && mouseY >= this.ticketPreview.getY() && mouseY <= this.ticketPreview.getY() + this.ticketPreview.getHeight();
            if (!hoveredTicketPreview && this.fluidStorage.isHovered()) {
                this.fluidStorage.appendTooltip(guiGraphics, mouseX, mouseY);
            }
        }
    }

    private MechanicalFluidImporterBlockEntity getBlockEntity() {
        if (this.menu.getBlockEntity() instanceof MechanicalFluidImporterBlockEntity blockEntity) {
            return blockEntity;
        }
        return null;
    }

    @Override
    protected Set<Direction> getAllowedDirections() {
        return Set.of(Direction.DOWN, Direction.WEST, Direction.EAST, Direction.UP);
    }
}
