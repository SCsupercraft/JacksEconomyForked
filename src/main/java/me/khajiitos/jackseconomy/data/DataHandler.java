package me.khajiitos.jackseconomy.data;

import com.google.gson.*;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.util.NBTUtil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import java.io.*;

public abstract class DataHandler {
	public final File DATA_FILE;
	DataHandler(final File file) {
		this.DATA_FILE = file;
	}
	public abstract void save(JsonObject object);
	public abstract void save(CompoundTag compoundTag);
	public abstract JsonObject loadAsJson();
	public abstract CompoundTag loadAsNbt();

	public static class NBTDataHandler extends DataHandler {

		public NBTDataHandler(File file) {
			super(file);
		}

		@Override
		public void save(JsonObject object) {
			JacksEconomy.LOGGER.info("Save: {}", DATA_FILE.getName());
			if (!DATA_FILE.getParentFile().isDirectory() && !DATA_FILE.getParentFile().mkdirs()) { return; }

			try {
				CompoundTag nbt = (CompoundTag) NBTUtil.jsonToNbt(object);
				if (nbt == null) { return; }

				NbtIo.write(nbt, DATA_FILE.toPath());
			} catch (IOException e) {
				JacksEconomy.LOGGER.error("Failed to save data", e);
			}
		}
		@Override
		public void save(CompoundTag compoundTag) {
			JacksEconomy.LOGGER.info("Save: {}", DATA_FILE.getName());
			if (!DATA_FILE.getParentFile().isDirectory() && !DATA_FILE.getParentFile().mkdirs()) { return; }

			try {
				NbtIo.write(compoundTag, DATA_FILE.toPath());
			} catch (IOException e) {
				JacksEconomy.LOGGER.error("Failed to save data", e);
			}
		}

		@Override
		public JsonObject loadAsJson() {
			JacksEconomy.LOGGER.info("Load: {}", DATA_FILE.getName());
			if (DATA_FILE.exists()) {
				try {
					return NBTUtil.nbtToJson(NbtIo.read(DATA_FILE.toPath())).getAsJsonObject();
				} catch (IOException e) {
					JacksEconomy.LOGGER.error("Failed to load data", e);
				}
			} else {
				JacksEconomy.LOGGER.error("Data file not found!");
			}
			return null;
		}
		@Override
		public CompoundTag loadAsNbt() {
			JacksEconomy.LOGGER.info("Load: {}", DATA_FILE.getName());
			if (DATA_FILE.exists()) {
				try {
					return NbtIo.read(DATA_FILE.toPath());
				} catch (IOException e) {
					JacksEconomy.LOGGER.error("Failed to load data", e);
				}
			} else {
				JacksEconomy.LOGGER.error("Data file not found!");
			}
			return null;
		}
	}
	public static class JSONDataHandler extends DataHandler {

		private final Gson GSON;
		public JSONDataHandler(File file) {
			super(file);

			GSON = new GsonBuilder().setPrettyPrinting().create();
		}
		public JSONDataHandler(File file, boolean usePrettyPrint) {
			super(file);

			if (usePrettyPrint) {
				GSON = new GsonBuilder().setPrettyPrinting().create();
			} else {
				GSON = new GsonBuilder().create();
			}
		}

		@Override
		public void save(JsonObject object) {
			JacksEconomy.LOGGER.info("Save: {}", DATA_FILE.getName());
			if (!DATA_FILE.getParentFile().isDirectory() && !DATA_FILE.getParentFile().mkdirs()) { return; }

			try (FileWriter fileWriter = new FileWriter(DATA_FILE)) {
				fileWriter.write(GSON.toJson(object));
			} catch (IOException e) {
				JacksEconomy.LOGGER.error("Failed to save data", e);
			}
		}
		@Override
		public void save(CompoundTag compoundTag) {
			JacksEconomy.LOGGER.info("Save: {}", DATA_FILE.getName());
			if (!DATA_FILE.getParentFile().isDirectory() && !DATA_FILE.getParentFile().mkdirs()) { return; }

			try (FileWriter fileWriter = new FileWriter(DATA_FILE)) {
				fileWriter.write(GSON.toJson(NBTUtil.nbtToJson(compoundTag)));
			} catch (IOException e) {
				JacksEconomy.LOGGER.error("Failed to save data", e);
			}
		}

		@Override
		public JsonObject loadAsJson() {
			JacksEconomy.LOGGER.info("Load: {}", DATA_FILE.getName());
			if (DATA_FILE.exists()) {
				try (FileReader fileReader = new FileReader(DATA_FILE)) {
					return GSON.fromJson(fileReader, JsonObject.class);
				} catch (JsonSyntaxException | ClassCastException | IOException e) {
					JacksEconomy.LOGGER.error("Failed to load data", e);
				}
			} else {
				JacksEconomy.LOGGER.error("Data file not found!");
			}
			return null;
		}
		@Override
		public CompoundTag loadAsNbt() {
			JacksEconomy.LOGGER.info("Load: {}", DATA_FILE.getName());
			if (DATA_FILE.exists()) {
				try (FileReader fileReader = new FileReader(DATA_FILE)) {
					return (CompoundTag) NBTUtil.jsonToNbt(GSON.fromJson(fileReader, JsonObject.class));
				} catch (JsonSyntaxException | ClassCastException | IOException e) {
					JacksEconomy.LOGGER.error("Failed to load data", e);
				}
			} else {
				JacksEconomy.LOGGER.error("Data file not found!");
			}
			return null;
		}
	}
}
