package me.khajiitos.jackseconomy.data;

import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.LevelResource;

public class StockMarketManager {
	private static DataHandler dataHandler;
	private static long lastUpdated;

	public static void load() {
		dataHandler = new DataHandler(
				JacksEconomy.server.getWorldPath(LevelResource.ROOT)
						.resolve("data/jackseconomy/stock-market.dat")
						.toFile()
		);
		resetData();
		if (dataHandler.fileExists()) {
			CompoundTag data = dataHandler.load();
			lastUpdated = data.contains("lastUpdated") ? data.getLong("lastUpdated") : 0;
		} else save();
	}

	public static void save() {
		CompoundTag data = new CompoundTag();
		data.putLong("lastUpdated", lastUpdated);

		dataHandler.save(data);
	}

	public static void resetData() {
		lastUpdated = 0;
	}

	public static void tick() {
		if (getDay() > lastUpdated) {
			lastUpdated = getDay();
			tryUpdate();
		}
	}

	private static long getDay() {
		return JacksEconomy.server.overworld().getDayTime() / 24000;
	}

	private static void tryUpdate() {
		JacksEconomy.LOGGER.info("Updating stock market...");
		try {
			update();
			JacksEconomy.LOGGER.info("Updated stock market!");
		} catch (Exception ignored) {
			JacksEconomy.LOGGER.error("Error updating stock market!");
		}
	}

	private static void update() {}
}
