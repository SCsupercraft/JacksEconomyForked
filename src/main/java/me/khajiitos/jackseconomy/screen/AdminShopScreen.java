package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.gamestages.GameStagesCheck;
import me.khajiitos.jackseconomy.gamestages.GameStagesManager;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.item.OIMWalletItem;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.menu.AdminShopMenu;
import me.khajiitos.jackseconomy.packet.AcknowledgeUnlocksPacket;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import me.khajiitos.jackseconomy.screen.widget.BetterScrollPanel;
import me.khajiitos.jackseconomy.screen.widget.CategoryEntry;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import me.khajiitos.jackseconomy.util.NewShopUnlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class AdminShopScreen extends AbstractContainerScreen<AdminShopMenu> {
    protected static final ResourceLocation BACKGROUND = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/admin_shop.png");
    protected static final ResourceLocation NO_WALLET = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/no_wallet.png");
    protected static final ResourceLocation BALANCE_PROGRESS = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/balance_progress.png");
    protected static final ResourceLocation QUESTION_MARK = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/question_mark.png");
    protected static final ResourceLocation STAR = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/star.png");
    protected static final ResourceLocation LOCK = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/lock.png");

    protected @Nullable String adminShopName;

    protected final LinkedHashMap<Category, LinkedHashMap<InnerCategory, List<ShopItem>>> shopItems;

    protected final HashMap<ItemDescription, ItemSellabilityInfo> sellPrices;

    public LinkedHashMap<CategorizedShopItem, Integer> shoppingCart = new LinkedHashMap<>();
    public LinkedHashMap<ItemDescription, Integer> itemsToSell = new LinkedHashMap<>();

    protected InnerCategory selectedCategory = null;
    protected Category selectedBigCategory = null;
    protected int categoryOffset = 0;
    protected int page = 0;
    protected List<Component> tooltip;

    protected ImageButton categoryPreviousButton;
    protected ImageButton categoryNextButton;
    protected ImageButton pageNextButton;
    protected ImageButton pagePreviousButton;
    protected ImageButton shoppingCartButton;

    protected boolean shouldRenderBackground;

    protected BetterScrollPanel categoryPanel;

    protected final NewShopUnlocks newShopUnlocks;
    protected final NewShopUnlocks acknowledgedShopUnlocks;
    public final boolean oneItemCurrencyMode;

    protected AdminShopScreen(AdminShopMenu pMenu, Inventory pPlayerInventory, Component pTitle, LinkedHashMap<Category, LinkedHashMap<InnerCategory, List<ShopItem>>> shopItems, HashMap<ItemDescription, ItemSellabilityInfo> sellPrices, @Nullable String adminShopName) {
        super(pMenu, pPlayerInventory, pTitle);

        this.oneItemCurrencyMode = pMenu.oneItemCurrencyMode;

        newShopUnlocks = new NewShopUnlocks();
        acknowledgedShopUnlocks = new NewShopUnlocks();

        this.imageHeight = 232;
        this.inventoryLabelY = this.imageHeight - 94;

        this.shopItems = shopItems;
        this.sellPrices = sellPrices;

        this.adminShopName = adminShopName;

        this.selectFirstAvailableCategory();
    }

    public AdminShopScreen(AdminShopMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        this(pMenu, pPlayerInventory, pTitle, new LinkedHashMap<>() {
            @Override
            public LinkedHashMap<InnerCategory, List<ShopItem>> get(Object key) {
                return this.getOrDefault(key, new LinkedHashMap<>());
            }
        }, new HashMap<>(), null);
    }

    public void onShopData(CompoundTag data, @Nullable String adminShopName) {
        this.adminShopName = adminShopName;

        ListTag categoriesTag = data.getList("categories", Tag.TAG_COMPOUND);

        categoriesTag.forEach(tag -> {
            if (tag instanceof CompoundTag compoundTag) {
                String categoryName = compoundTag.getString("name");
                ItemDescription itemDescription = ItemDescription.fromNbt(compoundTag.getCompound("item"));

                if (itemDescription == null) {
                    return;
                }

                if (compoundTag.contains("recentlyUnlocked", Tag.TAG_BYTE) && compoundTag.getBoolean("recentlyUnlocked")) {
                    newShopUnlocks.unlockedCategories.add(categoryName);
                }

                Category category = new Category(categoryName, itemDescription);
                LinkedHashMap<InnerCategory, List<ShopItem>> innerCategories = new LinkedHashMap<>();
                shopItems.put(category, innerCategories);
                ListTag categoriesInnerTag = compoundTag.getList("categories", Tag.TAG_COMPOUND);

                categoriesInnerTag.forEach(innerTag -> {
                    if (innerTag instanceof CompoundTag innerCompoundTag) {
                        String innerCategoryName = innerCompoundTag.getString("name");
                        ItemDescription innerItemDescription = ItemDescription.fromNbt(innerCompoundTag.getCompound("item"));

                        if (innerItemDescription == null) {
                            return;
                        }

                        if (innerCompoundTag.contains("recentlyUnlocked", Tag.TAG_BYTE) && innerCompoundTag.getBoolean("recentlyUnlocked")) {
                            newShopUnlocks.unlockedCategories.add(categoryName + ":" + innerCategoryName);
                        }

                        InnerCategory innerCategory = new InnerCategory(innerCategoryName, innerItemDescription);
                        innerCategories.put(innerCategory, new ArrayList<>());
                    }
                });
            }
        });

        LinkedHashMap<String, List<UnpreparedShopItem>> slotlessShopItems = new LinkedHashMap<>();

        ListTag itemsTag = data.getList("items", Tag.TAG_COMPOUND);

        itemsTag.forEach(tag -> {
            if (tag instanceof CompoundTag compoundTag) {
                ItemDescription itemDescription = ItemDescription.fromNbt(compoundTag);

                if (itemDescription == null) {
                    return;
                }

                if (compoundTag.contains("adminShopSellPrice")) {
                    double sellPrice = compoundTag.getDouble("adminShopSellPrice");
                    String sellStage = compoundTag.contains("adminShopSellStage") ? compoundTag.getString("adminShopSellStage") : null;
                    sellPrices.put(itemDescription, new ItemSellabilityInfo(oneItemCurrencyMode ? Math.round(sellPrice) : sellPrice, sellStage));
                    return;
                }

                double buyPrice = compoundTag.getDouble("adminShopBuyPrice");
                int slot = compoundTag.contains("slot") ? compoundTag.getInt("slot") : -1;
                String customName = compoundTag.contains("customAdminShopName") ? compoundTag.getString("customAdminShopName") : null;
                String stage = compoundTag.contains("adminShopStage") ? compoundTag.getString("adminShopStage") : null;
                String category = compoundTag.getString("category");

                if (compoundTag.contains("recentlyUnlocked", Tag.TAG_BYTE) && compoundTag.getBoolean("recentlyUnlocked")) {
                    newShopUnlocks.unlockedItems.add(new NewShopUnlocks.Item(slot, category));
                }

                double price = oneItemCurrencyMode ? Math.round(buyPrice) : buyPrice;
                if (slot < 0) {
                    slotlessShopItems.computeIfAbsent(category, categoryName -> new ArrayList<>()).add(new UnpreparedShopItem(itemDescription, price, customName, stage));
                } else {
                    String[] categoryNamesInner = category.split(":", 2);
                    if (categoryNamesInner.length < 2) {
                        return;
                    }

                    String bigCategoryName = categoryNamesInner[0];
                    String innerCategoryName = categoryNamesInner[1];

                    for (Map.Entry<Category, LinkedHashMap<InnerCategory, List<ShopItem>>> categoryEntry : shopItems.entrySet()) {
                        if (categoryEntry.getKey().name.equals(bigCategoryName)) {
                            for (Map.Entry<InnerCategory, List<ShopItem>> entry : shopItems.get(categoryEntry.getKey()).entrySet()) {
                                if (entry.getKey().name.equals(innerCategoryName)) {
                                    entry.getValue().add(new ShopItem(itemDescription, price, slot, customName, stage));
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }

            }
        });

        slotlessShopItems.forEach((categoryName, list) -> {
            String[] categoriesNames = categoryName.split(":", 2);

            if (categoriesNames.length < 2) {
                return;
            }

            String bigCategoryName = categoriesNames[0];
            String innerCategoryName = categoriesNames[1];

            for (Map.Entry<Category, LinkedHashMap<InnerCategory, List<ShopItem>>> entry : shopItems.entrySet()) {
                if (entry.getKey().name.equals(bigCategoryName)) {
                    for (UnpreparedShopItem item : list) {
                        for (Map.Entry<InnerCategory, List<ShopItem>> entry1 : entry.getValue().entrySet()) {
                            if (entry1.getKey().name.equals(innerCategoryName)) {
                                shopItems.get(entry.getKey()).get(entry1.getKey()).add(new ShopItem(item.itemDescription, item.price, this.findFirstAvailableSlot(entry.getKey()), item.customName, item.stage));
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        });

        this.selectFirstAvailableCategory();

        this.clearWidgets();
        this.init();
    }

    protected void selectFirstAvailableCategory() {
        this.selectedBigCategory = this.shopItems.entrySet().stream().filter(entry -> this.selectedBigCategory == null || entry.getValue().values().stream().anyMatch(this::hasAnyItemUnlocked)).map(Map.Entry::getKey).findFirst().orElse(this.selectedBigCategory);

        if (this.selectedBigCategory != null) {
            this.selectedCategory = this.shopItems.get(this.selectedBigCategory).entrySet().stream().filter(entry -> this.hasAnyItemUnlocked(entry.getValue())).map(Map.Entry::getKey).findFirst().orElse(null);
        }
    }

    private void initButtons() {
        if (this.categoryPreviousButton != null) {
            this.removeWidget(this.categoryPreviousButton);
            this.categoryPreviousButton = null;
        }

        if (this.categoryNextButton != null) {
            this.removeWidget(this.categoryNextButton);
            this.categoryNextButton = null;
        }

        if (this.pagePreviousButton != null) {
            this.removeWidget(this.pagePreviousButton);
            this.pagePreviousButton = null;
        }

        if (this.pageNextButton != null) {
            this.removeWidget(this.pageNextButton);
            this.pageNextButton = null;
        }

        if (this.categoryOffset > 0) {
            this.categoryPreviousButton = this.addRenderableWidget(new ImageButton(this.leftPos + 6, this.topPos + 30, 6, 10, 176, 58, 10, BACKGROUND, (b) -> {
                this.categoryOffset--;
                this.initButtons();
            }));
        }

        if (this.categoryOffset + (this.isEditMode() ? 7 : 8) < this.shopItems.size()) {
            this.categoryNextButton = this.addRenderableWidget(new ImageButton(this.leftPos + 164, this.topPos + 30, 6, 10, 182, 58, 10, BACKGROUND, (b) -> {
                this.categoryOffset++;
                this.initButtons();
            }));
        }

        if (this.page > 0) {
            this.pagePreviousButton = this.addRenderableWidget(new ImageButton(this.leftPos + 55, this.topPos + 126, 6, 10, 176, 58, 10, BACKGROUND, (b) -> {
                this.page--;
                this.initButtons();
            }));
        }

        if (this.isEditMode() || this.page < this.getPages(this.selectedCategory) - 1) {
            this.pageNextButton = this.addRenderableWidget(new ImageButton(this.leftPos + 113, this.topPos + 126, 6, 10, 182, 58, 10, BACKGROUND, (b) -> {
                this.page++;
                this.initButtons();
            }));
        }
    }

    protected <A, B> void addAtIndex(LinkedHashMap<A, B> hashMap, A key, B value, int index) {
        if (index == hashMap.size()) {
            hashMap.put(key, value);
            return;
        }

        LinkedHashMap<A, B> copy = new LinkedHashMap<>(hashMap);

        hashMap.clear();

        int i = 0;

        for (Map.Entry<A, B> entry : copy.entrySet()) {
            if (i == index) {
                hashMap.put(key, value);
            }

            hashMap.put(entry.getKey(), entry.getValue());
            i++;
        }
    }

    /**
     * @return true if the sellItems map was modified due to lacking items in the inventory, else false
     */
    public boolean reduceSellItemsIfMissing() {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) {
            return false;
        }

        Inventory inventory = player.getInventory();

        HashMap<ItemDescription, Integer> itemsInInventory = new HashMap<>();

        for (ItemStack itemStack : inventory.items) {
            if (itemStack.isEmpty()) {
                continue;
            }

            ItemDescription itemDescription = ItemDescription.ofItem(itemStack);
            itemsInInventory.put(itemDescription, itemsInInventory.getOrDefault(itemDescription, 0) + itemStack.getCount());
        }

        boolean modified = false;

        for (ItemDescription itemDescription : itemsToSell.keySet()) {
            int amount = itemsToSell.get(itemDescription);
            int inventoryAmount = itemsInInventory.getOrDefault(itemDescription, 0);

            if (amount > inventoryAmount) {
                modified = true;

                if (inventoryAmount > 0) {
                    itemsToSell.put(itemDescription, inventoryAmount);
                } else {
                    itemsToSell.remove(itemDescription);
                }
            }

        }

        return modified;
    }

    protected int getPages(InnerCategory selectedCategory) {
        if (selectedCategory == null) {
            return 0;
        }

        List<ShopItem> itemsList = this.shopItems.get(selectedBigCategory).get(selectedCategory);

        int pages = 1;

        for (ShopItem shopItem : itemsList) {
            int onPage = 1 + shopItem.slot() / 27;
            if (pages < onPage) {
                pages = onPage;
            }
        }

        return pages;
    }

    public BigDecimal getShoppingCartValue() {
        BigDecimal value = BigDecimal.ZERO;
        for (Map.Entry<CategorizedShopItem, Integer> items : this.shoppingCart.entrySet()) {
            value = value.add(BigDecimal.valueOf(items.getKey().price()).multiply(new BigDecimal(items.getValue())));
        }
        return value;
    }

    protected @Nullable InnerCategory getCategoryById(int id) {
        List<InnerCategory> categories = this.getInnerCategories();

        if (id < 0 || id >= categories.size()) {
            return null;
        }

        return categories.get(id);
    }

    protected void selectBigCategory(Category bigCategory) {
        // NOTTODO: make sure this is actually a big category, otherwise we will crash lol
        // Update: whatever it works

        this.selectedBigCategory = bigCategory;
        this.selectedCategory = null;

        for (Map.Entry<InnerCategory, List<ShopItem>> categoryEntry : this.shopItems.get(bigCategory).entrySet()) {
            if (hasAnyItemUnlocked(categoryEntry.getValue())) {
                this.selectedCategory = categoryEntry.getKey();
                break;
            }
        }

        this.page = 0;
        this.initButtons();
    }

    protected void initCategoryPanel() {
        BetterScrollPanel oldPanel = this.categoryPanel;
        this.categoryPanel = this.addRenderableWidget(new BetterScrollPanel(Minecraft.getInstance(), this.leftPos - 80, this.topPos + 20, 75, this.imageHeight - 40));

        for (Category category : shopItems.keySet()) {
            if (!this.isEditMode()) {
                if (!this.shopItems.get(category).isEmpty() && this.getInnerCategories(category).isEmpty()) {
                    continue;
                }
            }
            
            this.categoryPanel.children.add(new CategoryEntry(0, 0, this.categoryPanel.isScrollbarDisplayed() ? 67 : 75, 25, category, (entry, button) -> selectBigCategory(category), () -> this.selectedBigCategory == category, () -> this.newShopUnlocks.unlockedCategories.contains(category.getName())));
        }

        // Makes the list not scroll all the way up when adding new categories
        if (oldPanel != null) {
            this.categoryPanel.setScrollDistance(oldPanel.getScrollDistance(), true);
        }
    }

    // nice name
    public void acknowledgeCategoryUnlocksForEmpties() {
        for (Map.Entry<Category, LinkedHashMap<InnerCategory, List<ShopItem>>> entry : this.shopItems.entrySet()) {
            boolean anyNewlyUnlockedItemsInCategory = false;

            for (Map.Entry<InnerCategory, List<ShopItem>> innerEntry : entry.getValue().entrySet()) {
                boolean anyNewlyUnlockedItemsInInnerCategory = false;
                for (ShopItem shopItem : innerEntry.getValue()) {
                    NewShopUnlocks.Item item = new NewShopUnlocks.Item(shopItem.slot, entry.getKey().name + ":" + innerEntry.getKey().name);
                    if (newShopUnlocks.unlockedItems.contains(item)) {
                        anyNewlyUnlockedItemsInInnerCategory = true;
                        anyNewlyUnlockedItemsInCategory = true;
                        break;
                    }
                }

                String innerCategoryName = entry.getKey().name + ":" + innerEntry.getKey().name;
                if (!anyNewlyUnlockedItemsInInnerCategory && this.newShopUnlocks.unlockedCategories.contains(innerCategoryName)) {
                    this.newShopUnlocks.unlockedCategories.remove(innerCategoryName);
                    this.acknowledgedShopUnlocks.unlockedCategories.add(innerCategoryName);
                }
            }

            String categoryName = entry.getKey().name;

            if (!anyNewlyUnlockedItemsInCategory && this.newShopUnlocks.unlockedCategories.contains(categoryName)) {
                this.newShopUnlocks.unlockedCategories.remove(categoryName);
                this.acknowledgedShopUnlocks.unlockedCategories.add(categoryName);
            }
        }
    }

    @Override
    protected void slotClicked(@Nullable Slot pSlot, int pSlotId, int pMouseButton, @NotNull ClickType pType) {
        if (!Config.disableAdminShopSelling.get() && pSlot != null && (pType == ClickType.QUICK_MOVE || pType == ClickType.SWAP || pType == ClickType.PICKUP) && (pMouseButton == 0 || pMouseButton == 1)) {
            ItemDescription itemDescription = ItemDescription.ofItem(pSlot.getItem());
            ItemSellabilityInfo info = this.sellPrices.get(itemDescription);
            String stage = info != null ? info.stage : null;

            if (stage != null && !GameStagesManager.hasGameStage(Minecraft.getInstance().player, stage)) {
                return;
            }

            if (sellPrices.containsKey(itemDescription)) {
                int oldAmount = itemsToSell.getOrDefault(itemDescription, 0);

                int count;
                if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                    count = 64;
                } else if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)) {
                    count = 10;
                } else {
                    count = 1;
                }

                if (pMouseButton == 1) {
                    count *= -1;
                }

                int newAmount = Math.max(0, oldAmount + count);

                if (newAmount > 0) {
                    itemsToSell.put(itemDescription, Math.max(0, oldAmount + count));
                } else {
                    itemsToSell.remove(itemDescription);
                }

                this.reduceSellItemsIfMissing();
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pMouseButton == 1 ? 0.75F : 1.25F));
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        initButtons();
        initCategoryPanel();

        if (!this.isEditMode()) {
            shoppingCartButton = this.addRenderableWidget(new ImageButton(this.leftPos + 139, this.topPos + 121, 30, 26, 176, 0, 26, BACKGROUND, 256, 256, (b) -> {
                assert this.minecraft != null;
                this.minecraft.screen = new ShoppingCartScreen(this.menu, this.menu.inventory, this);
                this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
            }, Component.empty()));

            LocalPlayer player = Minecraft.getInstance().player;

            if (player != null && player.isCreative() && player.getPermissionLevel() >= 4) {
                this.addRenderableWidget(Button.builder(Component.translatable("jackseconomy.edit"), (b) -> {
                    Minecraft.getInstance().screen = new EditAdminShopScreen(this.menu, this.menu.inventory, this.title, this.shopItems, this.sellPrices, this.adminShopName);
                    Minecraft.getInstance().screen.init(Minecraft.getInstance(), this.width, this.height);
                }).bounds(3, 3, 75, 20).build());
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        if (!this.shouldRenderBackground) {
            return;
        }

        this.renderBackground(guiGraphics);

        RenderSystem.setShaderTexture(0, BACKGROUND);
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(BACKGROUND, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    private void addItemToCart(CategorizedShopItem shopItem, int amount) {
        int countAlready = shoppingCart.getOrDefault(shopItem, 0);

        int newCount = Math.max(0, countAlready + amount);

        if (newCount == 0) {
            shoppingCart.remove(shopItem);
        } else {
            shoppingCart.put(shopItem, newCount);
        }
    }

    protected boolean isHoverObstructed(int mouseX, int mouseY) {
        return false;
    }

    private static int animationTick;

    public static void renderStar(GuiGraphics guiGraphics, int x, int y) {
        int animationPhase = (animationTick / 3) % 3;
        guiGraphics.blit(STAR, x, y, 160, 0, animationPhase * 16.f, 16, 16, 16, 48);
    }

    public static void renderDollarSignForSellableItems(GuiGraphics guiGraphics, int xOffset, int yOffset, List<Slot> slots, HashMap<ItemDescription, Integer> itemsToSell, HashMap<ItemDescription, ItemSellabilityInfo> sellPrices) {
        for (Slot slot : slots) {
            ItemStack itemStack = slot.getItem();

            if (itemStack.isEmpty()) {
                continue;
            }

            ItemDescription itemDescription = ItemDescription.ofItem(itemStack);

            // Reason for this pose stuff: blocks and stuff get rendered at an offset, so the $ would be rendered behind items/blocks
            if (itemsToSell.getOrDefault(itemDescription, 0) > 0) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 256);
                guiGraphics.drawString(Minecraft.getInstance().font, "$", xOffset + slot.x + 1, yOffset + slot.y + 1, 0xFF00AA00, false);
                guiGraphics.pose().popPose();
            } else if (sellPrices.containsKey(itemDescription)) {
                ItemSellabilityInfo info = sellPrices.get(itemDescription);

                if (info.worth > 0 && (info.stage() == null || GameStagesManager.hasGameStage(Minecraft.getInstance().player, info.stage()))) {
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(0, 0, 256);
                    guiGraphics.drawString(Minecraft.getInstance().font, "$", xOffset + slot.x + 1, yOffset + slot.y + 1, 0xFFAA0000, false);
                    guiGraphics.pose().popPose();
                }
            }
        }
    }


    @Override
    protected void containerTick() {
        super.containerTick();
        animationTick++;
        this.reduceSellItemsIfMissing();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        tooltip = null;

        shouldRenderBackground = true;
        this.renderBg(guiGraphics, pPartialTick, pMouseX, pMouseY);

        if (this.selectedCategory != null) {
            Component component = Component.translatable(this.selectedCategory.name);
            int width = Minecraft.getInstance().font.width(component);

            float scale = 1.f;
            int space = (this.imageWidth / 2) - 4;
            if (width > space) {
                scale = (float) space / width;
                guiGraphics.pose().pushPose();
                guiGraphics.pose().scale(scale, scale, scale);
            }

            guiGraphics.drawCenteredString(this.font, component, (int) ((this.leftPos + (this.imageWidth / 2)) / scale), (int) ((this.topPos + 6) / scale + this.font.lineHeight * (1 - scale)), 0xFFFFFFFF);
            if (width > space) {
                guiGraphics.pose().popPose();
            }
        } else if (isEditMode()) {
            guiGraphics.drawCenteredString(this.font, Component.translatable("jackseconomy.add_category").withStyle(ChatFormatting.RED), this.leftPos + (this.imageWidth / 2), this.topPos + 6, 0xFFFFFFFF);
        }

        if (shoppingCartButton != null && shoppingCartButton.isHovered()) {
            this.tooltip = List.of(Component.translatable("jackseconomy.items", Component.literal(String.valueOf(this.shoppingCart.size()))).withStyle(ChatFormatting.GRAY),
                    Component.translatable("jackseconomy.value", Component.literal(oneItemCurrencyMode ? "$" + getShoppingCartValue().longValue() : CurrencyHelper.format(getShoppingCartValue()))).withStyle(ChatFormatting.GRAY));
        }

        /*
        for (AbstractWidget child : this.categoryPanel.children) {
            if (child instanceof CategoryEntry categoryEntry && categoryEntry.isHovered()) {
                Category category = categoryEntry.getCategory();

                if (category != null && category.name != null && this.newShopUnlocks.unlockedCategories.contains(category.name)) {
                    this.newShopUnlocks.unlockedCategories.remove(category.name);
                    this.acknowledgedShopUnlocks.unlockedCategories.add(category.name);

                    this.acknowledgeCategoryUnlocksForEmpties();
                }
            }
        }*/

        List<InnerCategory> categories = this.getInnerCategories();
        for (int i = 0; i < 8; i++) {
            int categoryId = this.categoryOffset + i;
            int categoryX = this.leftPos + 17 + i * 18, categoryY = this.topPos + 27;
            boolean hovered = pMouseX >= categoryX && pMouseX <= categoryX + 16 && pMouseY >= categoryY && pMouseY <= categoryY + 16;

            if (hovered && !isHoverObstructed(pMouseX, pMouseY)) {
                renderSlotHighlight(guiGraphics, categoryX, categoryY, 0);
            }

            if (categoryId >= 0 && categoryId < categories.size()) {
                InnerCategory category = categories.get(categoryId);
                String innerCategoryName = selectedBigCategory.name + ":" + category.name;
                guiGraphics.renderItem(category.itemDescription.createItemStack(), categoryX, categoryY);

                if (!isEditMode() && newShopUnlocks.unlockedCategories.contains(innerCategoryName)) {
                    renderStar(guiGraphics, categoryX + 4, categoryY + 4);
                }

                if (hovered) {
                    /*
                    if (newShopUnlocks.unlockedCategories.contains(innerCategoryName)) {
                        newShopUnlocks.unlockedCategories.remove(innerCategoryName);
                        acknowledgedShopUnlocks.unlockedCategories.add(innerCategoryName);

                        this.acknowledgeCategoryUnlocksForEmpties();
                    }*/

                    if (this.isEditMode()) {
                        this.tooltip = List.of(Component.translatable(category.name), Component.translatable("jackseconomy.right_click_to_rename").withStyle(ChatFormatting.GRAY), Component.translatable("jackseconomy.middle_click_to_remove_category").withStyle(ChatFormatting.RED));
                    } else {
                        this.tooltip = List.of(Component.translatable(category.name));
                    }
                }

                if (this.selectedCategory == category) {
                    guiGraphics.blit(BACKGROUND, categoryX + 4, categoryY + 19, 176, 52, 10, 6);
                }
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = this.leftPos + 8 + col * 18, slotY = this.topPos + 66 + row * 18;

                ShopItem shopItem = this.getItemAtSlot(this.page * 27 + row * 9 + col, this.selectedCategory);

                if (shopItem != null) {
                    if (!isEditMode() && shopItem.isLocked()) {
                        guiGraphics.blit(QUESTION_MARK, slotX, slotY, 0, 0, 16, 16, 16, 16);
                    } else {
                        ItemStack itemStack = shopItem.itemDescription.createItemStack();
                        guiGraphics.renderItem(itemStack, slotX, slotY);

                        if (!isEditMode() && newShopUnlocks.unlockedItems.contains(new NewShopUnlocks.Item(shopItem.slot, selectedBigCategory.name + ":" + selectedCategory.name))) {
                            renderStar(guiGraphics, slotX + 4, slotY + 4);
                        }
                    }

                    if (isEditMode() && shopItem.stage != null) {
                        guiGraphics.blit(LOCK, slotX + 6, slotY + 5, 160, 0, 0, 12, 12, 12, 12);
                    }
                }

                if (pMouseX >= slotX && pMouseX <= slotX + 16 && pMouseY >= slotY && pMouseY <= slotY + 16 && !isHoverObstructed(pMouseX, pMouseY)) {
                    renderSlotHighlight(guiGraphics, slotX, slotY, 0);

                    if (shopItem != null) {
                        NewShopUnlocks.Item unlockedItem = new NewShopUnlocks.Item(shopItem.slot, selectedBigCategory.name + ":" + selectedCategory.name);

                        if (newShopUnlocks.unlockedItems.contains(unlockedItem)) {
                            newShopUnlocks.unlockedItems.remove(unlockedItem);
                            acknowledgedShopUnlocks.unlockedItems.add(unlockedItem);

                            this.acknowledgeCategoryUnlocksForEmpties();
                        }

                        ItemStack itemStack = shopItem.itemDescription().createItemStack();

                        this.tooltip = new ArrayList<>();

                        if (this.isEditMode() || !shopItem.isLocked() || Config.showNamesForLockedAdminShopItems.get()) {
                            this.tooltip.add(shopItem.customName != null ? Component.literal(shopItem.customName) :  itemStack.getHoverName().copy().withStyle(shopItem.itemDescription.item().getRarity(itemStack).getStyleModifier()));

                            Level level = Minecraft.getInstance().player == null ? null : Minecraft.getInstance().player.level();
                            itemStack.getItem().appendHoverText(itemStack, level, this.tooltip, TooltipFlag.Default.NORMAL);

                            this.tooltip.add(Component.literal(" "));
                        }

                        if (this.isEditMode()) {
                            this.tooltip.add(shopItem.price == -1 ? Component.translatable("jackseconomy.no_price").withStyle(ChatFormatting.GRAY) : Component.literal(oneItemCurrencyMode ? "$" + (long)shopItem.price : CurrencyHelper.format(shopItem.price)).withStyle(ChatFormatting.GRAY));
                            if (GameStagesCheck.isInstalled() && shopItem.stage() != null) {
                                this.tooltip.add(Component.translatable("jackseconomy.locked_behind_stage", shopItem.stage()).withStyle(ChatFormatting.GRAY));
                            }
                            this.tooltip.add(Component.translatable("jackseconomy.right_click_to_edit_price").withStyle(ChatFormatting.AQUA));
                            this.tooltip.add(Component.translatable("jackseconomy.shift_right_click_to_rename").withStyle(ChatFormatting.AQUA));
                            if (GameStagesCheck.isInstalled()) {
                                this.tooltip.add(Component.translatable("jackseconomy.lctrl_right_click_to_change_stage").withStyle(ChatFormatting.AQUA));
                            }
                            this.tooltip.add(Component.translatable("jackseconomy.middle_click_to_remove").withStyle(ChatFormatting.RED));
                        } else {
                            if (shopItem.isLocked()) {
                                this.tooltip.add(Component.translatable("jackseconomy.locked").withStyle(ChatFormatting.GRAY));

                                if (Config.showStageForLockedAdminShopItems.get()) {
                                    this.tooltip.add(Component.translatable("jackseconomy.required_game_stage", Component.literal(shopItem.stage())).withStyle(ChatFormatting.GRAY));
                                }
                            } else {
                                this.tooltip.add(Component.literal(oneItemCurrencyMode ? "$" + (long)shopItem.price : CurrencyHelper.format(shopItem.price)).withStyle(ChatFormatting.GRAY));
                                this.tooltip.add(Component.translatable("jackseconomy.in_cart", Component.literal(String.valueOf(shoppingCart.getOrDefault(new CategorizedShopItem(shopItem, selectedBigCategory.name + ":" + selectedCategory.name), 0))).withStyle(ChatFormatting.AQUA)));
                            }
                        }
                    }
                }
            }
        }

        if (!this.isEditMode()) {
            ItemStack wallet = CuriosWallet.get(Minecraft.getInstance().player);

            if (!Config.oneItemCurrencyMode.get() && wallet != null && wallet.getItem() instanceof WalletItem walletItem) {
                BigDecimal balance = WalletItem.getBalance(wallet);

                Component component = Component.literal(CurrencyHelper.formatShortened(balance));
                int textWidth = this.font.width(component);
                int totalWidth = 29 + textWidth;
                guiGraphics.fill(this.leftPos + 181, this.topPos + 5, this.leftPos + 181 + totalWidth, this.topPos + 35, 0xFF4c4c4c);
                guiGraphics.fill(this.leftPos + 182, this.topPos + 6, this.leftPos + 182 + totalWidth, this.topPos + 34, 0xFFc6c6c6);
                guiGraphics.renderItem(wallet, this.leftPos + 183, this.topPos + 8);
                guiGraphics.drawString(this.font, component, this.leftPos + 203, this.topPos + 13, 0xFFFFFFFF);

                RenderSystem.setShaderTexture(0, BALANCE_PROGRESS);

                int barStartX = this.leftPos + 181 + ((totalWidth - 51) / 2);
                int barStartY = this.topPos + 26;
                double progress = balance.divide(BigDecimal.valueOf(walletItem.getCapacity()), RoundingMode.DOWN).min(BigDecimal.ONE).doubleValue();
                guiGraphics.blit(BALANCE_PROGRESS, barStartX, barStartY, 0, 0, 0, 51, 5, 256, 256);
                guiGraphics.blit(BALANCE_PROGRESS, barStartX, barStartY, 0, 0, 5, ((int)(51 * progress)), 5, 256, 256);

                if (pMouseX >= barStartX && pMouseX <= barStartX + 51 && pMouseY >= barStartY && pMouseY <= barStartY + 5) {
                    tooltip = List.of(Component.translatable("jackseconomy.balance_out_of", Component.literal(CurrencyHelper.format(balance)).withStyle(ChatFormatting.YELLOW), Component.literal(CurrencyHelper.format(walletItem.getCapacity()))).withStyle(ChatFormatting.GOLD));
                }
            } else if (Config.oneItemCurrencyMode.get()) {
                long balance = OIMWalletItem.getTotalDollars(wallet, Minecraft.getInstance().player);

                Component component = Component.literal("$" + balance);
                int textWidth = this.font.width(component);
                int totalWidth = 29 + textWidth;
                guiGraphics.fill(this.leftPos + 181, this.topPos + 5, this.leftPos + 181 + totalWidth, this.topPos + 27, 0xFF4c4c4c);
                guiGraphics.fill(this.leftPos + 182, this.topPos + 6, this.leftPos + 182 + totalWidth, this.topPos + 26, 0xFFc6c6c6);
                guiGraphics.renderItem(wallet != null && !wallet.isEmpty() ? wallet : new ItemStack(ItemBlockReg.WALLET_ITEM.get()), this.leftPos + 183, this.topPos + 8);
                guiGraphics.drawString(this.font, component, this.leftPos + 203, this.topPos + 13, 0xFFFFFFFF);
            } else {
                Component component = Component.translatable("jackseconomy.no_wallet").withStyle(ChatFormatting.DARK_RED);
                int width = this.font.width(component);
                guiGraphics.fill(this.leftPos + 181, this.topPos + 5, this.leftPos + 209 + width, this.topPos + 25, 0xFF4c4c4c);
                guiGraphics.fill(this.leftPos + 182, this.topPos + 6, this.leftPos + 208 + width, this.topPos + 24, 0xFFc6c6c6);
                guiGraphics.blit(NO_WALLET, this.leftPos + 183, this.topPos + 8, 0, 0, 0, 16, 16, 16, 16);
                guiGraphics.drawString(Minecraft.getInstance().font, component, this.leftPos + 203, this.topPos + 13, 0xFFFFFFFF, false);
            }

        }

        this.shouldRenderBackground = false;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 256);
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
        renderDollarSignForSellableItems(guiGraphics, this.leftPos, this.topPos, this.menu.slots, this.itemsToSell, this.sellPrices);
        guiGraphics.pose().popPose();
        this.renderStageTwo(guiGraphics, pMouseX, pMouseY, pPartialTick);

        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack itemstack = this.hoveredSlot.getItem();

            List<Component> tooltip = new ArrayList<>(this.getTooltipFromContainerItem(itemstack));

            ItemDescription itemDescription = ItemDescription.ofItem(itemstack);
            ItemSellabilityInfo info = sellPrices.get(itemDescription);
            String stage = info != null ? info.stage : null;
            double price = info != null ? info.worth : -1.0;

            if (!Config.disableAdminShopSelling.get()) {
                if (this.isEditMode()) {
                    tooltip.add(Component.literal(" "));

                    if (price != -1) {
                        tooltip.add(Component.literal(oneItemCurrencyMode ? "$" + (long)price : CurrencyHelper.format(price)).withStyle(ChatFormatting.GRAY));
                    } else {
                        tooltip.add(Component.translatable("jackseconomy.no_sell_price").withStyle(ChatFormatting.GRAY));
                    }

                    if (stage != null && GameStagesCheck.isInstalled()) {
                        tooltip.add(Component.translatable("jackseconomy.required_game_stage", stage).withStyle(ChatFormatting.GRAY));
                    }

                    tooltip.add(Component.translatable("jackseconomy.right_click_to_edit_price").withStyle(ChatFormatting.AQUA));

                    if (GameStagesCheck.isInstalled()) {
                        tooltip.add(Component.translatable("jackseconomy.shift_right_click_to_edit_stage").withStyle(ChatFormatting.AQUA));
                    }

                    if (price != -1) {
                        tooltip.add(Component.translatable("jackseconomy.middle_click_to_remove_sell_price").withStyle(ChatFormatting.RED));
                    }

                    if (stage != null && GameStagesCheck.isInstalled()) {
                        tooltip.add(Component.translatable("jackseconomy.shift_middle_click_to_remove_stage").withStyle(ChatFormatting.RED));
                    }
                } else {
                    tooltip.add(Component.literal(" "));

                    if (stage != null && !GameStagesManager.hasGameStage(Minecraft.getInstance().player, stage)) {
                        if (Config.showStageForLockedSellItems.get()) {
                            tooltip.add(Component.translatable("jackseconomy.selling_locked_behind_gamestage", stage).withStyle(ChatFormatting.GRAY));
                        } else {
                            tooltip.add(Component.translatable("jackseconomy.selling_locked").withStyle(ChatFormatting.GRAY));
                        }
                    } else {
                        if (price != -1) {
                            tooltip.add(Component.literal(oneItemCurrencyMode ? "$" + (long)price : CurrencyHelper.format(price)).withStyle(ChatFormatting.GRAY));
                            tooltip.add(Component.translatable("jackseconomy.to_sell", Component.literal(String.valueOf(itemsToSell.getOrDefault(itemDescription, 0))).withStyle(ChatFormatting.AQUA)));
                        } else {
                            tooltip.add(Component.translatable("jackseconomy.no_sell_price").withStyle(ChatFormatting.GRAY));
                        }
                    }
                }
            }
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 256);
            guiGraphics.renderTooltip(this.font, tooltip, itemstack.getTooltipImage(), itemstack, pMouseX, pMouseY);
            guiGraphics.pose().popPose();
        }

        if (tooltip != null) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 256);
            guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(), pMouseX, pMouseY);
            guiGraphics.pose().popPose();
        }
    }

    public boolean hasAnyItemUnlocked(List<AdminShopScreen.ShopItem> shopItems) {
        boolean anyUnlocked = shopItems.isEmpty();
        for (ShopItem shopItem : shopItems) {
            if (!shopItem.isLocked()) {
                anyUnlocked = true;
                break;
            }
        }

        return anyUnlocked;
    }

    protected List<InnerCategory> getInnerCategories(Category category) {
        // TODO: override this in EditAdminShopScreen to show all
        return this.shopItems.get(category).entrySet().stream().filter(entry -> hasAnyItemUnlocked(entry.getValue())).map(Map.Entry::getKey).toList();
    }

    protected List<InnerCategory> getInnerCategories() {
        return this.getInnerCategories(this.selectedBigCategory);
    }

    protected void renderStageTwo(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {}

    protected boolean onCategorySlotClicked(int pButton, int categoryId) {
        if (pButton == 0) {
            List<InnerCategory> categories = this.getInnerCategories();

            if (categoryId >= 0 && categoryId < categories.size()) {
                this.selectedCategory = categories.get(categoryId);
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.25F));
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (int i = 0; i < 8; i++) {
            int categoryId = this.categoryOffset + i;
            int categoryX = this.leftPos + 17 + i * 18, categoryY = this.topPos + 27;

            if (pMouseX >= categoryX && pMouseX <= categoryX + 16 && pMouseY >= categoryY && pMouseY <= categoryY + 16) {
                if (onCategorySlotClicked(pButton, categoryId)) {
                    return true;
                }
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = this.leftPos + 8 + col * 18, slotY = this.topPos + 66 + row * 18;

                if (pMouseX >= slotX && pMouseX <= slotX + 16 && pMouseY >= slotY && pMouseY <= slotY + 16) {
                    this.onSlotClicked(this.page * 27 + row * 9 + col, pButton);
                }
            }
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    protected @Nullable ShopItem getItemAtSlot(int slot, InnerCategory category) {
        for (ShopItem shopItem : this.shopItems.get(selectedBigCategory).getOrDefault(category, List.of())) {
            if (shopItem.slot == slot) {
                return shopItem;
            }
        }

        return null;
    }

    // Returns old item
    protected ShopItem setItemAtSlot(ShopItem shopItem, int slot, InnerCategory category) {

        if (!this.shopItems.get(selectedBigCategory).containsKey(category)) {
            return shopItem;
        }

        ShopItem existingItem = getItemAtSlot(slot, category);

        if (existingItem != null) {
            this.shopItems.get(selectedBigCategory).get(category).remove(existingItem);
        }

        if (shopItem != null) {
            this.shopItems.get(selectedBigCategory).get(category).add(new ShopItem(shopItem.itemDescription, shopItem.price, slot, shopItem.customName, shopItem.stage));
        }

        return existingItem;
    }

    protected void onSlotClicked(int slot, int pButton) {
        if (pButton != 0 && pButton != 1) {
            return;
        }

        ShopItem shopItem = getItemAtSlot(slot, this.selectedCategory);

        if (shopItem == null) {
            return;
        }

        if (shopItem.isLocked()) {
            return;
        }

        int count;
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
            count = 64;
        } else if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)) {
            count = 10;
        } else {
            count = 1;
        }

        if (pButton == 1) {
            count *= -1;
        }

        this.addItemToCart(new CategorizedShopItem(shopItem, this.selectedBigCategory.name + ":" + this.selectedCategory.name), count);
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pButton == 1 ? 0.75F : 1.25F));
    }

    protected int findFirstAvailableSlot(InnerCategory category) {
        for (int slot = 0;;slot++) {
            ShopItem existingShopItem = this.getItemAtSlot(slot, category);

            if (existingShopItem == null) {
                return slot;
            }
        }
    }

    protected Pair<Integer, Integer> getSlotPos(int slot) {
        int slotInPage = slot % 27;
        int row = slotInPage / 9;
        int col = slotInPage % 9;
        return Pair.of(this.leftPos + 8 + col * 18, this.topPos + 66 + row * 18);
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;

        if (!this.shoppingCart.isEmpty()) {
            this.minecraft.screen = new AdminShopExitPromptScreen(this);
            this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
        } else {
            super.onClose();
            this.sendShopUnlocksAcknowledgements();
        }
    }

    public void sendShopUnlocksAcknowledgements() {
        if (!acknowledgedShopUnlocks.isEmpty()) {
            Packets.sendToServer(new AcknowledgeUnlocksPacket(acknowledgedShopUnlocks));
            acknowledgedShopUnlocks.unlockedCategories.clear();
            acknowledgedShopUnlocks.unlockedItems.clear();
        }
    }

    protected boolean isEditMode() {
        return false;
    }

    public static class InnerCategory {
        protected String name;
        protected ItemDescription itemDescription;

        public InnerCategory(String name, ItemDescription description) {
            this.name = name;
            this.itemDescription = description;
        }

        public String getName() {
            return name;
        }

        public ItemDescription getItemDescription() {
            return itemDescription;
        }
    }

    public static class Category extends InnerCategory {
        public Category(String name, ItemDescription item) {
            super(name, item);
        }
    }

    public static class ShopItem {
        private final ItemDescription itemDescription;
        private final double price;
        private final int slot;
        private final String customName;
        private final String stage;

        public ShopItem(@NotNull ItemDescription itemDescription, double price, int slot, @Nullable String customName, @Nullable String stage) {
            this.itemDescription = itemDescription;
            this.price = price;
            this.slot = slot;
            this.customName = customName;
            this.stage = stage;
        }

        public ItemDescription itemDescription() {
            return itemDescription;
        }

        public double price() {
            return price;
        }

        public int slot() {
            return slot;
        }

        public String customName() {
            return customName;
        }

        public String stage() {
            return stage;
        }

        public boolean isLocked() {
            return this.stage != null && !GameStagesManager.hasGameStage(Minecraft.getInstance().player, this.stage);
        }

        @Override
        public int hashCode() {
            return Objects.hash(itemDescription, price, slot, customName, stage);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            ShopItem that = (ShopItem) obj;
            return Double.compare(that.price, price) == 0 &&
                    slot == that.slot &&
                    Objects.equals(itemDescription, that.itemDescription) &&
                    Objects.equals(customName, that.customName) &&
                    Objects.equals(stage, that.stage);
        }
    }

    public static class CategorizedShopItem extends ShopItem {
        private final String category;

        public CategorizedShopItem(@NotNull ItemDescription itemDescription, double price, int slot, @Nullable String customName, @Nullable String stage, String category) {
            super(itemDescription, price, slot, customName, stage);
            this.category = category;
        }

        public CategorizedShopItem(ShopItem shopItem, String category) {
            super(shopItem.itemDescription(), shopItem.price(), shopItem.slot(), shopItem.customName(), shopItem.stage());
            this.category = category;
        }

        public String category() {
            return category;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), category);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            if (!super.equals(obj)) {
                return false;
            }
            CategorizedShopItem that = (CategorizedShopItem) obj;
            return Objects.equals(category, that.category);
        }
    }

    public record ItemSellabilityInfo(double worth, String stage) { }
    public record UnpreparedShopItem(@NotNull ItemDescription itemDescription, double price, @Nullable String customName, @Nullable String stage) { }
}