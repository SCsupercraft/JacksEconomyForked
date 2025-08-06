package me.khajiitos.jackseconomy.screen;

import me.khajiitos.jackseconomy.blockentity.MechanicalImporterBlockEntity;
import me.khajiitos.jackseconomy.menu.MechanicalImporterMenu;
import me.khajiitos.jackseconomy.screen.widget.SpeedStatusWidget;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class MechanicalImporterScreen extends AbstractImporterScreen<MechanicalImporterBlockEntity, MechanicalImporterMenu> {
    public MechanicalImporterScreen(MechanicalImporterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, Component.empty());
        this.imageHeight = 177;
        this.inventoryLabelY = this.imageHeight - 96;
    }

    @Override
    protected void init() {
        super.init();

        MechanicalImporterBlockEntity blockEntity = this.getBlockEntity();

        if (blockEntity != null) {
            this.addRenderableWidget(new SpeedStatusWidget(this.width / 2 + 50, this.height / 2 - 79, blockEntity::getSpeed, blockEntity::getProgressPerTick, tooltip -> this.tooltip = tooltip));
        }
    }

    private MechanicalImporterBlockEntity getBlockEntity() {
        if (this.menu.getBlockEntity() instanceof MechanicalImporterBlockEntity blockEntity) {
            return blockEntity;
        }
        return null;
    }

    @Override
    protected Set<Direction> getAllowedDirections() {
        return Set.of(Direction.DOWN, Direction.WEST, Direction.EAST, Direction.UP);
    }
}
