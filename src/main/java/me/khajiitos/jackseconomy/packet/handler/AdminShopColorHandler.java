package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.JacksEconomyClient;
import me.khajiitos.jackseconomy.packet.AdminShopColorPacket;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class AdminShopColorHandler {

    public static void handle(AdminShopColorPacket msg, final IPayloadContext context) {
        JacksEconomyClient.adminShopColors.clear();
        JacksEconomyClient.defaultAdminShopColor = msg.defaultColor();
        JacksEconomyClient.synced = true;

        msg.colors().forEach(tag -> {
            if (tag instanceof CompoundTag colorTag && colorTag.contains("name") && colorTag.contains("color"))
                JacksEconomyClient.adminShopColors.put(colorTag.getString("name"), colorTag.getInt("color"));
        });
    }
}
