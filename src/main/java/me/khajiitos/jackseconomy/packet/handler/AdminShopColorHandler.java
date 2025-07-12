package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.JacksEconomyClient;
import me.khajiitos.jackseconomy.packet.AdminShopColorPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AdminShopColorHandler {

    public static void handle(AdminShopColorPacket msg, Supplier<NetworkEvent.Context> ctx) {
        JacksEconomyClient.adminShopColors.clear();
        JacksEconomyClient.defaultAdminShopColor = msg.defaultColor();

        msg.colors().forEach(tag -> {
            if (tag instanceof CompoundTag colorTag && colorTag.contains("name") && colorTag.contains("color"))
                JacksEconomyClient.adminShopColors.put(colorTag.getString("name"), colorTag.getInt("color"));
        });
    }
}
