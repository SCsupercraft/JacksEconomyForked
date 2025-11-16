package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import me.khajiitos.jackseconomy.gamestages.GameStagesCheck;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.menu.AdminShopMenu;
import me.khajiitos.jackseconomy.packet.UpdateAdminShopPacket;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import me.khajiitos.jackseconomy.screen.widget.BetterScrollPanel;
import me.khajiitos.jackseconomy.screen.widget.EditCategoryEntry;
import me.khajiitos.jackseconomy.screen.widget.FloatingEditBoxWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.*;

public class EditAdminShopScreen extends AdminShopScreen {
    protected ShopItem itemOnCursor;
    protected FloatingEditBoxWidget floatingEditBox;

    public EditAdminShopScreen(AdminShopMenu pMenu, Inventory pPlayerInventory, Component pTitle, LinkedHashMap<Category, LinkedHashMap<InnerCategory, List<ShopItem>>> shopItems, HashMap<ItemDescription, ItemSellabilityInfo> sellPrices, @Nullable String adminShopName) {
        super(pMenu, pPlayerInventory, pTitle, shopItems, sellPrices, adminShopName);

        pMenu.setSlotsLocked(true);
    }

    @Override
    protected boolean isHoverObstructed(int mouseX, int mouseY) {
        return this.floatingEditBox != null && mouseX >= this.floatingEditBox.getX() && mouseX <= this.floatingEditBox.getX() + this.floatingEditBox.getWidth() && mouseY >= this.floatingEditBox.getY() && mouseY <= this.floatingEditBox.getY() + this.floatingEditBox.getWidth();
    }

    @Override
    protected void selectBigCategory(Category bigCategory) {
        super.selectBigCategory(bigCategory);

        if (this.floatingEditBox != null) {
            this.removeWidget(this.floatingEditBox);
            this.floatingEditBox = null;
        }
    }

