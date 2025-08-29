package me.khajiitos.jackseconomy.data;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.packet.AdminShopColorPacket;
import me.khajiitos.jackseconomy.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;

public class AdminShopColorManager {
	public static final HashMap<String, Integer> adminShopColors = new HashMap<>();
	public static Integer defaultAdminShopColor = -1;
	private static final DataHandler DATA_HANDLER = new DataHandler.NBTDataHandler(
			new File("config/jackseconomy_adminshop_colors.dat")
	);

	public static void load() {
		resetData();
		if (DATA_HANDLER.DATA_FILE.exists()) {
			CompoundTag data = DATA_HANDLER.loadAsNbt();

			if (data.contains("colors")) {
				ListTag listTag = data.getList("colors", Tag.TAG_COMPOUND);
				listTag.forEach(tag -> {
					if (tag instanceof CompoundTag colorTag && colorTag.contains("name") && colorTag.contains("color"))
						adminShopColors.put(colorTag.getString("name"), colorTag.getInt("color"));
				});
			}
			defaultAdminShopColor = data.contains("default") ? data.getInt("default") : -1;
		} else save();
	}

	public static void save() {
		CompoundTag tag = new CompoundTag();
		tag.putInt("default", defaultAdminShopColor);
		tag.put("colors", toAdminShopColorsList());
		DATA_HANDLER.save(tag);
	}

	public static void resetData() {
		adminShopColors.clear();
		defaultAdminShopColor = -1;
		updateAll();
	}

	private static ListTag toAdminShopColorsList() {
		ListTag listTag = new ListTag();
		adminShopColors.forEach((name, color) -> {
			CompoundTag tag = new CompoundTag();
			tag.putString("name", name);
			tag.putInt("color", color);
			listTag.add(tag);
		});
		return listTag;
	}
	public static AdminShopColorPacket toUpdatePacket() {
		return new AdminShopColorPacket(toAdminShopColorsList(), defaultAdminShopColor);
	}

	public static void updatePlayer(ServerPlayer player) {
		Packets.sendToClient(player, toUpdatePacket());
	}
	public static void updateAll() {
		JacksEconomy.server.getPlayerList().getPlayers().forEach(AdminShopColorManager::updatePlayer);
	}

	public static void setColor(@Nullable String name, int color) {
		if (name != null) adminShopColors.put(name, color);
		else defaultAdminShopColor = color;

		updateAll();
		save();
	}
	public static void setColorFromHex(@Nullable String name, String color) {
		setColor(name, Utils.hexToMinecraftColor(color));
	}
	public static void resetColor(@Nullable String name) {
		if (name != null) adminShopColors.remove(name);
		else defaultAdminShopColor = -1;

		updateAll();
		save();
	}

	public static int getColor(@Nullable String name) {
		return name != null && adminShopColors.containsKey(name) ? adminShopColors.get(name) : defaultAdminShopColor;
	}
}
