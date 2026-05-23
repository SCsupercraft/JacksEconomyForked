package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import me.khajiitos.jackseconomy.gamestages.GameStagesCheck;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import me.khajiitos.jackseconomy.packet.RequestAdminShopSchemaPacket;
import me.khajiitos.jackseconomy.packet.UpdateAdminShopPacket;
import me.khajiitos.jackseconomy.screen.widget.BetterScrollPanel;
import me.khajiitos.jackseconomy.screen.widget.EditCategoryEntry;
import me.khajiitos.jackseconomy.screen.widget.FloatingEditBoxWidget;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.*;

public class BulkAdminShopScreen extends ItemSelectionScreen<BulkAdminShopScreen.Menu> {
	private static final ResourceLocation SLOT = ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "textures/gui/bulk_admin_shop_opts.png");
	protected @Nullable String adminShopName;
	protected AdminShopScreen.Category category;
	protected AdminShopScreen.InnerCategory innerCategory;
	protected int itemsWithBuyPrices = 0;
	protected int itemsWithSellPrices = 0;
	protected int itemsWithBuyStage = 0;
	protected int itemsWithSellStage = 0;
	protected final LinkedHashMap<AdminShopScreen.Category, LinkedHashMap<AdminShopScreen.InnerCategory, List<AdminShopScreen.ShopItem>>> shopItems = new LinkedHashMap<>();
	protected final HashMap<ItemDescription, AdminShopScreen.ItemSellabilityInfo> sellPrices = new HashMap<>();
	public final boolean oneItemCurrencyMode;
	protected BetterScrollPanel categoryPanel;
	protected boolean categoryHovered = false;
	protected boolean selectingInnerCategory = false;

	public BulkAdminShopScreen(Menu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.oneItemCurrencyMode = menu.oneItemCurrencyMode;
	}

	@Override
	protected void init() {
		super.init();

		updateOpts();
		requestShopData();
	}

	@Override
	public void onClose() {
		this.sendChanges();
		super.onClose();
	}

	@Override
	protected void refreshItemsWith() {
		itemsWithBuyPrices = 0;
		itemsWithSellPrices = 0;
		itemsWithBuyStage = 0;
		itemsWithSellStage = 0;
		super.refreshItemsWith();
	}

	@Override
	protected void removeItemsWith(ItemDescription description) {
		Optional<AdminShopScreen.ShopItem> shopItem = this.shopItems.get(category).get(innerCategory).stream().filter(item -> item.itemDescription().equals(description)).findFirst();
		AdminShopScreen.ItemSellabilityInfo sellabilityInfo = this.sellPrices.get(description);

		boolean hasBuyPrice = shopItem.isPresent();
		boolean hasSellPrice = sellabilityInfo != null;
		boolean hasBuyStage = hasBuyPrice && shopItem.orElseThrow().stage() != null;
		boolean hasSellStage = hasSellPrice && sellabilityInfo.stage() != null;

		if (hasBuyPrice) itemsWithBuyPrices--;
		if (hasSellPrice) itemsWithSellPrices--;
		if (hasBuyStage) itemsWithBuyStage--;
		if (hasSellStage) itemsWithSellStage--;
	}

	@Override
	protected void addItemsWith(ItemDescription description) {
		Optional<AdminShopScreen.ShopItem> shopItem = this.shopItems.get(category).get(innerCategory).stream().filter(item -> item.itemDescription().equals(description)).findFirst();
		AdminShopScreen.ItemSellabilityInfo sellabilityInfo = this.sellPrices.get(description);

		boolean hasBuyPrice = shopItem.isPresent();
		boolean hasSellPrice = sellabilityInfo != null;
		boolean hasBuyStage = hasBuyPrice && shopItem.orElseThrow().stage() != null;
		boolean hasSellStage = hasSellPrice && sellabilityInfo.stage() != null;

		if (hasBuyPrice) itemsWithBuyPrices++;
		if (hasSellPrice) itemsWithSellPrices++;
		if (hasBuyStage) itemsWithBuyStage++;
		if (hasSellStage) itemsWithSellStage++;
	}

	@Override
	protected void onSelectedSlotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
		boolean ctrl = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL);
		boolean shift = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT);
		switch (pMouseButton) {
			case 1 -> { // Right-click
				if (shift) {
					if (ctrl) setBuyStage(pSlot); else setSellStage(pSlot);
				} else if (ctrl) setBuyPrice(pSlot); else setSellPrice(pSlot);
				return;
			}
			case 2 -> { // Middle-click
				if (shift) {
					if (ctrl) removeBuyStage(pSlot); else removeSellStage(pSlot);
				} else if (ctrl) removeBuyPrice(pSlot); else removeSellPrice(pSlot);
				return;
			}
		}
		super.onSelectedSlotClicked(pSlot, pSlotId, pMouseButton, pType);
	}

	private void setBuyPrice(Slot slot) {
		this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, getGuiLeft() + imageWidth / 2, getGuiTop() + imageHeight + 28, imageWidth, 15, true, (value) -> {
			try {
				double newPrice = Double.parseDouble(value);

				for (ItemStack stack : selectedItems) {
					ItemDescription description = ItemDescription.ofItem(stack);
					List<AdminShopScreen.ShopItem> items = this.shopItems.get(category).get(innerCategory);
					AdminShopScreen.ShopItem existingInfo = items.stream().filter(shopItem -> shopItem.itemDescription().equals(description)).findFirst().orElse(null);

					if (existingInfo != null) {
						items.remove(existingInfo);
						items.add(new AdminShopScreen.ShopItem(
								existingInfo.itemDescription(),
								newPrice,
								existingInfo.slot(),
								existingInfo.customName(),
								existingInfo.stage()
						));
					} else {
						items.add(new AdminShopScreen.ShopItem(
							description,
							newPrice,
							findFirstAvailableSlot(innerCategory),
							null,
							null
						));
					}
				}
				this.itemsWithBuyPrices = selectedItems.size();
			} catch (NumberFormatException ignored) {}
			this.removeWidget(this.floatingEditBox);
			this.floatingEditBox = null;
		}));
		this.setFocused(this.floatingEditBox);
	}

	private void setSellPrice(Slot slot) {
		this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, getGuiLeft() + imageWidth / 2, getGuiTop() + imageHeight + 28, imageWidth, 15, true, (value) -> {
			try {
				double newPrice = Double.parseDouble(value);

				for (ItemStack stack : selectedItems) {
					ItemDescription description = ItemDescription.ofItem(stack);
					AdminShopScreen.ItemSellabilityInfo existingInfo = this.sellPrices.get(description);

					if (existingInfo != null) {
						this.sellPrices.put(description, new AdminShopScreen.ItemSellabilityInfo(newPrice, existingInfo.stage()));
					} else {
						this.sellPrices.put(description, new AdminShopScreen.ItemSellabilityInfo(newPrice, null));
					}
				}
				this.itemsWithSellPrices = selectedItems.size();
			} catch (NumberFormatException ignored) {}
			this.removeWidget(this.floatingEditBox);
			this.floatingEditBox = null;
		}));
		this.setFocused(this.floatingEditBox);
	}

	private void removeBuyPrice(Slot slot) {
		for (ItemDescription description : selectedItems.stream().map(ItemDescription::ofItem).toList()) {
			List<AdminShopScreen.ShopItem> items = this.shopItems.get(category).get(innerCategory);
			items.stream().filter(shopItem -> shopItem.itemDescription().equals(description)).findFirst().ifPresent(items::remove);
		}
		this.itemsWithBuyPrices = 0;
	}

	private void removeSellPrice(Slot slot) {
		for (ItemDescription description : selectedItems.stream().map(ItemDescription::ofItem).toList()) {
			this.sellPrices.remove(description);
		}
		this.itemsWithSellPrices = 0;
	}

	private void setBuyStage(Slot slot) {
		if (!GameStagesCheck.isInstalled()) return;
		this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, getGuiLeft() + imageWidth / 2, getGuiTop() + imageHeight + 28, imageWidth, 15, false, (value) -> {
			for (ItemStack stack : selectedItems) {
				ItemDescription description = ItemDescription.ofItem(stack);
				List<AdminShopScreen.ShopItem> items = this.shopItems.get(category).get(innerCategory);
				AdminShopScreen.ShopItem existingInfo = items.stream().filter(shopItem -> shopItem.itemDescription().equals(description)).findFirst().orElse(null);

				if (existingInfo != null) {
					items.remove(existingInfo);
					items.add(new AdminShopScreen.ShopItem(
							existingInfo.itemDescription(),
							existingInfo.price(),
							existingInfo.slot(),
							existingInfo.customName(),
							value
					));
				}
			}
			this.itemsWithBuyStage = itemsWithBuyPrices;
			this.removeWidget(this.floatingEditBox);
			this.floatingEditBox = null;
		}));
		this.setFocused(this.floatingEditBox);
	}

	private void setSellStage(Slot slot) {
		if (!GameStagesCheck.isInstalled()) return;
		this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, getGuiLeft() + imageWidth / 2, getGuiTop() + imageHeight + 28, imageWidth, 15, false, (value) -> {
			for (ItemStack stack : selectedItems) {
				ItemDescription description = ItemDescription.ofItem(stack);
				AdminShopScreen.ItemSellabilityInfo existingInfo = this.sellPrices.get(description);

				if (existingInfo != null) {
					this.sellPrices.put(description, new AdminShopScreen.ItemSellabilityInfo(existingInfo.worth(), value));
				}
			}
			this.itemsWithSellStage = itemsWithSellPrices;
			this.removeWidget(this.floatingEditBox);
			this.floatingEditBox = null;
		}));
		this.setFocused(this.floatingEditBox);
	}

	private void removeBuyStage(Slot slot) {
		if (!GameStagesCheck.isInstalled()) return;
		for (ItemDescription description : selectedItems.stream().map(ItemDescription::ofItem).toList()) {
			List<AdminShopScreen.ShopItem> items = this.shopItems.get(category).get(innerCategory);
			items.stream().filter(shopItem -> shopItem.itemDescription().equals(description)).findFirst().ifPresent(item -> {
				items.remove(item);
				items.add(new AdminShopScreen.ShopItem(item.itemDescription(), item.price(), item.slot(), item.customName(), null));
			});
		}
		this.itemsWithBuyPrices = 0;
	}

	private void removeSellStage(Slot slot) {
		if (!GameStagesCheck.isInstalled()) return;
		for (ItemDescription description : selectedItems.stream().map(ItemDescription::ofItem).toList()) {
			this.sellPrices.put(description, new AdminShopScreen.ItemSellabilityInfo(this.sellPrices.get(description).worth(), null));
		}
		this.itemsWithSellPrices = 0;
	}

	@Override
	protected void slotClicked(@Nullable Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
		if (pSlot == null || pSlot.container != this.menu.OPT_CONTAINER && (pSlot.container != this.menu.CONTAINER || this.categoryPanel == null)) {
			super.slotClicked(pSlot, pSlotId, pMouseButton, pType);
			return;
		}

		if (pSlot.container == this.menu.OPT_CONTAINER) {
			optSlotClicked(pSlot, pSlotId, pMouseButton, pType);
			return;
		}

		if (pMouseButton == 1) { // Right-click (Create category)
			final ItemStack stack = pSlot.getItem();
			final ItemDescription description = ItemDescription.ofItem(stack);

			if (selectingInnerCategory)
				this.shopItems.get(category).put(new AdminShopScreen.InnerCategory(getUnnamedInnerCategoryName(), description), new ArrayList<>());
			else
				this.shopItems.put(new AdminShopScreen.Category(getUnnamedCategoryName(), description), new LinkedHashMap<>());

			if (selectingInnerCategory) setupInnerCategoryPanel(); else setupCategoryPanel();
		}
	}

	protected void optSlotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
		CompoundTag tag = pSlot.getItem().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe();
		if (!tag.contains("adminShopBulkOpt")) return;

		int opt = tag.getInt("adminShopBulkOpt");
		switch (opt) {
			case 0 -> adminShopOptClicked(pSlot, pMouseButton);
			case 1 -> categoryOptClicked(pSlot, pMouseButton);
			case 2 -> innerCategoryOptClicked(pSlot, pMouseButton);
		}
	}

	protected void adminShopOptClicked(Slot slot, int mouseButton) {
		switch (mouseButton) {
			case 0 -> { // Left-click (Unused)

			}
			case 1 -> { // Right-click (Edit)
				if (this.categoryPanel != null || floatingEditBox != null) return;
				this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, getGuiLeft() + imageWidth / 2, getGuiTop() + imageHeight + 28, imageWidth, 15, false, (value) -> {
					this.sendChanges();
					adminShopName = value.isEmpty() ? null : value;
					this.requestShopData();
					this.removeWidget(this.floatingEditBox);
					this.floatingEditBox = null;
				}));
				this.setFocused(this.floatingEditBox);
			}
			case 2 -> { // Middle-click (Reset)
				if (this.categoryPanel != null || this.floatingEditBox == null) return;

				clearWidgets();
				if (adminShopName == null) return;
				sendChanges();
				adminShopName = null;
				this.requestShopData();
			}
		}
	}

	protected void categoryOptClicked(Slot slot, int mouseButton) {
		switch (mouseButton) {
			case 0 -> { // Left-click (Unused)

			}
			case 1 -> { // Right-click (Change)
				if (this.categoryPanel != null || floatingEditBox != null) return;
				setupCategoryPanel();
			}
			case 2 -> { // Middle-click (Cancel)
				if (this.categoryPanel == null) return;

				updateOpts();
				refreshItemsWith();
				clearWidgets();
			}
		}
	}

	protected void innerCategoryOptClicked(Slot slot, int mouseButton) {
		switch (mouseButton) {
			case 0 -> { // Left-click (Unused)

			}
			case 1 -> { // Right-click (Change)
				if (this.category == null || this.categoryPanel != null || floatingEditBox != null) return;
				setupInnerCategoryPanel();
			}
			case 2 -> { // Middle-click (Cancel)
				if (this.categoryPanel == null || this.category == null) return;

				updateOpts();
				refreshItemsWith();
				clearWidgets();
			}
		}
	}

	@Override
	protected void onTooltip(List<Component> components, ItemStack stack) {
		if (this.categoryPanel != null || this.innerCategory == null) return;

		ItemDescription description = ItemDescription.ofItem(stack);
		AdminShopScreen.ShopItem item = this.shopItems
				.get(category)
				.get(innerCategory)
				.stream()
				.filter(shopItem -> shopItem.itemDescription().equals(description))
				.findFirst()
				.orElse(null);

		boolean buy = item != null;
		boolean sell = this.sellPrices.containsKey(description);

		double buyPrice = buy ? item.price() : -1;
		double sellPrice = sell ? this.sellPrices.get(description).worth() : -1;

		if (buy) components.add(Component.translatable("jackseconomy.buy_price", 1, Component.literal(oneItemCurrencyMode ? "$" + (long)buyPrice : CurrencyHelper.format(buyPrice)).withStyle(ChatFormatting.GRAY)));
		if (sell) components.add(Component.translatable("jackseconomy.sell_price", 1, Component.literal(oneItemCurrencyMode ? "$" + (long)sellPrice : CurrencyHelper.format(sellPrice)).withStyle(ChatFormatting.GRAY)));
		if (buy || sell) components.add(Component.empty());
	}

	@Override
	protected void onTooltipSelected(List<Component> components, ItemStack stack) {
		super.onTooltipSelected(components, stack);
		if (!shouldShowSelection) return;

		components.add(Component.translatable("jackseconomy.bulk_set_sell_price").withStyle(ChatFormatting.AQUA));
		components.add(Component.translatable("jackseconomy.bulk_set_buy_price").withStyle(ChatFormatting.AQUA));
		if (itemsWithSellPrices != 0 && GameStagesCheck.isInstalled()) components.add(Component.translatable("jackseconomy.bulk_set_sell_stage").withStyle(ChatFormatting.AQUA));
		if (itemsWithBuyPrices != 0 && GameStagesCheck.isInstalled()) components.add(Component.translatable("jackseconomy.bulk_set_buy_stage").withStyle(ChatFormatting.AQUA));
		if (itemsWithSellPrices != 0) components.add(Component.translatable("jackseconomy.bulk_remove_sell_price").withStyle(ChatFormatting.RED));
		if (itemsWithBuyPrices != 0) components.add(Component.translatable("jackseconomy.bulk_remove_buy_price").withStyle(ChatFormatting.RED));
		if (itemsWithBuyStage != 0 && GameStagesCheck.isInstalled()) components.add(Component.translatable("jackseconomy.bulk_remove_sell_stage").withStyle(ChatFormatting.RED));
		if (itemsWithSellStage != 0 && GameStagesCheck.isInstalled()) components.add(Component.translatable("jackseconomy.bulk_remove_buy_stage").withStyle(ChatFormatting.RED));
	}

	@Override
	public @NotNull List<Component> getTooltipFromContainerItem(ItemStack pStack) {
		if (categoryPanel != null) {
			List<Component> list = new ArrayList<>();
			if (floatingEditBox == null) list.add(Component.literal("Right-click to create category").withStyle(ChatFormatting.AQUA));
			return list;
		}
		return super.getTooltipFromContainerItem(pStack);
	}

	public List<Component> getTooltipFromOptContainerItem(ItemStack stack, int opt) {
		List<Component> list = new ArrayList<>();
		switch (opt) {
			case 0 -> {
				list.add(Component.translatable(adminShopName != null ? adminShopName : "jackseconomy.default"));
				if (this.categoryPanel == null) {
					list.add(Component.empty());
					if (floatingEditBox == null) list.add(Component.translatable("jackseconomy.right_click_to_change").withStyle(ChatFormatting.AQUA));
					if (adminShopName != null || floatingEditBox != null) list.add(Component.translatable("jackseconomy.middle_click_to_reset").withStyle(ChatFormatting.RED));
				}
			}
			case 1 -> {
				list.add(category != null ? Component.literal(category.name) : Component.translatable("jackseconomy.none_selected"));

				if (this.categoryPanel != null || floatingEditBox == null) list.add(Component.empty());
				if (this.categoryPanel == null && floatingEditBox == null) list.add(Component.translatable("jackseconomy.right_click_to_change").withStyle(ChatFormatting.AQUA));
				if (this.categoryPanel != null) list.add(Component.translatable("jackseconomy.middle_click_to_cancel").withStyle(ChatFormatting.RED));
			}
			case 2 -> {
				list.add(innerCategory != null ? Component.literal(innerCategory.name) : Component.translatable("jackseconomy.none_selected"));
				if (this.category == null) return list;

				if (this.categoryPanel != null || floatingEditBox == null) list.add(Component.empty());
				if (this.categoryPanel == null && floatingEditBox == null) list.add(Component.translatable("jackseconomy.right_click_to_change").withStyle(ChatFormatting.AQUA));
				if (this.categoryPanel != null) list.add(Component.translatable("jackseconomy.middle_click_to_cancel").withStyle(ChatFormatting.RED));
			}
		}
		return list;
	}

	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta, double pOffset) {
		return this.categoryPanel != null && this.categoryPanel.isMouseOver(pMouseX, pMouseY) ? this.categoryPanel.mouseScrolled(pMouseX, pMouseY, pDelta, pOffset) : super.mouseScrolled(pMouseX, pMouseY, pDelta, pOffset);
	}

	@Override
	public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		this.categoryHovered = false;
		super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
	}

	@Override
	protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
		if (categoryHovered) {
			List<Component> list = List.of(
					Component.translatable("jackseconomy.right_click_to_rename").withStyle(ChatFormatting.AQUA),
					Component.translatable("jackseconomy.middle_click_to_remove_category").withStyle(ChatFormatting.RED)
			);
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0, 0, 256);
			guiGraphics.renderTooltip(this.font, list, Optional.empty(), x, y);
			guiGraphics.pose().popPose();
			return;
		}

		if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
			ItemStack itemstack = this.hoveredSlot.getItem();

			if (this.hoveredSlot.container == this.menu.OPT_CONTAINER) {
				guiGraphics.renderTooltip(this.font, this.getTooltipFromOptContainerItem(itemstack, this.hoveredSlot.getContainerSlot()), itemstack.getTooltipImage(), itemstack, x, y);
			} else guiGraphics.renderTooltip(this.font, this.getTooltipFromContainerItem(itemstack), itemstack.getTooltipImage(), itemstack, x, y);
		}
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
		super.renderBg(guiGraphics, pPartialTick, pMouseX, pMouseY);
		RenderSystem.setShaderTexture(0, SLOT);
		for (int i = 0; i < 3; i++) {
			guiGraphics.blit(SLOT, this.leftPos - 31, this.topPos + 17 + 20 * i, 0, 0, 18, 18, 18, 18);
		}
	}

	protected void requestShopData() {
		PacketDistributor.sendToServer(new RequestAdminShopSchemaPacket(Optional.ofNullable(adminShopName)));
	}

	private void sendChanges() {
		PacketDistributor.sendToServer(new UpdateAdminShopPacket(this.toAdminShopUpdateCompound(), Optional.ofNullable(adminShopName)));
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

			for (Map.Entry<AdminShopScreen.InnerCategory, List<AdminShopScreen.ShopItem>> entry : innerCategories.entrySet()) {
				CompoundTag innerCategoryTag = new CompoundTag();
				innerCategoryTag.putString("name", entry.getKey().name);
				innerCategoryTag.put("item", entry.getKey().itemDescription.toNbt());

				for (AdminShopScreen.ShopItem shopItem : entry.getValue()) {
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

	public void onShopData(CompoundTag data, @Nullable String adminShopName) {
		if (!Objects.equals(adminShopName, this.adminShopName)) return;

		this.category = null;
		this.innerCategory = null;
		this.shopItems.clear();
		this.sellPrices.clear();

		ListTag categoriesTag = data.getList("categories", Tag.TAG_COMPOUND);

		categoriesTag.forEach(tag -> {
			if (tag instanceof CompoundTag compoundTag) {
				String categoryName = compoundTag.getString("name");
				ItemDescription itemDescription = ItemDescription.fromNbt(compoundTag.getCompound("item"));

				if (itemDescription == null) {
					return;
				}

				AdminShopScreen.Category category = new AdminShopScreen.Category(categoryName, itemDescription);
				LinkedHashMap<AdminShopScreen.InnerCategory, List<AdminShopScreen.ShopItem>> innerCategories = new LinkedHashMap<>();
				shopItems.put(category, innerCategories);
				ListTag categoriesInnerTag = compoundTag.getList("categories", Tag.TAG_COMPOUND);

				categoriesInnerTag.forEach(innerTag -> {
					if (innerTag instanceof CompoundTag innerCompoundTag) {
						String innerCategoryName = innerCompoundTag.getString("name");
						ItemDescription innerItemDescription = ItemDescription.fromNbt(innerCompoundTag.getCompound("item"));

						if (innerItemDescription == null) {
							return;
						}

						AdminShopScreen.InnerCategory innerCategory = new AdminShopScreen.InnerCategory(innerCategoryName, innerItemDescription);
						innerCategories.put(innerCategory, new ArrayList<>());
					}
				});
			}
		});

		LinkedHashMap<String, List<AdminShopScreen.UnpreparedShopItem>> slotlessShopItems = new LinkedHashMap<>();

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
					sellPrices.put(itemDescription, new AdminShopScreen.ItemSellabilityInfo(oneItemCurrencyMode ? Math.round(sellPrice) : sellPrice, sellStage));
					return;
				}

				double buyPrice = compoundTag.getDouble("adminShopBuyPrice");
				int slot = compoundTag.contains("slot") ? compoundTag.getInt("slot") : -1;
				String customName = compoundTag.contains("customAdminShopName") ? compoundTag.getString("customAdminShopName") : null;
				String stage = compoundTag.contains("adminShopStage") ? compoundTag.getString("adminShopStage") : null;
				String category = compoundTag.getString("category");

				double price = oneItemCurrencyMode ? Math.round(buyPrice) : buyPrice;
				if (slot < 0) {
					slotlessShopItems.computeIfAbsent(category, categoryName -> new ArrayList<>()).add(new AdminShopScreen.UnpreparedShopItem(itemDescription, price, customName, stage));
				} else {
					String[] categoryNamesInner = category.split(":", 2);
					if (categoryNamesInner.length < 2) {
						return;
					}

					String bigCategoryName = categoryNamesInner[0];
					String innerCategoryName = categoryNamesInner[1];

					for (Map.Entry<AdminShopScreen.Category, LinkedHashMap<AdminShopScreen.InnerCategory, List<AdminShopScreen.ShopItem>>> categoryEntry : shopItems.entrySet()) {
						if (categoryEntry.getKey().name.equals(bigCategoryName)) {
							for (Map.Entry<AdminShopScreen.InnerCategory, List<AdminShopScreen.ShopItem>> entry : shopItems.get(categoryEntry.getKey()).entrySet()) {
								if (entry.getKey().name.equals(innerCategoryName)) {
									entry.getValue().add(new AdminShopScreen.ShopItem(itemDescription, price, slot, customName, stage));
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

			for (Map.Entry<AdminShopScreen.Category, LinkedHashMap<AdminShopScreen.InnerCategory, List<AdminShopScreen.ShopItem>>> entry : shopItems.entrySet()) {
				if (entry.getKey().name.equals(bigCategoryName)) {
					for (AdminShopScreen.UnpreparedShopItem item : list) {
						for (Map.Entry<AdminShopScreen.InnerCategory, List<AdminShopScreen.ShopItem>> entry1 : entry.getValue().entrySet()) {
							if (entry1.getKey().name.equals(innerCategoryName)) {
								shopItems.get(entry.getKey()).get(entry1.getKey()).add(new AdminShopScreen.ShopItem(item.itemDescription(), item.price(), this.findFirstAvailableSlot(entry.getKey()), item.customName(), item.stage()));
								break;
							}
						}
					}
					break;
				}
			}
		});

		this.selectFirstAvailableCategory();
		this.updateOpts();
		this.refreshItemsWith();

		this.clearWidgets();
	}

	@Override
	protected void clearWidgets() {
		super.clearWidgets();
		this.floatingEditBox = null;
		this.categoryPanel = null;
	}

	protected void selectFirstAvailableCategory() {
		this.category = this.shopItems.keySet().stream().findFirst().orElse(null);
		selectFirstAvailableInnerCategory();
	}

	protected void selectFirstAvailableInnerCategory() {
		if (this.category != null) {
			this.innerCategory = this.shopItems.get(this.category).keySet().stream().findFirst().orElse(null);
		}
	}

	protected @Nullable AdminShopScreen.ShopItem getItemAtSlot(int slot, AdminShopScreen.InnerCategory category) {
		for (AdminShopScreen.ShopItem shopItem : this.shopItems.get(this.category).getOrDefault(category, List.of())) {
			if (shopItem.slot() == slot) {
				return shopItem;
			}
		}

		return null;
	}

	protected int findFirstAvailableSlot(AdminShopScreen.InnerCategory category) {
		for (int slot = 0;;slot++) {
			AdminShopScreen.ShopItem existingShopItem = this.getItemAtSlot(slot, category);

			if (existingShopItem == null) {
				return slot;
			}
		}
	}

	protected void updateOpts() {
		ItemStack adminShopStack = new ItemStack(ItemBlockReg.ADMIN_SHOP.get());
		if (adminShopName != null) {
			CompoundTag tag = new CompoundTag();
			tag.putString("adminShopName", adminShopName);
			BlockItem.setBlockEntityData(adminShopStack, BlockEntityReg.ADMIN_SHOP.get(), tag);
		}
		menu.OPT_CONTAINER.setItem(0, menu.setOpt(adminShopStack, 0));
		menu.OPT_CONTAINER.setItem(1, menu.setOpt(category != null ? category.itemDescription.createItemStack() : new ItemStack(Items.BARRIER), 1));
		menu.OPT_CONTAINER.setItem(2, menu.setOpt(innerCategory != null ? innerCategory.itemDescription.createItemStack() : new ItemStack(Items.BARRIER), 2));

		if (this.innerCategory != null) {
			canEditSelection = true;
			shouldShowSelection = true;
		} else {
			canEditSelection = false;
			shouldShowSelection = false;
		}
	}

	protected void setupCategoryPanel() {
		if (this.categoryPanel != null) removeWidget(this.categoryPanel);
		this.categoryPanel = this.addRenderableWidget(new BetterScrollPanel(Minecraft.getInstance(), this.leftPos - 125, this.topPos, 80, this.imageHeight));
		selectingInnerCategory = false;
		canEditSelection = false;
		shouldShowSelection = false;

		for (AdminShopScreen.Category category : shopItems.keySet()) {
			this.categoryPanel.children.add(new EditCategoryEntry(0, 0, 80, 25, category, (categoryEntry, button) -> {
				if (button == 0) {
					this.category = category;
					this.innerCategory = null;
					selectFirstAvailableInnerCategory();
					updateOpts();
					refreshItemsWith();
					clearWidgets();
				} else if (button == 1) {
					if (this.floatingEditBox != null) {
						this.removeWidget(this.floatingEditBox);
						this.floatingEditBox = null;
					}

					this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, getGuiLeft() + imageWidth / 2, getGuiTop() + imageHeight + 28, imageWidth, 15, (value) -> {
						for (AdminShopScreen.Category otherCategory : this.shopItems.keySet()) {
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

							setupCategoryPanel();
							return;
						}
					}));
					this.setFocused(this.floatingEditBox);
				} else if (button == 2) {
					this.shopItems.remove(category);

					if (this.category == category) {
						this.category = null;
						for (AdminShopScreen.Category category1 : this.shopItems.keySet()) {
							this.category = category1;
							break;
						}
					}

					setupCategoryPanel();
					updateOpts();
				}
			}, () -> this.category == category, () -> this.categoryHovered = true, this.categoryPanel));
		}
	}

	protected void setupInnerCategoryPanel() {
		if (this.categoryPanel != null) removeWidget(this.categoryPanel);
		this.categoryPanel = this.addRenderableWidget(new BetterScrollPanel(Minecraft.getInstance(), this.leftPos - 125, this.topPos, 80, this.imageHeight));
		selectingInnerCategory = true;
		canEditSelection = false;
		shouldShowSelection = false;

		for (AdminShopScreen.InnerCategory category : shopItems.get(this.category).keySet()) {
			this.categoryPanel.children.add(new EditCategoryEntry(0, 0, 80, 25, category, (categoryEntry, button) -> {
				if (button == 0) {
					this.innerCategory = category;
					updateOpts();
					refreshItemsWith();
					clearWidgets();
				} else if (button == 1) {
					if (this.floatingEditBox != null) {
						this.removeWidget(this.floatingEditBox);
						this.floatingEditBox = null;
					}

					this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, getGuiLeft() + imageWidth / 2, getGuiTop() + imageHeight + 28, imageWidth, 15, (value) -> {
						for (AdminShopScreen.InnerCategory otherCategory : this.shopItems.get(this.category).keySet()) {
							if (this.shopItems.get(this.category).size() > 1 && otherCategory == category) {
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

							setupInnerCategoryPanel();
							return;
						}
					}));
					this.setFocused(this.floatingEditBox);
				} else if (button == 2) {
					this.shopItems.get(this.category).remove(category);

					if (this.innerCategory == category) {
						this.innerCategory = null;
						for (AdminShopScreen.InnerCategory category1 : this.shopItems.get(this.category).keySet()) {
							this.innerCategory = category1;
							break;
						}
					}

					setupInnerCategoryPanel();
					updateOpts();
				}
			}, () -> this.innerCategory == category, () -> this.categoryHovered = true, this.categoryPanel));
		}
	}

	protected List<AdminShopScreen.InnerCategory> getInnerCategories(AdminShopScreen.Category category) {
		return this.shopItems.get(category).keySet().stream().toList();
	}

	protected String getUnnamedInnerCategoryName() {
		List<AdminShopScreen.InnerCategory> categories = this.getInnerCategories(category);
		int i = -1;
		while (true) {
			String name = "Unnamed" + (i == -1 ? "" : " " + i);
			boolean exists = false;

			for (AdminShopScreen.InnerCategory category : categories) {
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
		List<AdminShopScreen.Category> categories = this.shopItems.keySet().stream().toList();
		int i = -1;
		while (true) {
			String name = "Unnamed" + (i == -1 ? "" : " " + i);
			boolean exists = false;

			for (AdminShopScreen.InnerCategory category : categories) {
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

	public static class Menu extends ItemPickerMenu {
		private final SimpleContainer OPT_CONTAINER = new SimpleContainer(3);
		public final boolean oneItemCurrencyMode;
		public Menu(int pContainerId, Inventory pPlayerInventory) {
			super(pContainerId, pPlayerInventory);

			this.oneItemCurrencyMode = Config.oneItemCurrencyMode.get();

			this.addSlot(new CustomCreativeSlot(this.OPT_CONTAINER, 0, -30, 18));
			this.addSlot(new CustomCreativeSlot(this.OPT_CONTAINER, 1, -30, 38));
			this.addSlot(new CustomCreativeSlot(this.OPT_CONTAINER, 2, -30, 58));

			OPT_CONTAINER.setItem(0, setOpt(new ItemStack(ItemBlockReg.ADMIN_SHOP_ITEM.get(), 1), 0));
			OPT_CONTAINER.setItem(1, setOpt(new ItemStack(Items.BARRIER, 1), 1));
			OPT_CONTAINER.setItem(2, setOpt(new ItemStack(Items.BARRIER, 1), 2));
		}

		public ItemStack setOpt(ItemStack stack, int opt) {
			CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			tag.putInt("adminShopBulkOpt", opt);
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
			return stack;
		}

		@Override
		public @NotNull MenuType<?> getType() {
			return ContainerReg.BULK_ADMIN_SHOP_MENU.get();
		}

		@Override
		public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
			return super.canTakeItemForPickAll(pStack, pSlot) && pSlot.container != this.OPT_CONTAINER;
		}

		@Override
		public boolean canDragTo(Slot pSlot) {
			return super.canDragTo(pSlot) && pSlot.container != this.OPT_CONTAINER;
		}
	}
}
