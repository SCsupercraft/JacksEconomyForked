package me.khajiitos.jackseconomy.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PurchaseManager {
	private static DataHandler dataHandler;
	private static CompoundTag data;
	private static CompoundTag players;

	public static void load() {
		dataHandler = new DataHandler.NBTDataHandler(
				JacksEconomy.server.getWorldPath(LevelResource.ROOT)
						.resolve("data/jackseconomy/purchases.dat")
						.toFile()
		);
		if (dataHandler.DATA_FILE.exists()) {
			data = dataHandler.loadAsNbt();
			players = data.contains("players") ? data.getCompound("players") : new CompoundTag();
		} else {
			data = new CompoundTag();
			data.put("players", new CompoundTag());

			players = new CompoundTag();

			dataHandler.save(data);
		}
		JacksEconomy.server.addTickable(StockMarketManager::tick);
	}
	public static void save() {
		data.put("players", players);

		dataHandler.save(data);
	}
	public static void resetData() {
		players = new CompoundTag();
	}
	public static void processPurchases(ArrayList<Purchase> purchases, ServerPlayer player) {
		String buyerKey = player.getUUID().toString();
		Buyer buyer = players.contains(buyerKey) ? Buyer.fromNbt(players.getCompound(buyerKey)) : new Buyer(player.getUUID(), player.getName().getString(), new ArrayList<>());
		Map<ItemDescription, Long> mergedPurchases = new HashMap<>();

		for (Purchase purchase : buyer.purchases()) {
			mergedPurchases.merge(purchase.itemDescription(), purchase.count(), Long::sum);
		}
		for (Purchase purchase : purchases) {
			mergedPurchases.merge(purchase.itemDescription(), purchase.count(), Long::sum);
		}

		long time = JacksEconomy.server.overworld().getDayTime();
		ArrayList<Purchase> finalizedPurchases = new ArrayList<>();
		for (Map.Entry<ItemDescription, Long> entry : mergedPurchases.entrySet()) {
			Purchase mergedPurchase = new Purchase(entry.getKey(), entry.getValue(), time);
			finalizedPurchases.add(mergedPurchase);
		}
		players.put(buyer.uuid().toString(), new Buyer(buyer.uuid(), buyer.name(), finalizedPurchases).toNbt());
	}

	public record Buyer(UUID uuid, String name, ArrayList<Purchase> purchases) {
		public static Buyer fromJson(JsonElement element) {
			return fromJson(element.getAsJsonObject());
		}
		public static Buyer fromJson(JsonObject object) {
			UUID uuid = UUID.fromString(object.get("uuid").getAsString());
			String name = object.get("name").getAsString();
			ArrayList<Purchase> purchases = new ArrayList<>(object.get("purchases").getAsJsonArray().asList().stream().map(Purchase::fromJson).toList());
			return new Buyer(uuid, name, purchases);
		}
		public static Buyer fromNbt(Tag tag) {
			return fromNbt((CompoundTag) tag);
		}
		public static Buyer fromNbt(CompoundTag compoundTag) {
			UUID uuid = compoundTag.getUUID("uuid");
			String name = compoundTag.getString("name");
			ArrayList<Purchase> purchases = new ArrayList<>(compoundTag.getList("purchases", Tag.TAG_COMPOUND).stream().map(Purchase::fromNbt).toList());
			return new Buyer(uuid, name, purchases);
		}

		public JsonObject toJson() {
			JsonObject object = new JsonObject();
			object.addProperty("uuid", this.uuid().toString());
			object.addProperty("name", this.name());

			JsonArray purchases = new JsonArray();
			for (Purchase purchase : this.purchases()) {
				purchases.add(purchase.toJson());
			}
			object.add("purchases", purchases);

			return object;
		}
		public CompoundTag toNbt() {
			CompoundTag tag = new CompoundTag();

			tag.putUUID("uuid", this.uuid());
			tag.putString("name", this.name());

			ListTag purchases = new ListTag();
			for (Purchase purchase : this.purchases()) {
				purchases.add(purchase.toNbt());
			}
			tag.put("purchases", purchases);

			return tag;
		}
	}
	public record Purchase(ItemDescription itemDescription, long count, long time) {
		public static Purchase fromJson(JsonElement element) {
			return fromJson(element.getAsJsonObject());
		}
		public static Purchase fromJson(JsonObject object) {
			ItemDescription itemDescription = ItemDescription.fromJson(object.getAsJsonObject().get("description").getAsJsonObject());
			long count = object.getAsJsonObject().get("count").getAsLong();
			long time = object.getAsJsonObject().get("time").getAsLong();
			return new Purchase(itemDescription, count, time);
		}
		public static Purchase fromNbt(Tag tag) {
			return fromNbt((CompoundTag) tag);
		}
		public static Purchase fromNbt(CompoundTag tag) {
			ItemDescription itemDescription = ItemDescription.fromNbt(tag.getCompound("description"));
			long count = tag.getLong("count");
			long time = tag.getLong("time");
			return new Purchase(itemDescription, count, time);
		}

		public JsonObject toJson() {
			JsonObject object = new JsonObject();
			object.add("description", this.itemDescription().toJson());
			object.addProperty("count", this.count());
			object.addProperty("time", this.time());

			return object;
		}
		public CompoundTag toNbt() {
			CompoundTag tag = new CompoundTag();
			tag.put("description", this.itemDescription().toNbt());
			tag.putLong("count", this.count());
			tag.putLong("time", this.time());

			return tag;
		}
	}
}
