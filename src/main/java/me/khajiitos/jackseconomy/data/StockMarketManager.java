package me.khajiitos.jackseconomy.data;

import com.google.gson.JsonObject;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.world.level.storage.LevelResource;

public class StockMarketManager {
	private static DataHandler dataHandler;
	private static JsonObject data;
	private static long lastUpdated;

	public static void load() {
		dataHandler = new DataHandler.NBTDataHandler(
				JacksEconomy.server.getWorldPath(LevelResource.ROOT)
						.resolve("data/jackseconomy/stock-market.dat")
						.toFile()
		);
		if (dataHandler.DATA_FILE.exists()) {
			data = dataHandler.loadAsJson();
			lastUpdated = data.has("lastUpdated") ? data.get("lastUpdated").getAsLong() : 0;
		} else {
			data = new JsonObject();
			data.addProperty("lastUpdated", 0);

			lastUpdated = 0;

			dataHandler.save(data);
		}
		JacksEconomy.server.addTickable(StockMarketManager::tick);
	}

	public static void save() {
		data.addProperty("lastUpdated", lastUpdated);

		dataHandler.save(data);
	}

	public static void tick() {
		if (JacksEconomy.server == null) return;

		if (getDay() > lastUpdated) {
			lastUpdated = getDay();
			update();
		}
	}

	private static long getDay() {
		return JacksEconomy.server.overworld().getDayTime() / 24000;
	}

	private static void update() {
		JacksEconomy.LOGGER.info("Updating stock market...");
		try {
			JacksEconomy.LOGGER.info("Updated stock market!");
		} catch (Exception ignored) {
			JacksEconomy.LOGGER.error("Error updating stock market!");
		}
	}
}
