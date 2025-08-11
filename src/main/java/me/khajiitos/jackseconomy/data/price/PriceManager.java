package me.khajiitos.jackseconomy.data.price;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.data.DataHandler;
import me.khajiitos.jackseconomy.gamestages.GameStagesManager;
import me.khajiitos.jackseconomy.packet.AdminShopSchemaPacket;
import me.khajiitos.jackseconomy.packet.PricesInfoPacket;
import me.khajiitos.jackseconomy.util.NewShopUnlocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class PriceManager {
    private static final List<ItemPriceEntry> itemPriceInfos = new ArrayList<>();
    private static final List<FluidPriceEntry> fluidPriceInfos = new ArrayList<>();
    private static final LinkedHashMap<Category, List<Category>> categories = new LinkedHashMap<>();
    private static final DataHandler DATA_HANDLER = new DataHandler.JSONDataHandler(
            new File("config/jackseconomy_prices.json")
    );

    static {
        itemPriceInfos.add(new ItemPriceEntry(ItemDescription.ofItem(new ItemStack(Items.DIAMOND)), new PricesItemPriceInfo(50.0, 45.0,100.0, null, null)));
        itemPriceInfos.add(new ItemPriceEntry(ItemDescription.ofItem(new ItemStack(Items.DIAMOND)), new AdminShopItemPriceInfo(150.0, "General:Gems", 0, null, null, null)));
        fluidPriceInfos.add(new FluidPriceEntry(FluidDescription.ofFluid(new FluidStack(Fluids.LAVA, 1)), new PricesFluidPriceInfo(0.02, 0.05)));

        ArrayList<Category> categoriesInnerDefault = new ArrayList<>();
        categoriesInnerDefault.add(new Category("Gems", ItemDescription.ofItem(new ItemStack(Items.DIAMOND)), null));
        categories.put(new Category("General", ItemDescription.ofItem(new ItemStack(Items.DIRT)), null), categoriesInnerDefault);
    }

    @Deprecated
    public static ItemPriceInfo getInfo(ItemDescription itemDescription) {
        return itemPriceInfos.stream().filter(itemPriceEntry -> itemPriceEntry.itemDescription.equals(itemDescription)).findFirst().map(ItemPriceEntry::itemPriceInfo).orElse(null);
    }

    public static ItemPriceInfo getInfo(ItemStack itemStack) {
        return getInfo(ItemDescription.ofItem(itemStack));
    }

    public static PricesItemPriceInfo getPricesInfo(ItemDescription itemDescription) {
        return getPricesInfo(itemDescription, null);
    }

    public static PricesItemPriceInfo getPricesInfo(ItemDescription itemDescription, @Nullable String adminShopName) {
        return itemPriceInfos.stream().filter(itemPriceEntry -> itemPriceEntry.itemDescription.equals(itemDescription) && itemPriceEntry.itemPriceInfo instanceof PricesItemPriceInfo info && Objects.equals(info.adminShopName, adminShopName)).map(entry -> ((PricesItemPriceInfo)entry.itemPriceInfo)).findFirst().orElse(null);
    }

    @Deprecated
    public static FluidPriceInfo getInfo(FluidDescription fluidDescription) {
        return fluidPriceInfos.stream().filter(fluidPriceEntry -> fluidPriceEntry.fluidDescription.equals(fluidDescription)).findFirst().map(FluidPriceEntry::fluidPriceInfo).orElse(null);
    }

    public static FluidPriceInfo getInfo(FluidStack fluidStack) {
        return getInfo(FluidDescription.ofFluid(fluidStack));
    }

    public static PricesFluidPriceInfo getPricesInfo(FluidDescription fluidDescription) {
        return fluidPriceInfos.stream().filter(fluidPriceEntry -> fluidPriceEntry.fluidDescription.equals(fluidDescription) && fluidPriceEntry.fluidPriceInfo instanceof PricesFluidPriceInfo).map(entry -> ((PricesFluidPriceInfo)entry.fluidPriceInfo)).findFirst().orElse(null);
    }

    public static List<ItemPriceEntry> getItemPriceInfos() {
        return itemPriceInfos;
    }

    public static List<FluidPriceEntry> getFluidPriceInfos() {
        return fluidPriceInfos;
    }

    public static LinkedHashMap<Category, List<Category>> getCategories() {
        return categories;
    }

    public static double getExporterSellPrice(ItemDescription itemDescription, int count) {
        return itemPriceInfos.stream().filter(itemPriceEntry -> itemPriceEntry.itemDescription.equals(itemDescription) && itemPriceEntry.itemPriceInfo instanceof PricesItemPriceInfo info && info.adminShopName == null).map(entry -> ((PricesItemPriceInfo)entry.itemPriceInfo).sellPrice * count).findFirst().orElse(-1.0);
    }

    public static double getImporterBuyPrice(ItemDescription itemDescription, int count) {
        return itemPriceInfos.stream().filter(itemPriceEntry -> itemPriceEntry.itemDescription.equals(itemDescription) && itemPriceEntry.itemPriceInfo instanceof PricesItemPriceInfo info && info.adminShopName == null).map(entry -> ((PricesItemPriceInfo)entry.itemPriceInfo).importerBuyPrice * count).findFirst().orElse(-1.0);
    }

    public static double getFluidExporterSellPrice(FluidDescription fluidDescription, int count) {
        return fluidPriceInfos.stream().filter(fluidPriceEntry -> fluidPriceEntry.fluidDescription.equals(fluidDescription) && fluidPriceEntry.fluidPriceInfo instanceof PricesFluidPriceInfo).map(entry -> ((PricesFluidPriceInfo)entry.fluidPriceInfo).sellPrice * count).findFirst().orElse(-1.0);
    }

    public static double getFluidImporterBuyPrice(FluidDescription fluidDescription, int count) {
        return fluidPriceInfos.stream().filter(fluidPriceEntry -> fluidPriceEntry.fluidDescription.equals(fluidDescription) && fluidPriceEntry.fluidPriceInfo instanceof PricesFluidPriceInfo).map(entry -> ((PricesFluidPriceInfo)entry.fluidPriceInfo).importerBuyPrice * count).findFirst().orElse(-1.0);
    }

    public static double getAdminShopSellPrice(ItemDescription itemDescription, int count, @Nullable String adminShopName) {
        return itemPriceInfos.stream().filter(itemPriceEntry -> itemPriceEntry.itemDescription.equals(itemDescription) && itemPriceEntry.itemPriceInfo instanceof PricesItemPriceInfo info && Objects.equals(info.adminShopName, adminShopName)).map(entry -> ((PricesItemPriceInfo)entry.itemPriceInfo).adminShopSellPrice * count).findFirst().orElse(-1.0);
    }

    public static String getAdminShopSellStage(ItemDescription itemDescription, @Nullable String adminShopName) {
        return itemPriceInfos.stream()
                .filter(itemPriceEntry -> itemPriceEntry.itemDescription.equals(itemDescription) && itemPriceEntry.itemPriceInfo instanceof PricesItemPriceInfo info && Objects.equals(info.adminShopName, adminShopName))
                .map(entry -> ((PricesItemPriceInfo)entry.itemPriceInfo).adminShopSellStage)
                .map(Optional::ofNullable)
                .findFirst()
                .orElse(Optional.empty()).orElse(null);
    }

    public static double getAdminShopBuyPrice(ItemDescription itemDescription, int count, int slot, String category, @Nullable String adminShopName) {
        return itemPriceInfos.stream().filter(itemPriceEntry -> itemPriceEntry.itemDescription.equals(itemDescription) && itemPriceEntry.itemPriceInfo instanceof AdminShopItemPriceInfo info && Objects.equals(info.adminShopName, adminShopName) && info.adminShopSlot == slot && Objects.equals(info.category, category)).map(entry -> ((AdminShopItemPriceInfo)entry.itemPriceInfo).adminShopBuyPrice * count).findFirst().orElse(-1.0);
    }

    private static @Nullable ItemDescription itemDescriptionFromJson(JsonObject element) {
        try {
            return element.get("id").isJsonObject() ? ItemDescription.fromJson(element.get("id").getAsJsonObject()) :
                    ItemDescription.fromJson(element);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static void load() {
        final File file = DATA_HANDLER.DATA_FILE;
        if (file.exists()) {
            itemPriceInfos.clear();
            fluidPriceInfos.clear();
            categories.clear();

            try {
                JsonObject pricesObj = DATA_HANDLER.loadAsJson();
                JsonArray itemsArray = pricesObj.has("items") ? pricesObj.getAsJsonArray("items") : new JsonArray();
                JsonArray fluidsArray = pricesObj.has("fluids") ? pricesObj.getAsJsonArray("fluids") : new JsonArray();
                JsonArray categoriesArray = pricesObj.has("categories") ? pricesObj.getAsJsonArray("categories") : new JsonArray();

                categoriesArray.forEach(jsonElement -> {
                    JsonObject object = jsonElement.getAsJsonObject();
                    String categoryName = object.get("name").getAsString();

                    String adminShopName = object.has("adminShopName") ? object.get("adminShopName").getAsString() : null;
                    if (adminShopName != null && adminShopName.length() > 32) return;

                    ItemDescription itemDescription = itemDescriptionFromJson(object);
                    if (itemDescription == null) return;

                    JsonArray categoriesList = object.getAsJsonArray("categories");

                    if (categoriesList != null) {
                        Category category = new Category(categoryName, itemDescription, adminShopName);
                        ArrayList<Category> innerCategories = new ArrayList<>();
                        categories.put(category, innerCategories);

                        categoriesList.forEach(jsonElementInner -> {
                            if (jsonElementInner instanceof JsonObject categoryObject) {
                                String categoryNameInner = categoryObject.get("name").getAsString();

                                ItemDescription innerItemDescription = itemDescriptionFromJson(categoryObject);
                                if (innerItemDescription == null) return;

                                innerCategories.add(new Category(categoryNameInner, innerItemDescription, null));
                            }
                        });
                    }
                });

                itemsArray.forEach(jsonElement -> {
                    JsonObject object = jsonElement.getAsJsonObject();

                    ItemDescription itemDescription = ItemDescription.fromJson(object);
                    ItemPriceInfo priceInfo = ItemPriceInfo.fromJson(object);

                    if (itemDescription != null && priceInfo != null)
                        itemPriceInfos.add(new ItemPriceEntry(itemDescription, priceInfo));
                    else
                        JacksEconomy.LOGGER.warn("Invalid item price info!");
                });

                fluidsArray.forEach(jsonElement -> {
                    JsonObject object = jsonElement.getAsJsonObject();

                    FluidDescription fluidDescription = FluidDescription.fromJson(object);
                    FluidPriceInfo priceInfo = FluidPriceInfo.fromJson(object);

                    if (fluidDescription != null && priceInfo != null)
                        fluidPriceInfos.add(new FluidPriceEntry(fluidDescription, priceInfo));
                    else
                        JacksEconomy.LOGGER.warn("Invalid fluid price info!");
                });
            } catch (JsonSyntaxException | ClassCastException e) {
                JacksEconomy.LOGGER.error("Failed to load prices", e);
            }
        } else if (file.getParentFile().isDirectory() || file.getParentFile().mkdirs()) {
            save();
        }
        JacksEconomy.LOGGER.info(categories.toString());
    }

    public static void save() {
        JsonObject object = new JsonObject();
        JsonArray itemsArray = new JsonArray();
        JsonArray fluidsArray = new JsonArray();
        JsonArray categoriesArray = new JsonArray();

        itemPriceInfos.forEach((entry) -> {
            itemsArray.add(merge(entry.itemDescription.toJson(), entry.itemPriceInfo.toJson()));
        });

        fluidPriceInfos.forEach((entry) -> {
            fluidsArray.add(merge(entry.fluidDescription.toJson(), entry.fluidPriceInfo.toJson()));
        });

        categories.forEach((category, categories) -> {
            JsonObject categoryObj = category.icon.toJson();
            categoryObj.addProperty("name", category.name);

            if (category.adminShopName != null) categoryObj.addProperty("adminShopName", category.adminShopName);

            JsonArray innerCategories = new JsonArray();

            categories.forEach(categoryInner -> {
                JsonObject categoryInnerObj = categoryInner.icon().toJson();

                categoryInnerObj.addProperty("name", categoryInner.name);
                innerCategories.add(categoryInnerObj);
            });

            categoryObj.add("categories", innerCategories);
            categoriesArray.add(categoryObj);
        });

        object.add("items", itemsArray);
        object.add("fluids", fluidsArray);
        object.add("categories", categoriesArray);

        DATA_HANDLER.save(object);
    }

    public static ListTag toTag(boolean fluid) {
        if (fluid) {
            ListTag listTag = new ListTag();
            fluidPriceInfos.forEach((entry) -> {
                if (entry.fluidPriceInfo instanceof PricesFluidPriceInfo fluidPriceInfo) {
                    CompoundTag itemTag = entry.fluidDescription.toNbt().copy();
                    itemTag.putDouble("sellPrice", fluidPriceInfo.sellPrice);
                    itemTag.putDouble("importerBuyPrice", fluidPriceInfo.importerBuyPrice);
                    listTag.add(itemTag);
                }
            });
            return listTag;
        }
        ListTag listTag = new ListTag();
        itemPriceInfos.forEach((entry) -> {
            if (entry.itemPriceInfo instanceof PricesItemPriceInfo itemPriceInfo) {
                CompoundTag itemTag = entry.itemDescription.toNbt().copy();
                itemTag.putDouble("sellPrice", itemPriceInfo.sellPrice);
                itemTag.putDouble("importerBuyPrice", itemPriceInfo.importerBuyPrice);
                itemTag.putDouble("adminShopSellPrice", itemPriceInfo.adminShopSellPrice);
                if (itemPriceInfo.adminShopSellStage != null) {
                    itemTag.putString("adminShopSellStage", itemPriceInfo.adminShopSellStage);
                }
                listTag.add(itemTag);
            }
        });
        return listTag;
    }

    private static int getPagesCount(String categoryName) {
        int maxPage = 1;
        for (ItemPriceEntry entry : itemPriceInfos) {
            if (entry.itemPriceInfo instanceof AdminShopItemPriceInfo adminShopItemPriceInfo) {
                if (Objects.equals(adminShopItemPriceInfo.category, categoryName)) {
                    int itemPage = 1 + adminShopItemPriceInfo.adminShopSlot / 27;

                    if (itemPage > maxPage) {
                        maxPage = itemPage;
                    }
                }
            }
        }
        return maxPage;
    }

    public static CompoundTag toAdminShopSchemaCompound(Player player, @Nullable String name) {
        CompoundTag tag = new CompoundTag();

        ListTag itemsTag = new ListTag();
        ListTag categoriesTag = new ListTag();

        NewShopUnlocks shopUnlocks = GameStagesManager.getNewShopUnlocks(player);

        itemPriceInfos.forEach((entry) -> {
            if (entry.itemPriceInfo instanceof AdminShopItemPriceInfo itemPriceInfo) {
                if (itemPriceInfo.adminShopBuyPrice <= 0 || !Objects.equals(itemPriceInfo.adminShopName, name)) {
                    return;
                }

                CompoundTag itemTag = entry.itemDescription.toNbt();
                itemTag.putDouble("adminShopBuyPrice", itemPriceInfo.adminShopBuyPrice);
                itemTag.putString("category", itemPriceInfo.category);
                itemTag.putInt("slot", itemPriceInfo.adminShopSlot);

                if (itemPriceInfo.customAdminShopName != null) {
                    itemTag.putString("customAdminShopName", itemPriceInfo.customAdminShopName);
                }

                if (itemPriceInfo.adminShopStage != null) {
                    itemTag.putString("adminShopStage", itemPriceInfo.adminShopStage);
                }

                if (shopUnlocks != null && shopUnlocks.unlockedItems.contains(new NewShopUnlocks.Item(itemPriceInfo.adminShopSlot, itemPriceInfo.category))) {
                    itemTag.putBoolean("recentlyUnlocked", true);
                }

                itemsTag.add(itemTag);
            } else if (entry.itemPriceInfo instanceof PricesItemPriceInfo itemPriceInfo) {
                if (itemPriceInfo.adminShopSellPrice <= 0 || !Objects.equals(itemPriceInfo.adminShopName, name)) {
                    return;
                }

                CompoundTag itemTag = entry.itemDescription.toNbt();

                itemTag.putDouble("adminShopSellPrice", itemPriceInfo.adminShopSellPrice);

                if (itemPriceInfo.adminShopSellStage != null) {
                    itemTag.putString("adminShopSellStage", itemPriceInfo.adminShopSellStage);
                }

                itemsTag.add(itemTag);
            }
        });

        categories.forEach((category, categories) -> {
            if (!Objects.equals(category.adminShopName, name)) return;

            CompoundTag compoundTag = new CompoundTag();

            compoundTag.putString("name", category.name);
            compoundTag.put("item", category.icon.toNbt());

            if (shopUnlocks != null && shopUnlocks.unlockedCategories.contains(category.name)) {
                compoundTag.putBoolean("recentlyUnlocked", true);
            }

            ListTag innerCategories = new ListTag();

            categories.forEach(categoryInner -> {
                CompoundTag compoundTagInner = new CompoundTag();

                compoundTagInner.putString("name", categoryInner.name);
                compoundTagInner.put("item", categoryInner.icon().toNbt());

                if (shopUnlocks != null && shopUnlocks.unlockedCategories.contains(category.name + ":" + categoryInner.name)) {
                    compoundTagInner.putBoolean("recentlyUnlocked", true);
                }

                innerCategories.add(compoundTagInner);
            });

            compoundTag.put("categories", innerCategories);
            categoriesTag.add(compoundTag);
        });

        tag.put("items", itemsTag);
        tag.put("categories", categoriesTag);

        return tag;
    }

    // TODO: merge if exists?
    public static void addPriceInfo(ItemDescription itemDescription, ItemPriceInfo priceInfo) {
        itemPriceInfos.add(new ItemPriceEntry(itemDescription, priceInfo));
    }

    public static void addPriceInfo(ItemStack itemStack, ItemPriceInfo priceInfo) {
        addPriceInfo(ItemDescription.ofItem(itemStack), priceInfo);
    }

    public static void addPriceInfo(FluidDescription fluidDescription, FluidPriceInfo priceInfo) {
        fluidPriceInfos.add(new FluidPriceEntry(fluidDescription, priceInfo));
    }

    public static void addPriceInfo(FluidStack fluidStack, FluidPriceInfo priceInfo) {
        addPriceInfo(FluidDescription.ofFluid(fluidStack), priceInfo);
    }

    public static void sendDataToPlayers(boolean includeAdminShops) {
        JacksEconomy.server.getPlayerList().getPlayers().forEach(player -> sendDataToPlayer(player, includeAdminShops));
    }

    public static void sendDataToPlayer(ServerPlayer serverPlayer, boolean includeAdminShops) {
        PacketDistributor.sendToPlayer(serverPlayer, new PricesInfoPacket(toTag(false), toTag(true)));

        if (includeAdminShops) {
            Set<String> names = getCategories().keySet().stream().map(PriceManager.Category::adminShopName).filter(Objects::nonNull).collect(Collectors.toSet());

            for (String name: names) {
                PacketDistributor.sendToPlayer(serverPlayer, new AdminShopSchemaPacket(PriceManager.toAdminShopSchemaCompound(serverPlayer, name), Optional.of(name), Config.oneItemCurrencyMode.get()));
            }
            PacketDistributor.sendToPlayer(serverPlayer, new AdminShopSchemaPacket(PriceManager.toAdminShopSchemaCompound(serverPlayer, null), Optional.empty(), Config.oneItemCurrencyMode.get()));
        }
    }

    private static JsonObject merge(JsonObject object1, JsonObject object2) {
        JsonObject object = new JsonObject();
        object1.keySet().forEach(name -> object.add(name, object1.get(name)));
        object2.keySet().forEach(name -> object.add(name, object2.get(name)));
        return object;
    }

    public record Category(String name, ItemDescription icon, @Nullable String adminShopName) {}
    public record ItemPriceEntry(ItemDescription itemDescription, ItemPriceInfo itemPriceInfo) {}
    public record FluidPriceEntry(FluidDescription fluidDescription, FluidPriceInfo fluidPriceInfo) {}
}
