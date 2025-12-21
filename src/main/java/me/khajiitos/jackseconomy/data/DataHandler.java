package me.khajiitos.jackseconomy.data;

import com.google.gson.*;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.util.NBTUtil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import javax.annotation.Nullable;
import java.io.*;

public final class DataHandler {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final File nbtFile;
    @Nullable
    private final File legacyJsonFile;

    public DataHandler(final File nbtFile) {
        this(nbtFile, null);
    }

    public DataHandler(final File nbtFile, @Nullable final File legacyJsonFile) {
        this.nbtFile = nbtFile;
        this.legacyJsonFile = legacyJsonFile;
    }

    public boolean fileExists() {
        return nbtFile.exists() || (legacyJsonFile != null && legacyJsonFile.exists());
    }

    public boolean save(CompoundTag compoundTag) {
        if (!nbtFile.getParentFile().isDirectory() && !nbtFile.getParentFile().mkdirs()) { return false; }

        try {
            NbtIo.write(compoundTag, nbtFile.toPath());
        } catch (IOException e) {
            JacksEconomy.LOGGER.error("Failed to save data", e);
            return false;
        }
        return true;
    }

    public @Nullable CompoundTag load() {
        if (nbtFile.exists()) {
            try {
                return NbtIo.read(nbtFile.toPath());
            } catch (IOException e) {
                JacksEconomy.LOGGER.error("Failed to load data", e);
            }
        } else if (legacyJsonFile != null) {
            if (legacyJsonFile.exists()) {
                JacksEconomy.LOGGER.warn("Attempting to load data from legacy json file! Converting '{}' --> '{}'", legacyJsonFile, nbtFile);
                CompoundTag tag = null;
                try (FileReader fileReader = new FileReader(legacyJsonFile)) {
                    tag = (CompoundTag) NBTUtil.jsonToNbt(GSON.fromJson(fileReader, JsonObject.class));
                } catch (JsonSyntaxException | ClassCastException | IOException e) {
                    JacksEconomy.LOGGER.error("Failed to load data", e);
                }
                if (tag == null) return null;

                // Save data to new file. If successfully saved, delete legacy file.
                if (save(tag)) {
                    JacksEconomy.LOGGER.info("Successfully converted to nbt data! Deleting legacy json file");
                    if (!legacyJsonFile.delete()) {
                        JacksEconomy.LOGGER.warn("Failed to delete legacy json file: {}", legacyJsonFile);
                    }
                }
                return tag;
            } else {
                JacksEconomy.LOGGER.error("Failed to load data: missing file '{}' or '{}'", nbtFile, legacyJsonFile);
            }
        } else {
            JacksEconomy.LOGGER.error("Failed to load data: missing file '{}'", nbtFile);
        }
        return null;
    }
}
