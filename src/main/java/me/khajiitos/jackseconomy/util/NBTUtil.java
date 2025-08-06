package me.khajiitos.jackseconomy.util;

import com.google.gson.*;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.nbt.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class NBTUtil {

	public static @NotNull JsonElement nbtToJson(@Nullable Tag tag) {
		if (tag instanceof StringTag stringTag) {
			return new JsonPrimitive(stringTag.getAsString());
		} else if (tag instanceof IntTag intTag) {
			return new JsonPrimitive(intTag.getAsInt());
		} else if (tag instanceof DoubleTag doubleTag) {
			return new JsonPrimitive(doubleTag.getAsDouble());
		} else if (tag instanceof ShortTag shortTag) {
			return new JsonPrimitive(shortTag.getAsShort());
		} else if (tag instanceof ByteTag byteTag) {
			return new JsonPrimitive(byteTag.getAsByte());
		} else if (tag instanceof CompoundTag compoundTag) {
			JsonObject resultObj = new JsonObject();

			compoundTag.getAllKeys().forEach(key -> {
				Tag objTag = compoundTag.get(key);

				if (objTag == null) {
					return;
				}

				resultObj.add(key, nbtToJson(objTag));
			});

			return resultObj;
		} else if (tag instanceof ListTag listTag) {
			JsonArray resultArray = new JsonArray();

			listTag.forEach(arrayTag -> {
				if (arrayTag == null) {
					return;
				}

				resultArray.add(nbtToJson(arrayTag));
			});

			return resultArray;
		} else if (tag instanceof IntArrayTag intArrayTag) {
			JsonArray jsonArray = new JsonArray();

			for (IntTag intTag : intArrayTag) {
				jsonArray.add(intTag.getAsInt());
			}

			return jsonArray;
		} else if (tag == null) {
			return JsonNull.INSTANCE;
		} else {
			JacksEconomy.LOGGER.warn("NBTUtil::nbtToJson - unsupported tag type " + tag.getClass().getSimpleName());
		}

		return JsonNull.INSTANCE;
	}

	public static @Nullable Tag jsonToNbt(JsonElement jsonElement) {
		if (jsonElement instanceof JsonPrimitive jsonPrimitive) {
			if (jsonPrimitive.isBoolean()) {
				return ByteTag.valueOf(jsonElement.getAsBoolean());
			} else if (jsonPrimitive.isNumber()) {
				// There is no way to differentiate what type of number that is - double is the most universal
				return DoubleTag.valueOf(jsonPrimitive.getAsDouble());
			} else if (jsonPrimitive.isString()) {
				return StringTag.valueOf(jsonPrimitive.getAsString());
			}
		} else if (jsonElement instanceof JsonObject jsonObject) {
			CompoundTag resultTag = new CompoundTag();
			jsonObject.entrySet().forEach(entry -> {
				if (entry.getValue() != null) {
					Tag tag = jsonToNbt(entry.getValue());
					if (tag != null) {
						resultTag.put(entry.getKey(), tag);
					}
				}
			});

			return resultTag;
		} else if (jsonElement instanceof JsonArray jsonArray) {
			ListTag listTag = new ListTag();
			jsonArray.forEach(element -> listTag.add(jsonToNbt(element)));
			return listTag;
		} else if (!(jsonElement instanceof JsonNull)) {
			JacksEconomy.LOGGER.warn("NBTUtil::jsonToNbt - unsupported element type " + jsonElement.getClass().getSimpleName());
		}

		return null;
	}
}
