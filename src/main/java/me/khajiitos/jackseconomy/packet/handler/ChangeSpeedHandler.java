package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.blockentity.*;
import me.khajiitos.jackseconomy.menu.*;
import me.khajiitos.jackseconomy.packet.ChangeSpeedPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ChangeSpeedHandler {
    public static void handle(final ChangeSpeedPacket msg, final IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sender) || msg.speed() < 0.f || msg.speed() > 1.0f) {
            return;
        }

        if (sender.containerMenu instanceof ExporterMenu exporterMenu && exporterMenu.getBlockEntity() instanceof ExporterBlockEntity exporterBlockEntity) {
            exporterBlockEntity.setSpeed(msg.speed());
            exporterBlockEntity.markUpdated();
        } else if (sender.containerMenu instanceof ImporterMenu importerMenu && importerMenu.getBlockEntity() instanceof ImporterBlockEntity importerBlockEntity) {
            importerBlockEntity.setSpeed(msg.speed());
            importerBlockEntity.markUpdated();
        } else if (sender.containerMenu instanceof FluidExporterMenu fluidExporterMenu && fluidExporterMenu.getBlockEntity() instanceof FluidExporterBlockEntity fluidExporterBlockEntity) {
            fluidExporterBlockEntity.setSpeed(msg.speed());
            fluidExporterBlockEntity.markUpdated();
        } else if (sender.containerMenu instanceof FluidImporterMenu fluidImporterMenu && fluidImporterMenu.getBlockEntity() instanceof FluidImporterBlockEntity fluidImporterBlockEntity) {
            fluidImporterBlockEntity.setSpeed(msg.speed());
            fluidImporterBlockEntity.markUpdated();
        }
    }
}