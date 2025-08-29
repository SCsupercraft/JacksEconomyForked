package me.khajiitos.jackseconomy.data;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.data.price.FluidDescription;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PurchaseManager {
	private static DataHandler dataHandler;
	private static final List<Purchase> purchases = new ArrayList<>();

	public static void load() {
		dataHandler = new DataHandler.NBTDataHandler(
				JacksEconomy.server.getWorldPath(LevelResource.ROOT)
						.resolve("data/jackseconomy/purchases.dat")
						.toFile()
		);
		resetData();
		if (dataHandler.DATA_FILE.exists()) {
			CompoundTag data = dataHandler.loadAsNbt();
			ListTag listTag = data.getList("purchases", Tag.TAG_COMPOUND);

			listTag.forEach(tag ->
					Purchase.CODEC.decode(NbtOps.INSTANCE, tag)
							.resultOrPartial(error -> JacksEconomy.LOGGER.warn("Failed to decode purchase: {}", error))
							.map(Pair::getFirst)
							.ifPresent(purchase -> {
								try {
									purchase.assertValid();
									purchases.add(purchase);
								} catch (IllegalStateException e) {
									JacksEconomy.LOGGER.error("Failed to validate purchase: {}", purchase, e);
								}
							})
			);
		} else save();
	}

	public static void save() {
		CompoundTag data = new CompoundTag();
		ListTag listTag = new ListTag();

		purchases.forEach(purchase -> listTag.add(Purchase.CODEC.encodeStart(NbtOps.INSTANCE, purchase).getOrThrow(false, (unused) -> {})));
		data.put("purchases", listTag);
		dataHandler.save(data);
	}

	public static void resetData() {
		purchases.clear();
	}

	public static class Purchases {
		private final UUID buyer;
		private final PurchaseSource source;
		private final List<Purchase> purchases = new ArrayList<>();
		private final long timestamp = JacksEconomy.server.overworld().getGameTime();

		public Purchases(PurchaseSource source) {
			this(null, source);
		}

		public Purchases(@Nullable UUID buyer, PurchaseSource source) {
			this.buyer = buyer;
			this.source = source;
		}

		public void addPurchase(@NotNull ItemDescription description, int quantity) {
			addPurchase(Purchase.of(description, buyer, quantity, timestamp, source));
		}
		public void addPurchase(@NotNull FluidDescription description, int quantity) {
			addPurchase(Purchase.of(description, buyer, quantity, timestamp, source));
		}

		private void addPurchase(@NotNull Purchase purchase) {
			purchase.assertValid();
			purchases.add(purchase);
		}

		public void processPurchases() {
			HashMap<Either<ItemDescription, FluidDescription>, Purchase> mergedPurchases = new HashMap<>();

			this.purchases.forEach(purchase -> {
				if (mergedPurchases.containsKey(purchase.description)) {
					Purchase current = mergedPurchases.get(purchase.description);

					mergedPurchases.put(purchase.description, new Purchase(
							purchase.description, purchase.buyer, purchase.quantity + current.quantity, timestamp, source
					));
				} else mergedPurchases.put(purchase.description, purchase);
			});

			PurchaseManager.purchases.addAll(mergedPurchases.values());
		}
	}

	record Purchase(Either<ItemDescription, FluidDescription> description, Optional<UUID> buyer, int quantity, long timestamp, PurchaseSource source) {
		public static final Codec<Purchase> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.either(ItemDescription.CODEC, FluidDescription.CODEC)
						.fieldOf("description")
						.forGetter(Purchase::description),
				UUIDUtil.CODEC.optionalFieldOf("buyer")
						.forGetter(Purchase::buyer),
				Codec.INT.fieldOf("quantity")
						.forGetter(Purchase::quantity),
				Codec.LONG.fieldOf("timestamp")
						.forGetter(Purchase::timestamp),
				PurchaseSource.CODEC.fieldOf("source")
						.forGetter(Purchase::source)
		).apply(instance, Purchase::new));

		public static Purchase of(@NotNull ItemDescription description, int quantity, long timestamp, @NotNull PurchaseSource source) {
			return of(description, null, quantity, timestamp, source);
		}
		public static Purchase of(@NotNull ItemDescription description, @Nullable UUID buyer, int quantity, long timestamp, @NotNull PurchaseSource source) {
			return new Purchase(Either.left(description), Optional.ofNullable(buyer), quantity, timestamp, source);
		}

		public static Purchase of(@NotNull FluidDescription description, int quantity, long timestamp, @NotNull PurchaseSource source) {
			return of(description, null, quantity, timestamp, source);
		}
		public static Purchase of(@NotNull FluidDescription description, @Nullable UUID buyer, int quantity, long timestamp, @NotNull PurchaseSource source) {
			return new Purchase(Either.right(description), Optional.ofNullable(buyer), quantity, timestamp, source);
		}

		public void assertValid() {
			source.assertValid(this);
		}

		public boolean isValid() {
			return source.isValid(this);
		}
	}
	public enum PurchaseSource {
		ADMIN_SHOP(true, Type.ITEM),
		IMPORTER(false, Type.ITEM),
		EXPORTER(false, Type.ITEM),
		FLUID_IMPORTER(false, Type.FLUID),
		FLUID_EXPORTER(false, Type.FLUID);

		public static final Codec<PurchaseSource> CODEC = Codec.STRING.xmap(PurchaseSource::valueOf, PurchaseSource::name);

		public final boolean requiresBuyer;
		public final Type type;

		PurchaseSource(boolean requiresBuyer, Type type) {
			this.requiresBuyer = requiresBuyer;
			this.type = type;
		}

		void assertValid(Purchase purchase) {
			if (purchase.description.left().isPresent() && type != Type.ITEM) throw new IllegalStateException(String.format("Purchase of type %s needs a fluid, not an item!", this.name()));
			if (purchase.description.right().isPresent() && type != Type.FLUID) throw new IllegalStateException(String.format("Purchase of type %s needs an item, not a fluid!", this.name()));
			if (purchase.buyer.isEmpty() && requiresBuyer) throw new IllegalStateException(String.format("Purchase of type %s requires a buyer!", this.name()));
		}

		boolean isValid(Purchase purchase) {
			if (purchase.description.left().isPresent() && type != Type.ITEM) return false;
			if (purchase.description.right().isPresent() && type != Type.FLUID) return false;
			return purchase.buyer.isPresent() || !requiresBuyer;
		}

		public enum Type {
			ITEM,
			FLUID
		}
	}
}