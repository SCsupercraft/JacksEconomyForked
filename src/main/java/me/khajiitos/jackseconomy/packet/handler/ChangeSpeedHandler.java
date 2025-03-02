package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.blockentity.ExporterBlockEntity;
import me.khajiitos.jackseconomy.blockentity.FluidExporterBlockEntity;
import me.khajiitos.jackseconomy.blockentity.FluidImporterBlockEntity;
import me.khajiitos.jackseconomy.blockentity.ImporterBlockEntity;
import me.khajiitos.jackseconomy.menu.ExporterMenu;
import me.khajiitos.jackseconomy.menu.FluidExporterMenu;
import me.khajiitos.jackseconomy.menu.FluidImporterMenu;
import me.khajiitos.jackseconomy.menu.ImporterMenu;
import me.khajiitos.jackseconomy.packet.ChangeSpeedPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChangeSpeedHandler {
    public static void handle(ChangeSpeedPacket msg, Supplier<NetworkEvent.Context> ctx) {
        if (msg.speed() < 0.f || msg.speed() > 1.0f) {
            return;
        }

        ServerPlayer sender = ctx.get().getSender();

        if (sender == null) {
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