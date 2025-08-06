package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.packet.AdminShopSchemaPacket;
import me.khajiitos.jackseconomy.screen.AdminShopScreen;
import me.khajiitos.jackseconomy.screen.BulkAdminShopScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class AdminShopSchemaHandler {
    public static void handle(AdminShopSchemaPacket msg, final IPayloadContext context) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof AdminShopScreen adminShopScreen) {
            adminShopScreen.onShopData(msg.data(), msg.adminShopName().orElse(null));
        } else if (screen instanceof BulkAdminShopScreen bulkAdminShopScreen) {
            bulkAdminShopScreen.onShopData(msg.data(), msg.adminShopName().orElse(null));
        }
    }
}
