package me.khajiitos.jackseconomy.data.price;

import com.google.gson.*;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.data.DataHandler;
import me.khajiitos.jackseconomy.gamestages.GameStagesManager;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.packet.PricesInfoPacket;
import me.khajiitos.jackseconomy.util.ItemHelper;
import me.khajiitos.jackseconomy.util.NewShopUnlocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class PriceManager {
    //private static final LinkedHashMap<ItemDescription, ItemPriceInfo> itemPriceInfos = new LinkedHashMap<>();
    private static final List<ItemPriceEntry> itemPriceInfos = new ArrayList<>();
    private static final List<FluidPriceEntry> fluidPriceInfos = new ArrayList<>();
    private static final LinkedHashMap<Category, List<Category>> categories = new LinkedHashMap<>();
    private static final DataHandler DATA_HANDLER = new DataHandler.JSONDataHandler(
            new File("config/jackseconomy_prices.json")
    );

    static {
        itemPriceInfos.add(new ItemPriceEntry(new ItemDescription(Items.DIAMOND, null), new PricesItemPriceInfo(50.0, 45.0,100.0, null)));
        itemPriceInfos.add(new ItemPriceEntry(new ItemDescription(Items.DIAMOND, null), new AdminShopItemPriceInfo(150.0, "General:Gems", 0, null, null)));
        fluidPriceInfos.add(new FluidPriceEntry(new FluidDescription(Fluids.LAVA, null), new PricesFluidPriceInfo(0.02, 0.05)));

        ArrayList<Category> categoriesInnerDefault = new ArrayList<>();
        categoriesInnerDefault.add(new Category("Gems", new ItemDescription(Items.DIAMOND, new CompoundTag())));
        categories.put(new Category("General", new ItemDescription(Items.DIRT, new CompoundTag())), categoriesInnerDefault);
    }

    @Deprecated
    public static ItemPriceInfo getInfo(ItemDescription itemDescription) {
        return itemPriceInfos.stream().filter(itemPriceEntry -> itemPriceEntry.itemDescription.equals(itemDescription)).findFirst().map(ItemPriceEntry::itemPriceInfo).orElse(null);
    }

    public static ItemPriceInfo getInfo(ItemStack itemStack) {
        return getInfo(ItemDescription.ofItem(itemStack));
    }

    public static PricesItemPriceInfo getPricesInfo(ItemDescription itemDescription) {
        return itemPriceInfos.stream().filter(itemPriceEntry -> itemPriceEntry.itemDescription.equals(itemDescription) && itemPriceEntry.itemPriceInfo instanceof PricesItemPriceInfo).map(entry -> ((PricesItemPriceInfo)entry.itemPriceInfo)).findFirst().orElse(null);
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
        return itemPriceInfos.stream().filter(itemPriceEntry -> itemPriceEntry.itemDescription.equals(itemDescription) && itemPriceEntry.itemPriceInfo instanceof PricesItemPriceInfo).map(entry -> ((PricesItemPriceInfo)entry.itemPriceInfo).sellPrice * count).findFirst().orElse(-1.0);
    }

    public static double getImporterBuyPrice(ItemDescription itemDescription, int count) {
        return itemPriceInfos.stream().filter(itemPriceEntry -> itemPriceEntry.itemDescription.equals(itemDescription) && itemPriceEntry.itemPriceInfo instanceof PricesItemPriceInfo).map(entry -> ((PricesItemPriceInfo)entry.itemPriceInfo).importerBuyPrice * count).findFirst().orElse(-1.0);
    }

    public static double getFluidExporterSellPrice(FluidDescription fluidDescription, int count) {
        return fluidPriceInfos.stream().filter(fluidPriceEntry -> fluidPriceEntry.fluidDescription.equals(fluidDescription) && fluidPriceEntry.fluidPriceInfo instanceof PricesFluidPriceInfo).map(entry -> ((PricesFluidPriceInfo)entry.fluidPriceInfo).sellPrice * count).findFirst().orElse(-1.0);
    }

    public static double getFluidImporterBuyPrice(FluidDescription fluidDescription, int count) {
        return fluidPriceInfos.stream().filter(fluidPriceEntry -> fluidPriceEntry.fluidDescription.equals(fluidDescription) && fluidPriceEntry.fluidPriceInfo instanceof PricesFluidPriceInfo).map(entry -> ((PricesFluidPriceInfo)entry.fluidPriceInfo).importerBuyPrice * count).findFirst().orElse(-1.0);
    }

    public static double getAdminShopSellPrice(ItemDescription itemDescription, int count) {
        return itemPriceInfos.stream().filter(itemPriceEntry -> itemPriceEntry.itemDescription.equals(itemDescription) && itemPriceEntry.itemPriceInfo instanceof PricesItemPriceInfo).map(entry -> ((PricesItemPriceInfo)entry.itemPriceInfo).adminShopSellPrice * count).findFirst().orElse(-1.0);
    }

    public static String getAdminShopSellStage(ItemDescription itemDescription) {
        return itemPriceInfos.stream()
                .filter(itemPriceEntry -> itemPriceEntry.itemDescription.equals(itemDescription) && itemPriceEntry.itemPriceInfo instanceof PricesItemPriceInfo)
                .map(entry -> ((PricesItemPriceInfo)entry.itemPriceInfo).adminShopSellStage)
                .map(Optional::ofNullable)
                .findFirst()
                .orElse(Optional.empty()).orElse(null);
    }

    public static double getAdminShopBuyPrice(ItemDescription itemDescription, int count, int slot, String category) {
        return itemPriceInfos.stream().filter(itemPriceEntry -> itemPriceEntry.itemDescription.equals(itemDescription) && itemPriceEntry.itemPriceInfo instanceof AdminShopItemPriceInfo adminShopItemPriceInfo && adminShopItemPriceInfo.adminShopSlot == slot && Objects.equals(adminShopItemPriceInfo.category, category)).map(entry -> ((AdminShopItemPriceInfo)entry.itemPriceInfo).adminShopBuyPrice * count).findFirst().orElse(-1.0);
    }

    private static @Nullable ItemDescription itemDescriptionFromJson(JsonElement element) {
        try {
            return element.isJsonObject() ? ItemDescription.fromJson(element.getAsJsonObject()) : // new json schema
                    new ItemDescription(ItemHelper.getItem(element.getAsString()), // old json schema
                            new CompoundTag());
        } catch (NullPointerException e) {
            // item was null when creating an item description from the old schema
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
                    JsonElement itemDesc = object.get("item");

                    ItemDescription itemDescription = itemDescriptionFromJson(itemDesc);
                    if (itemDescription == null) return;

                    JsonArray categoriesList = object.getAsJsonArray("categories");

                    if (categoriesList != null) {
                        Category category = new Category(categoryName, itemDescription);
                        ArrayList<Category> innerCategories = new ArrayList<>();
                        categories.put(category, innerCategories);

                        categoriesList.forEach(jsonElementInner -> {
                            if (jsonElementInner instanceof JsonObject categoryObject) {
                                String categoryNameInner = categoryObject.get("name").getAsString();
                                JsonElement itemDescInner = categoryObject.get("item");

                                ItemDescription innerItemDescription = itemDescriptionFromJson(itemDescInner);
                                if (innerItemDescription == null) return;

                                innerCategories.add(new Category(categoryNameInner, innerItemDescription));
                            }
                        });
                    }
                });

                itemsArray.forEach(jsonElement -> {
                    JsonObject object = jsonElement.getAsJsonObject();

                    ItemDescription itemDescription = ItemDescription.fromJson(object);

                    if (itemDescription != null) {
                        ItemPriceInfo[] priceInfos = ItemPriceInfo.fromJson(object);
                        for (ItemPriceInfo priceInfo : priceInfos) {
                            itemPriceInfos.add(new ItemPriceEntry(itemDescription, priceInfo));
                        }
                    } else {
                        JacksEconomy.LOGGER.warn("Invalid price info");
                    }
                });

                fluidsArray.forEach(jsonElement -> {
                    JsonObject object = jsonElement.getAsJsonObject();

                    FluidDescription fluidDescription = FluidDescription.fromJson(object);

                    if (fluidDescription != null) {
                        FluidPriceInfo[] priceInfos = FluidPriceInfo.fromJson(object);
                        for (FluidPriceInfo priceInfo : priceInfos) {
                            fluidPriceInfos.add(new FluidPriceEntry(fluidDescription, priceInfo));
                        }
                    } else {
                        JacksEconomy.LOGGER.warn("Invalid price info");
                    }
                });
            } catch (JsonSyntaxException | ClassCastException e) {
                JacksEconomy.LOGGER.error("Failed to load item prices", e);
            }
        } else if (file.getParentFile().isDirectory() || file.getParentFile().mkdirs()) {
            save();
        }
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
            JsonObject categoryObj = new JsonObject();
            categoryObj.add("item", category.icon.toJson());
            categoryObj.addProperty("name", category.name);

            JsonArray innerCategories = new JsonArray();

            categories.forEach(categoryInner -> {
                JsonObject categoryInnerObj = new JsonObject();

                categoryInnerObj.add("item", categoryInner.icon.toJson());
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

    public static CompoundTag toAdminShopSchemaCompound(Player player) {
        CompoundTag tag = new CompoundTag();

        ListTag itemsTag = new ListTag();
        ListTag categoriesTag = new ListTag();

        NewShopUnlocks shopUnlocks = GameStagesManager.getNewShopUnlocks(player);

        itemPriceInfos.forEach((entry) -> {
            if (entry.itemPriceInfo instanceof AdminShopItemPriceInfo itemPriceInfo) {
                if (itemPriceInfo.adminShopBuyPrice <= 0) {
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
                if (itemPriceInfo.adminShopSellPrice <= 0) {
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

    public static void sendDataToPlayers() {
        JacksEconomy.server.getPlayerList().getPlayers().forEach(serverPlayer -> Packets.sendToClient(serverPlayer, new PricesInfoPacket(toTag(false), toTag(true))));
    }

    private static JsonObject merge(JsonObject object1, JsonObject object2) {
        JsonObject object = new JsonObject();
        object1.keySet().forEach(name -> object.add(name, object1.get(name)));
        object2.keySet().forEach(name -> object.add(name, object2.get(name)));
        return object;
    }

    public record Category(String name, ItemDescription icon) {}
    public record ItemPriceEntry(ItemDescription itemDescription, ItemPriceInfo itemPriceInfo) {}
    public record FluidPriceEntry(FluidDescription fluidDescription, FluidPriceInfo fluidPriceInfo) {}
}