    @Override
    protected void initCategoryPanel() {
        if (this.categoryPanel != null) {
            this.removeWidget(this.categoryPanel);
        }

        BetterScrollPanel oldPanel = this.categoryPanel;
        this.categoryPanel = this.addRenderableWidget(new BetterScrollPanel(Minecraft.getInstance(), this.leftPos - 80, this.topPos + 20, 75, this.imageHeight - 40));

        for (Category category : shopItems.keySet()) {

            this.categoryPanel.children.add(new EditCategoryEntry(0, 0, 75, 25, category, (categoryEntry, button) -> {
                if (button == 0) {
                    if (this.itemOnCursor != null) {
                        category.itemDescription = this.itemOnCursor.itemDescription();
                        this.itemOnCursor = null;
                    } else {
                        this.selectBigCategory(category);
                    }
                } else if (button == 1) {
                    if (this.floatingEditBox != null) {
                        this.removeWidget(this.floatingEditBox);
                        this.floatingEditBox = null;
                    }

                    this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, categoryEntry.getX() + 37, categoryEntry.getY() + 20, 75, 15, (value) -> {
                        for (Category otherCategory : this.shopItems.keySet()) {
                            if (this.shopItems.size() > 1 && otherCategory == category) {
                                continue;
                            }

                            // Category names have to be unique
                            if (otherCategory.name.equals(value)) {
                                return;
                            }

                            category.name = value;

                            if (this.floatingEditBox != null) {
                                this.removeWidget(this.floatingEditBox);
                                this.floatingEditBox = null;
                            }

                            return;
                        }
                    }));
                    this.setFocused(this.floatingEditBox);
                } else if (button == 2) {
                    this.shopItems.remove(category);

                    if (this.selectedCategory == category) {
                        this.selectedCategory = null;
                        for (Category category1 : this.shopItems.keySet()) {
                            this.selectBigCategory(category1);
                            break;
                        }

                        if (this.selectedCategory == null) {
                            this.selectBigCategory(null);
                        }
                    }

                    this.initCategoryPanel();
                }
            }, () -> this.selectedBigCategory == category, () -> tooltip = List.of(Component.translatable("jackseconomy.right_click_to_rename").withStyle(ChatFormatting.AQUA), Component.translatable("jackseconomy.middle_click_to_remove_category").withStyle(ChatFormatting.RED)), this.categoryPanel));
        }

        this.categoryPanel.children.add(new EditCategoryEntry(0, 0, 75, 25, null, (categoryEntry, button) -> {
            if (button == 0 && this.itemOnCursor != null) {
                Category category = new Category(getUnnamedCategoryName(), this.itemOnCursor.itemDescription());
                this.shopItems.put(category, new LinkedHashMap<>());
                this.initCategoryPanel();
                this.itemOnCursor = null;
            }
        }, () -> false, () -> tooltip = List.of(Component.translatable("jackseconomy.drop_item_to_create_category")), this.categoryPanel));

        if (categoryPanel.isScrollbarDisplayed()) {
            for (AbstractWidget widget: categoryPanel.children) {
                widget.setWidth(67);
            }
        }

        // Makes the list not scroll all the way up when adding new categories
        if (oldPanel != null) {
            this.categoryPanel.setScrollDistance(oldPanel.getScrollDistance(), true);
        }
    }

    protected List<InnerCategory> getInnerCategories(Category category) {
        return this.shopItems.get(category).keySet().stream().toList();
    }

    protected String getUnnamedInnerCategoryName() {
        List<InnerCategory> categories = this.getInnerCategories();
        int i = -1;
        while (true) {
            String name = "Unnamed" + (i == -1 ? "" : " " + i);
            boolean exists = false;

            for (InnerCategory category : categories) {
                if (category.name.equals(name)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                return name;
            }

            i++;
        }
    }

    protected String getUnnamedCategoryName() {
        List<Category> categories = this.shopItems.keySet().stream().toList();
        int i = -1;
        while (true) {
            String name = "Unnamed" + (i == -1 ? "" : " " + i);
            boolean exists = false;

            for (InnerCategory category : categories) {
                if (category.name.equals(name)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                return name;
            }

            i++;
        }
    }

    @Override
    protected boolean onCategorySlotClicked(int pButton, int categoryId) {
        if (pButton == 0) {
            List<InnerCategory> categories = this.getInnerCategories();

            if (categoryId == categories.size() && this.itemOnCursor != null && this.floatingEditBox == null) {
                InnerCategory category = new InnerCategory(this.getUnnamedInnerCategoryName(), this.itemOnCursor.itemDescription());

                if (this.selectedCategory == null) {
                    this.selectedCategory = category;
                }

                this.shopItems.get(selectedBigCategory).put(category, new ArrayList<>());

                int categoryRenderId = categoryId - this.categoryOffset;
                int categoryX = this.leftPos + 17 + categoryRenderId * 18, categoryY = this.topPos + 27;

                this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, categoryX + 8, categoryY + 18, 50, 15, (value) -> {
                    for (InnerCategory otherCategory : this.shopItems.keySet()) {
                        if (otherCategory == category) {
                            continue;
                        }

                        // Category names have to be unique
                        if (otherCategory.name.equals(value)) {
                            return;
                        }

                        category.name = value;

                        if (this.floatingEditBox != null) {
                            this.removeWidget(this.floatingEditBox);
                            this.floatingEditBox = null;
                        }

                        return;
                    }
                }));
                //this.floatingEditBox.setValue(category.getName());
                this.setFocused(this.floatingEditBox);

                this.itemOnCursor = null;
                return true;
            }
        } else if (pButton == 1) {
            int categoryRenderId = categoryId - this.categoryOffset;
            int categoryX = this.leftPos + 17 + categoryRenderId * 18, categoryY = this.topPos + 27;

            InnerCategory category = this.getCategoryById(categoryId);
            if (category != null && this.floatingEditBox == null) {
                this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, categoryX + 8, categoryY + 18, 50, 15, (value) -> {
                    category.name = value;
                    this.removeWidget(this.floatingEditBox);
                    this.floatingEditBox = null;
                }));
                this.setFocused(this.floatingEditBox);
            }
        } else if (pButton == 2) {
            InnerCategory category = this.getCategoryById(categoryId);

            if (category != null) {
                this.shopItems.get(selectedBigCategory).remove(category);

                if (this.selectedCategory == category) {
                    this.selectedCategory = null;
                }
            }
        }
        return super.onCategorySlotClicked(pButton, categoryId);
    }

    @Override
    protected void onSlotClicked(int slot, int pButton) {
        if (this.selectedCategory == null) {
            return;
        }
        if (pButton == 0) {
            ShopItem onCursorBefore = this.itemOnCursor;
            if (onCursorBefore != null && onCursorBefore.price() == -1 && this.floatingEditBox == null) {
                Pair<Integer, Integer> slotPos = this.getSlotPos(slot);

                this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, slotPos.getFirst() + 8, slotPos.getSecond() + 18, 50, 15, true, (value) -> {
                    try {
                        double newPrice = Double.parseDouble(value);

                        this.setItemAtSlot(new ShopItem(onCursorBefore.itemDescription(), newPrice, slot, onCursorBefore.customName(), onCursorBefore.stage()), slot, this.selectedCategory);
                        this.removeWidget(this.floatingEditBox);
                        this.floatingEditBox = null;
                    } catch (NumberFormatException ignored) {}
                }));
                this.setFocused(this.floatingEditBox);
            }
            this.itemOnCursor = this.setItemAtSlot(this.itemOnCursor, slot, this.selectedCategory);
        } else if (pButton == 1) {
            if (this.floatingEditBox != null) {
                this.removeWidget(this.floatingEditBox);
                this.floatingEditBox = null;
                return;
            }

            ShopItem itemAtSlot = this.getItemAtSlot(slot, this.selectedCategory);

            if (itemAtSlot == null) {
                return;
            }

            Pair<Integer, Integer> slotPos = this.getSlotPos(slot);

            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, slotPos.getFirst() + 8, slotPos.getSecond() + 16, 50, 15, (value) -> {
                    if (!value.isEmpty()) {
                        this.setItemAtSlot(new ShopItem(itemAtSlot.itemDescription(), itemAtSlot.price(), slot, value, itemAtSlot.stage()), slot, this.selectedCategory);
                    } else {
                        this.setItemAtSlot(new ShopItem(itemAtSlot.itemDescription(), itemAtSlot.price(), slot, null, itemAtSlot.stage()), slot, this.selectedCategory);
                    }
                    this.removeWidget(this.floatingEditBox);
                    this.floatingEditBox = null;
                }));

                if (itemAtSlot.customName() != null) {
                    this.floatingEditBox.setValue(itemAtSlot.customName());
                }
            } else if (GameStagesCheck.isInstalled() && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)) {
                this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, slotPos.getFirst() + 8, slotPos.getSecond() + 16, 50, 15, (value) -> {
                    if (!value.isEmpty()) {
                        this.setItemAtSlot(new ShopItem(itemAtSlot.itemDescription(), itemAtSlot.price(), slot, itemAtSlot.customName(), value), slot, this.selectedCategory);
                    } else {
                        this.setItemAtSlot(new ShopItem(itemAtSlot.itemDescription(), itemAtSlot.price(), slot, itemAtSlot.customName(), null), slot, this.selectedCategory);
                    }
                    this.removeWidget(this.floatingEditBox);
                    this.floatingEditBox = null;
                }));

                if (itemAtSlot.stage() != null) {
                    this.floatingEditBox.setValue(itemAtSlot.stage());
                }
            } else {
                this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, slotPos.getFirst() + 8, slotPos.getSecond() + 16, 50, 15, true, (value) -> {
                    try {
                        double newPrice = Double.parseDouble(value);

                        this.setItemAtSlot(new ShopItem(itemAtSlot.itemDescription(), newPrice, slot, itemAtSlot.customName(), itemAtSlot.stage()), slot, this.selectedCategory);
                        this.removeWidget(this.floatingEditBox);
                        this.floatingEditBox = null;
                    } catch (NumberFormatException ignored) {}
                }));
                this.floatingEditBox.setValue(String.format(Locale.US, "%.2f", itemAtSlot.price()));
            }
            this.setFocused(this.floatingEditBox);
        } else if (pButton == 2) {
            ShopItem itemAtSlot = this.getItemAtSlot(slot, this.selectedCategory);

            if (itemAtSlot == null) {
                return;
            }

            this.setItemAtSlot(null, slot, this.selectedCategory);
        }
    }

    @Override
    protected boolean isEditMode() {
        return true;
    }

    public CompoundTag toAdminShopUpdateCompound() {
        CompoundTag tag = new CompoundTag();

        ListTag itemsTag = new ListTag();
        ListTag categoriesTag = new ListTag();

        HashMap<ItemDescription, List<CompoundTag>> tagsForItems = new HashMap<>();

        this.shopItems.forEach((category, innerCategories) -> {
            CompoundTag categoryTag = new CompoundTag();
            categoryTag.putString("name", category.name);
            categoryTag.put("item", category.itemDescription.toNbt());

            ListTag innerCategoriesTag = new ListTag();

            for (Map.Entry<InnerCategory, List<ShopItem>> entry : innerCategories.entrySet()) {
                CompoundTag innerCategoryTag = new CompoundTag();
                innerCategoryTag.putString("name", entry.getKey().name);
                innerCategoryTag.put("item", entry.getKey().itemDescription.toNbt());

                for (ShopItem shopItem : entry.getValue()) {
                    CompoundTag itemTag = shopItem.itemDescription().toNbt();
                    itemTag.putDouble("adminShopBuyPrice", shopItem.price());

                    itemTag.putString("category", category.name + ":" + entry.getKey().name);
                    itemTag.putInt("slot", shopItem.slot());

                    if (shopItem.customName() != null) {
                        itemTag.putString("customAdminShopName", shopItem.customName());
                    }

                    if (shopItem.stage() != null) {
                        itemTag.putString("adminShopStage", shopItem.stage());
                    }

                    tagsForItems.computeIfAbsent(shopItem.itemDescription(), (a) -> new ArrayList<>()).add(itemTag);
                    //itemsTag.add(itemTag);
                }

                innerCategoriesTag.add(innerCategoryTag);
            }

            categoryTag.put("categories", innerCategoriesTag);
            categoriesTag.add(categoryTag);
        });

        sellPrices.forEach((itemDescription, info) -> {
            if (info.worth() > 0) {
                CompoundTag itemTag = itemDescription.toNbt();
                itemTag.putDouble("adminShopSellPrice", info.worth());

                if (info.stage() != null) {
                    itemTag.putString("adminShopSellStage", info.stage());
                }
                tagsForItems.computeIfAbsent(itemDescription, (a) -> new ArrayList<>()).add(itemTag);
            }
        });

        tagsForItems.forEach(((itemDescription, compoundTags) -> itemsTag.addAll(compoundTags)));

        tag.put("items", itemsTag);
        tag.put("categories", categoriesTag);

        return tag;
    }

    private void sendChanges() {
        Packets.sendToServer(new UpdateAdminShopPacket(this.toAdminShopUpdateCompound(), this.adminShopName));
    }

    @Override
    public void renderStageTwo(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderStageTwo(guiGraphics, pMouseX, pMouseY, pPartialTick);

        if (this.itemOnCursor != null) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 512);
            guiGraphics.renderItem(this.itemOnCursor.itemDescription().createItemStack(), pMouseX - 8, pMouseY - 8);
            guiGraphics.pose().popPose();
        }
    }

    @Override
    protected void slotClicked(@Nullable Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
        if (pMouseButton == 0) {
            if (this.itemOnCursor == null && pSlot != null) {
                ItemStack itemStack = pSlot.getItem();
                if (!itemStack.isEmpty()) {
                    this.itemOnCursor = new ShopItem(ItemDescription.ofItem(itemStack), -1, -1, null, null);
                }
            } else if (pSlot == null) {
                this.itemOnCursor = null;

                if (this.floatingEditBox != null) {
                    this.removeWidget(this.floatingEditBox);
                    this.floatingEditBox = null;
                }
            }
        } else if (pMouseButton == 1 && pSlot != null) {
            if (this.floatingEditBox != null) {
                this.removeWidget(this.floatingEditBox);
                this.floatingEditBox = null;
            }
            ItemStack itemStack = pSlot.getItem();
            if (!itemStack.isEmpty()) {
                ItemDescription itemDescription = ItemDescription.ofItem(itemStack);
                if (GameStagesCheck.isInstalled() && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                    this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, this.leftPos + pSlot.x + 8, this.topPos + pSlot.y + 16, 50, 15, (value) -> {
                        if (sellPrices.containsKey(itemDescription)) {
                            sellPrices.put(itemDescription, new ItemSellabilityInfo(sellPrices.get(itemDescription).worth(), value));
                        } else {
                            sellPrices.put(itemDescription, new ItemSellabilityInfo(-1.0, value));
                        }

                        this.removeWidget(this.floatingEditBox);
                        this.floatingEditBox = null;
                    }));

                    if (sellPrices.containsKey(itemDescription)) {
                        this.floatingEditBox.setValue(sellPrices.get(itemDescription).stage());
                    }

                    this.setFocused(this.floatingEditBox);
                } else {
                    this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, this.leftPos + pSlot.x + 8, this.topPos + pSlot.y + 16, 50, 15, true, (value) -> {
                        try {
                            double newPrice = Double.parseDouble(value);

                            if (sellPrices.containsKey(itemDescription)) {
                                sellPrices.put(itemDescription, new ItemSellabilityInfo(newPrice, sellPrices.get(itemDescription).stage()));
                            } else {
                                sellPrices.put(itemDescription, new ItemSellabilityInfo(newPrice, null));
                            }

                            this.removeWidget(this.floatingEditBox);
                            this.floatingEditBox = null;
                        } catch (NumberFormatException ignored) {}
                    }));

                    if (sellPrices.containsKey(itemDescription)) {
                        this.floatingEditBox.setValue(String.format(Locale.US, "%.2f", sellPrices.get(itemDescription).worth()));
                    }

                    this.setFocused(this.floatingEditBox);
                }
            }
        } else if (pMouseButton == 2 && pSlot != null) {
            ItemStack itemStack = pSlot.getItem();

            if (itemStack.isEmpty()) {
                return;
            }

            ItemDescription itemDescription = ItemDescription.ofItem(itemStack);
            if (GameStagesCheck.isInstalled() && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                ItemSellabilityInfo info = sellPrices.get(itemDescription);

                if (info != null && info.stage() != null) {
                    sellPrices.put(itemDescription, new ItemSellabilityInfo(info.worth(), null));
                }
            } else {
                sellPrices.remove(itemDescription);
            }
        }
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (this.getFocused() instanceof FloatingEditBoxWidget) {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
            //return editBoxWidget.keyPressed(pKeyCode, pScanCode, pModifiers);
        }

        if (pKeyCode == GLFW.GLFW_KEY_LEFT) {
            if (this.getFocused() instanceof AbstractWidget widget && this.renderables.contains(widget)) {
                return false;
            }

            int categoryIndex = this.shopItems.get(selectedBigCategory).keySet().stream().toList().indexOf(this.selectedCategory);

            if (categoryIndex == -1 || categoryIndex == 0) {
                return false;
            }

            List<ShopItem> items = this.shopItems.get(selectedBigCategory).remove(this.selectedCategory);

            this.addAtIndex(this.shopItems.get(selectedBigCategory), this.selectedCategory, items, categoryIndex - 1);
            return true;
        } else if (pKeyCode == GLFW.GLFW_KEY_RIGHT) {
            if (this.getFocused() instanceof AbstractWidget widget && this.renderables.contains(widget)) {
                return false;
            }

            int categoryIndex = this.shopItems.get(selectedBigCategory).keySet().stream().toList().indexOf(this.selectedCategory);

            if (categoryIndex == -1 || categoryIndex == this.shopItems.size() - 1) {
                return false;
            }

            List<ShopItem> items = this.shopItems.get(selectedBigCategory).remove(this.selectedCategory);

            this.addAtIndex(this.shopItems.get(selectedBigCategory), this.selectedCategory, items, categoryIndex + 1);
            return true;
        } else {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }

    @Override
    public void onClose() {
        this.sendChanges();
        super.onClose();
    }
}
