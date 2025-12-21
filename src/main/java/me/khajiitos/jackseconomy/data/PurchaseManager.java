package me.khajiitos.jackseconomy.data;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.data.price.FluidDescription;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import me.khajiitos.jackseconomy.event.PurchaseEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PurchaseManager {
    private static DataHandler dataHandler;
    private static final List<Purchase> purchases = new ArrayList<>();

    public static void load() {
        dataHandler = new DataHandler(
                JacksEconomy.server.getWorldPath(LevelResource.ROOT)
                        .resolve("data/jackseconomy/purchases.dat")
                        .toFile()
        );
        resetData();
        if (dataHandler.fileExists()) {
            CompoundTag data = dataHandler.load();
            ListTag listTag = data.getList("purchases", Tag.TAG_COMPOUND);

            listTag.forEach(tag -> Purchase.CODEC.decode(NbtOps.INSTANCE, tag)
                    .resultOrPartial(error -> JacksEconomy.LOGGER.warn("Failed to decode purchase: {}", error))
                    .map(Pair::getFirst)
                    .filter(purchase -> !purchase.isEmpty())
                    .ifPresent(purchases::add));
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

    public static void addPurchase(Purchase purchase, ServerPlayer buyer) {
        if (purchase.isEmpty()) return;

        MinecraftForge.EVENT_BUS.post(new PurchaseEvent.Player(
                Collections.singletonList(purchase), buyer
        ));
        purchases.add(purchase);
    }

    public static void addPurchase(Purchase purchase, BlockPos pos, ServerLevel level) {
        if (purchase.isEmpty()) return;

        MinecraftForge.EVENT_BUS.post(new PurchaseEvent.Block(
                Collections.singletonList(purchase), pos, level
        ));
        purchases.add(purchase);
    }

    public static void addPurchase(Purchases purchases) {
        HashMap<Either<ItemDescription, FluidDescription>, Purchase> mergedPurchases = new HashMap<>();

        purchases.purchases.forEach(purchase -> {
            if (purchase.isEmpty()) return;
            if (mergedPurchases.containsKey(purchase.description))
                mergedPurchases.computeIfPresent(purchase.description,
                        (k, current) -> new Purchase(
                                purchase.description,
                                purchase.buyer,
                                purchase.quantity + current.quantity,
                                purchase.totalCost + current.totalCost,
                                purchases.timestamp,
                                purchases.source
                        ));
            else mergedPurchases.put(purchase.description, purchase);
        });

        PurchaseEvent event = purchases.buyer != null
                ? new PurchaseEvent.Player(List.copyOf(mergedPurchases.values()), purchases.buyer)
                : new PurchaseEvent.Block(List.copyOf(mergedPurchases.values()), purchases.pos, purchases.level);
        MinecraftForge.EVENT_BUS.post(event);

        PurchaseManager.purchases.addAll(mergedPurchases.values());
    }

    public static Purchases player(PurchaseSource source, ServerPlayer buyer) {
        return new Purchases(buyer, source);
    }

    public static Purchases block(PurchaseSource source, BlockPos pos, ServerLevel level) {
        return new Purchases(pos, level, source);
    }

    public static long timestamp() {
        return JacksEconomy.server.overworld().getGameTime();
    }

    public static class Purchases {
        private final ServerPlayer buyer;
        private final BlockPos pos;
        private final ServerLevel level;
        private final PurchaseSource source;
        private final List<Purchase> purchases = new ArrayList<>();
        private final long timestamp = PurchaseManager.timestamp();

        public Purchases(ServerPlayer buyer, PurchaseSource source) {
            this.buyer = buyer;
            this.pos = null;
            this.level = null;
            this.source = source;
        }

        public Purchases(BlockPos pos, ServerLevel level, PurchaseSource source) {
            this.buyer = null;
            this.pos = pos;
            this.level = level;
            this.source = source;
        }

        public void addPurchase(@NotNull ItemDescription description, int quantity, double totalCost) {
            addPurchase(Purchase.of(description, getBuyerUuid(), quantity, totalCost, timestamp, source));
        }

        public void addPurchase(@NotNull FluidDescription description, int quantity, double totalCost) {
            addPurchase(Purchase.of(description, getBuyerUuid(), quantity, totalCost, timestamp, source));
        }

        private void addPurchase(@NotNull Purchase purchase) {
            purchases.add(purchase);
        }

        private UUID getBuyerUuid() {
            return buyer == null ? null : buyer.getUUID();
        }

        /**
         * @deprecated please use {@link PurchaseManager#addPurchase(Purchases)} instead.
         * Will be removed in 1.2.2-1.7.0
         */
        @Deprecated(since = "1.2.2-1.6.2", forRemoval = true)
        public void processPurchases() {
            PurchaseManager.addPurchase(this);
        }
    }

    public record Purchase(Either<ItemDescription, FluidDescription> description, Optional<UUID> buyer, int quantity,
                           double totalCost, long timestamp, PurchaseSource source) {
        public static final Codec<Purchase> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.either(ItemDescription.CODEC, FluidDescription.CODEC).fieldOf("description")
                        .forGetter(Purchase::description),
                UUIDUtil.CODEC.optionalFieldOf("buyer")
                        .forGetter(Purchase::buyer),
                Codec.INT.fieldOf("quantity")
                        .forGetter(Purchase::quantity),
                Codec.DOUBLE.fieldOf("total_cost")
                        .forGetter(Purchase::totalCost),
                Codec.LONG.fieldOf("timestamp")
                        .forGetter(Purchase::timestamp),
                PurchaseSource.CODEC.fieldOf("source")
                        .forGetter(Purchase::source)
        ).apply(instance, Purchase::new));

        public Purchase {
            if (totalCost <= 0)
                throw new IllegalStateException("Purchases must have a valid unit cost! (greater than zero)");
            if (description.left().isPresent() && source.type != PurchaseSource.Type.ITEM)
                throw new IllegalStateException(String.format("Purchase of type %s needs a fluid, not an item!", this.source().name()));
            if (description.right().isPresent() && source.type != PurchaseSource.Type.FLUID)
                throw new IllegalStateException(String.format("Purchase of type %s needs an item, not a fluid!", this.source().name()));
            if (buyer.isEmpty() && source.requiresBuyer)
                throw new IllegalStateException(String.format("Purchase of type %s requires a buyer!", this.source().name()));
        }

        public static Purchase of(@NotNull ItemDescription description, int quantity, double totalCost, long timestamp, @NotNull PurchaseSource source) {
            return of(description, null, quantity, totalCost, timestamp, source);
        }

        public static Purchase of(@NotNull ItemDescription description, @Nullable UUID buyer, int quantity, double totalCost, long timestamp, @NotNull PurchaseSource source) {
            return new Purchase(Either.left(description), Optional.ofNullable(buyer), quantity, totalCost, timestamp, source);
        }

        public static Purchase of(@NotNull FluidDescription description, int quantity, double totalCost, long timestamp, @NotNull PurchaseSource source) {
            return of(description, null, quantity, totalCost, timestamp, source);
        }

        public static Purchase of(@NotNull FluidDescription description, @Nullable UUID buyer, int quantity, double totalCost, long timestamp, @NotNull PurchaseSource source) {
            return new Purchase(Either.right(description), Optional.ofNullable(buyer), quantity, totalCost, timestamp, source);
        }

        boolean isEmpty() {
            return quantity == 0 || description.map(i -> i.item() != Items.AIR, f -> f.fluid() != Fluids.EMPTY);
        }
    }

    public enum PurchaseSource {
        ADMIN_SHOP(true, Type.ITEM),
        IMPORTER(false, Type.ITEM),
        EXPORTER(false, Type.ITEM),
        FLUID_IMPORTER(false, Type.FLUID), FLUID_EXPORTER(false, Type.FLUID);

        public static final Codec<PurchaseSource> CODEC = Codec.STRING.xmap(PurchaseSource::valueOf, PurchaseSource::name);

        public final boolean requiresBuyer;
        public final Type type;

        PurchaseSource(boolean requiresBuyer, Type type) {
            this.requiresBuyer = requiresBuyer;
            this.type = type;
        }

        public enum Type {
            ITEM, FLUID
        }
    }
}