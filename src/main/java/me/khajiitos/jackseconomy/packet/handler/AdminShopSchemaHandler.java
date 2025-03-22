package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.packet.AdminShopSchemaPacket;
import me.khajiitos.jackseconomy.screen.AdminShopScreen;
import me.khajiitos.jackseconomy.screen.BulkAdminShopScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AdminShopSchemaHandler {
    public static void handle(AdminShopSchemaPacket msg, Supplier<NetworkEvent.Context> ctx) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof AdminShopScreen adminShopScreen) {
            adminShopScreen.onShopData(msg.data(), msg.adminShopName());
        } else if (screen instanceof BulkAdminShopScreen bulkAdminShopScreen) {
            bulkAdminShopScreen.onShopData(msg.data(), msg.adminShopName());
        }
    }
}
