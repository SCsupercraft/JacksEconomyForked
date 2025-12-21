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
import me.khajiitos.jackseconomy.util.ItemHelper;
import me.khajiitos.jackseconomy.util.NewShopUnlocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
    private static final DataHandler DATA_HANDLER = new DataHandler(
            new File("config/jackseconomy_prices.dat"),
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

    public static void load() {
        if (DATA_HANDLER.fileExists()) {
            itemPriceInfos.clear();
            fluidPriceInfos.clear();
            categories.clear();

            try {
                CompoundTag pricesTag = DATA_HANDLER.load();
                ListTag itemsArray = pricesTag.contains("items")
                        ? pricesTag.getList("items", Tag.TAG_COMPOUND)
                        : new ListTag();
                ListTag fluidsArray = pricesTag.contains("fluids")
                        ? pricesTag.getList("fluids", Tag.TAG_COMPOUND)
                        : new ListTag();
                ListTag categoriesArray = pricesTag.contains("categories")
                        ? pricesTag.getList("categories", Tag.TAG_COMPOUND)
                        : new ListTag();

                categoriesArray.forEach(tag -> {
                    CompoundTag compoundTag = ((CompoundTag) tag);
                    String categoryName = compoundTag.getString("name");

                    String adminShopName = compoundTag.contains("adminShopName") ? compoundTag.getString("adminShopName") : null;
                    if (adminShopName != null && adminShopName.length() > 32) return;

                    ItemDescription itemDescription = ItemDescription.fromNbt(compoundTag);
                    if (itemDescription == null) return;

                    ListTag categoriesList = compoundTag.getList("categories", Tag.TAG_COMPOUND);

                    if (!categoriesList.isEmpty()) {
                        Category category = new Category(categoryName, itemDescription, adminShopName);
                        ArrayList<Category> innerCategories = new ArrayList<>();
                        categories.put(category, innerCategories);

                        categoriesList.forEach(tagInner -> {
                            if (tagInner instanceof CompoundTag categoryTag) {
                                String categoryNameInner = categoryTag.getString("name");

                                ItemDescription innerItemDescription = ItemDescription.fromNbt(categoryTag);
                                if (innerItemDescription == null) return;

                                innerCategories.add(new Category(categoryNameInner, innerItemDescription, null));
                            }
                        });
                    }
                });

                itemsArray.forEach(tag -> {
                    CompoundTag compoundTag = ((CompoundTag) tag);

                    ItemDescription itemDescription = ItemDescription.fromNbt(compoundTag);
                    ItemPriceInfo priceInfo = ItemPriceInfo.fromNbt(compoundTag);

                    if (itemDescription != null && priceInfo != null)
                        itemPriceInfos.add(new ItemPriceEntry(itemDescription, priceInfo));
                    else
                        JacksEconomy.LOGGER.warn("Invalid item price info!");
                });

                fluidsArray.forEach(tag -> {
                    CompoundTag compoundTag = ((CompoundTag) tag);

                    FluidDescription fluidDescription = FluidDescription.fromNbt(compoundTag);
                    FluidPriceInfo priceInfo = FluidPriceInfo.fromNbt(compoundTag);

                    if (fluidDescription != null && priceInfo != null)
                        fluidPriceInfos.add(new FluidPriceEntry(fluidDescription, priceInfo));
                    else
                        JacksEconomy.LOGGER.warn("Invalid fluid price info!");
                });
            } catch (JsonSyntaxException | ClassCastException e) {
                JacksEconomy.LOGGER.error("Failed to load prices", e);
            }
        } else save();
        JacksEconomy.LOGGER.info(categories.toString());
    }

    public static void save() {
        CompoundTag tag = new CompoundTag();
        ListTag itemsArray = new ListTag();
        ListTag fluidsArray = new ListTag();
        ListTag categoriesArray = new ListTag();

        itemPriceInfos.forEach((entry) ->
            itemsArray.add(entry.itemDescription.toNbt().merge(entry.itemPriceInfo.toNbt())));

        fluidPriceInfos.forEach((entry) ->
            fluidsArray.add(entry.fluidDescription.toNbt().merge(entry.fluidPriceInfo.toNbt())));

        categories.forEach((category, categories) -> {
            CompoundTag categoryTag = category.icon.toNbt();
            categoryTag.putString("name", category.name);

            if (category.adminShopName != null) categoryTag.putString("adminShopName", category.adminShopName);

            ListTag innerCategories = new ListTag();

            categories.forEach(categoryInner -> {
                CompoundTag categoryInnerTag = categoryInner.icon().toNbt();

                categoryInnerTag.putString("name", categoryInner.name);
                innerCategories.add(categoryInnerTag);
            });

            categoryTag.put("categories", innerCategories);
            categoriesArray.add(categoryTag);
        });

        tag.put("items", itemsArray);
        tag.put("fluids", fluidsArray);
        tag.put("categories", categoriesArray);

        DATA_HANDLER.save(tag);
    }

    public static void resetData() {
        itemPriceInfos.clear();
        fluidPriceInfos.clear();
        categories.clear();
        save();
        sendDataToPlayers(true);
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
            Set<String> names = getCategories().keySet().stream().map(Category::adminShopName).filter(Objects::nonNull).collect(Collectors.toSet());

            for (String name: names) {
                PacketDistributor.sendToPlayer(serverPlayer, new AdminShopSchemaPacket(PriceManager.toAdminShopSchemaCompound(serverPlayer, name), Optional.of(name), Config.oneItemCurrencyMode.get()));
            }
            PacketDistributor.sendToPlayer(serverPlayer, new AdminShopSchemaPacket(PriceManager.toAdminShopSchemaCompound(serverPlayer, null), Optional.empty(), Config.oneItemCurrencyMode.get()));
        }
    }

    public record Category(String name, ItemDescription icon, @Nullable String adminShopName) {}
    public record ItemPriceEntry(ItemDescription itemDescription, ItemPriceInfo itemPriceInfo) {}
    public record FluidPriceEntry(FluidDescription fluidDescription, FluidPriceInfo fluidPriceInfo) {}
}
