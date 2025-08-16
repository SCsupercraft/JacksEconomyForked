package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import me.khajiitos.jackseconomy.data.price.FluidDescription;
import me.khajiitos.jackseconomy.screen.widget.FloatingEditBoxWidget;
import me.khajiitos.jackseconomy.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.SessionSearchTrees;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.CreativeModeTabSearchRegistry;
import net.neoforged.neoforge.client.gui.CreativeTabsScreenPage;
import net.neoforged.neoforge.common.CreativeModeTabRegistry;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @see net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
 */
public abstract class FluidSelectionScreen<T extends FluidSelectionScreen.ItemPickerMenu> extends AbstractContainerScreen<T> {
	protected static final ResourceLocation[] UNSELECTED_TOP_TABS = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_1"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_2"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_3"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_4"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_5"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_6"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_7")};
	protected static final ResourceLocation[] SELECTED_TOP_TABS = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_1"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_2"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_3"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_4"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_5"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_6"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_7")};
	protected static final ResourceLocation[] UNSELECTED_BOTTOM_TABS = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_1"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_2"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_3"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_4"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_5"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_6"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_7")};
	protected static final ResourceLocation[] SELECTED_BOTTOM_TABS = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_1"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_2"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_3"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_4"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_5"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_6"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_7")};
	protected static final int selectedItemColor = Utils.hexToMinecraftColor("#4DFFDE59");
	/** Currently selected creative inventory tab index. */
	protected static CreativeModeTab selectedTab = CreativeModeTabs.getDefaultTab();
	/** Amount scrolled in Creative mode inventory (0 = top, 1 = bottom) */
	protected float scrollOffs;
	/** True if the scrollbar is being dragged */
	protected boolean scrolling;
	protected EditBox searchBox;
	protected boolean ignoreTextInput;
	protected boolean hasClickedOutside;
	protected final Set<TagKey<Fluid>> visibleTags = new HashSet<>();
	protected final boolean displayOperatorCreativeTab;
	protected final List<CreativeTabsScreenPage> pages = new ArrayList<>();
	protected CreativeTabsScreenPage currentPage = new CreativeTabsScreenPage(new ArrayList<>());
	protected List<ItemStack> selectedItems = new ArrayList<>();
	protected FloatingEditBoxWidget floatingEditBox;
	protected boolean canEditSelection = true;
	protected boolean shouldShowSelection = true;
	protected boolean refresh = true;

	public FluidSelectionScreen(T menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageHeight = 136;
		this.imageWidth = 195;
		this.displayOperatorCreativeTab = false;
		CreativeModeTabs.tryRebuildTabContents(Minecraft.getInstance().getConnection().enabledFeatures(), this.hasPermissions(inventory.player), inventory.player.level().registryAccess());
	}

	protected List<ItemStack> onlyFluidContainers(Stream<ItemStack> stacks) {
		return stacks.filter(stack -> {
			IFluidHandlerItem fluidHandlerItem = stack.getCapability(Capabilities.FluidHandler.ITEM);
			if (fluidHandlerItem == null) return false;
			return !fluidHandlerItem.getFluidInTank(0).isEmpty();
		}).toList();
	}

	void refreshItemsWith() {
		for (ItemStack stack : selectedItems) {
			addItemsWith(FluidDescription.ofItem(stack));
		}
	}

	void removeItemsWith(FluidDescription description) {}

	void addItemsWith(FluidDescription description) {}

	protected boolean isSelected(ItemStack stack) {
		return !stack.isEmpty() && selectedItems.contains(stack);
	}

	protected void deselect(ItemStack stack) {
		if (!isSelected(stack) || !canEditSelection) return;
		selectedItems.remove(stack);
		removeItemsWith(FluidDescription.ofItem(stack));
	}

	protected void select(ItemStack stack) {
		if (isSelected(stack) || !canEditSelection || stack.isEmpty()) return;
		clearSelection();
		selectedItems.add(stack);
		refreshItemsWith();
	}

	protected void addToSelection(ItemStack stack) {
		if (isSelected(stack) || !canEditSelection || stack.isEmpty()) return;
		selectedItems.add(stack);
		addItemsWith(FluidDescription.ofItem(stack));
	}

	protected void clearSelection() {
		if (!canEditSelection) return;
		selectedItems.clear();
		refreshItemsWith();
	}

	protected void onTooltip(List<Component> components, ItemStack stack) {

	}

	protected void onTooltipSelected(List<Component> components, ItemStack stack) {
		if (shouldShowSelection) components.add(Component.translatable("jackseconomy.selected").withStyle(ChatFormatting.GREEN));
		if (canEditSelection) components.add(Component.translatable("jackseconomy.bulk_deselect").withStyle(ChatFormatting.AQUA));
	}

	protected void onTooltipUnselected(List<Component> components, ItemStack stack) {
		if (!canEditSelection) return;

		components.add(Component.translatable("jackseconomy.bulk_select").withStyle(ChatFormatting.AQUA));
		components.add(Component.translatable("jackseconomy.bulk_add_to_selection").withStyle(ChatFormatting.AQUA));
		components.add(Component.translatable("jackseconomy.bulk_reset_selection").withStyle(ChatFormatting.RED));
	}

	private boolean hasPermissions(Player pPlayer) {
		return pPlayer.canUseGameMasterBlocks() && this.displayOperatorCreativeTab;
	}

	private void tryRefreshInvalidatedTabs(FeatureFlagSet pEnabledFeatures, boolean pHasPermissions, HolderLookup.Provider pHolders) {
		if (CreativeModeTabs.tryRebuildTabContents(pEnabledFeatures, pHasPermissions, pHolders) || refresh) {
			refresh = false;
			for(CreativeModeTab creativemodetab : CreativeModeTabs.allTabs()) {
				Collection<ItemStack> collection = creativemodetab.getDisplayItems();
				if (creativemodetab == selectedTab) {
					if (creativemodetab.getType() == CreativeModeTab.Type.CATEGORY && collection.isEmpty()) {
						this.selectTab(CreativeModeTabs.getDefaultTab());
					} else {
						this.refreshCurrentTabContents(collection);
					}
				}
			}
		}
	}

	private void refreshCurrentTabContents(Collection<ItemStack> items) {
		int i = this.menu.getRowIndexForScroll(this.scrollOffs);
		this.menu.items.clear();
		if (selectedTab.hasSearchBar()) {
			this.refreshSearchResults();
		} else {
			this.menu.items.addAll(onlyFluidContainers(items.stream()));
		}

		this.scrollOffs = this.menu.getScrollForRowIndex(i);
		this.menu.scrollTo(this.scrollOffs);
	}

	public void containerTick() {
		super.containerTick();
		if (this.minecraft != null) {
			if (this.minecraft.player != null) {
				this.tryRefreshInvalidatedTabs(this.minecraft.player.connection.enabledFeatures(), this.hasPermissions(this.minecraft.player), this.minecraft.player.level().registryAccess());
			}

			if (this.floatingEditBox != null) {
				if (!this.floatingEditBox.isFocused()) setFocused(this.floatingEditBox);
			}
		}
	}

	/**
	 * Called when the mouse is clicked over a slot or outside the gui.
	 */
	protected void slotClicked(@Nullable Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
		if (this.isCreativeSlot(pSlot)) {
			if (isSelected(pSlot.getItem())) {
				onSelectedSlotClicked(pSlot, pSlotId, pMouseButton, pType);
			} else {
				onUnselectedSlotClicked(pSlot, pSlotId, pMouseButton, pType);
			}
		}
	}

	protected void onSelectedSlotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
		if (pMouseButton == 0) { // Left-click
			deselect(pSlot.getItem());
		}
	}

	protected void onUnselectedSlotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
		switch (pMouseButton) {
			case 0 -> // Left-click
					select(pSlot.getItem());
			case 1 -> // Right-click
					addToSelection(pSlot.getItem());
			case 2 -> // Middle-click
					clearSelection();
		}
	}

	private boolean isCreativeSlot(@Nullable Slot pSlot) {
		return pSlot != null && pSlot.container == this.menu.CONTAINER;
	}

	protected void init() {
		super.init();
		this.pages.clear();
		int tabIndex = 0;
		List<CreativeModeTab> currentPage = new ArrayList<>();

		for (CreativeModeTab sortedCreativeModeTab : CreativeModeTabRegistry.getSortedCreativeModeTabs()) {
			if (sortedCreativeModeTab.getType() == CreativeModeTab.Type.HOTBAR || sortedCreativeModeTab.getType() == CreativeModeTab.Type.INVENTORY || sortedCreativeModeTab.getDisplayItems().stream().noneMatch((stack) -> stack.getCapability(Capabilities.FluidHandler.ITEM) != null)) continue;

			currentPage.add(sortedCreativeModeTab);
			tabIndex++;
			if (tabIndex == 10) {
				this.pages.add(new CreativeTabsScreenPage(currentPage));
				currentPage = new ArrayList<>();
				tabIndex = 0;
			}
		}

		if (tabIndex != 0) {
			this.pages.add(new CreativeTabsScreenPage(currentPage));
		}

		if (this.pages.isEmpty()) {
			this.currentPage = new CreativeTabsScreenPage(new ArrayList<>());
		} else {
			this.currentPage = this.pages.get(0);
		}

		if (this.pages.size() > 1) {
			addRenderableWidget(Button.builder(Component.literal("<"), b -> setCurrentPage(this.pages.get(Math.max(this.pages.indexOf(this.currentPage) - 1, 0)))).pos(leftPos,  topPos - 50).size(20, 20).build());
			addRenderableWidget(Button.builder(Component.literal(">"), b -> setCurrentPage(this.pages.get(Math.min(this.pages.indexOf(this.currentPage) + 1, this.pages.size() - 1)))).pos(leftPos + imageWidth - 20, topPos - 50).size(20, 20).build());
		}

		this.currentPage = this.pages.stream().filter(page -> page.getVisibleTabs().contains(selectedTab)).findFirst().orElse(this.currentPage);
		if (!this.currentPage.getVisibleTabs().contains(selectedTab)) {
			selectedTab = this.currentPage.getVisibleTabs().get(0);
		}

		this.searchBox = new EditBox(this.font, this.leftPos + 82, this.topPos + 6, 80, 9, Component.translatable("itemGroup.search"));
		this.searchBox.setMaxLength(50);
		this.searchBox.setBordered(false);
		this.searchBox.setVisible(false);
		this.searchBox.setTextColor(16777215);

		this.addWidget(this.searchBox);

		this.selectTab(selectedTab);
		if (!selectedTab.shouldDisplay() && CreativeModeTabs.getDefaultTab().shouldDisplay()) {
			this.selectTab(CreativeModeTabs.getDefaultTab());
		}
	}

	public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
		int i = this.menu.getRowIndexForScroll(this.scrollOffs);
		String s = this.searchBox.getValue();
		this.init(pMinecraft, pWidth, pHeight);
		this.searchBox.setValue(s);
		if (!this.searchBox.getValue().isEmpty()) {
			this.refreshSearchResults();
		}

		this.scrollOffs = this.menu.getScrollForRowIndex(i);
		this.menu.scrollTo(this.scrollOffs);
	}

	/**
	 * Called when a character is typed within the GUI element.
	 * <p>
	 * @return {@code true} if the event is consumed, {@code false} otherwise.
	 * @param pCodePoint the code point of the typed character.
	 * @param pModifiers the keyboard modifiers.
	 */
	public boolean charTyped(char pCodePoint, int pModifiers) {
		if (this.floatingEditBox != null && this.floatingEditBox.charTyped(pCodePoint, pModifiers)) {
			return true;
		} else if (this.ignoreTextInput) {
			return false;
		} else if (!selectedTab.hasSearchBar()) {
			return false;
		} else {
			String s = this.searchBox.getValue();
			if (this.searchBox.charTyped(pCodePoint, pModifiers)) {
				if (!Objects.equals(s, this.searchBox.getValue())) {
					this.refreshSearchResults();
				}

				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Called when a keyboard key is pressed within the GUI element.
	 * <p>
	 * @return {@code true} if the event is consumed, {@code false} otherwise.
	 * @param pKeyCode the key code of the pressed key.
	 * @param pScanCode the scan code of the pressed key.
	 * @param pModifiers the keyboard modifiers.
	 */
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		this.ignoreTextInput = false;
		if (this.floatingEditBox != null && pKeyCode == GLFW.GLFW_KEY_ESCAPE) {
			removeWidget(floatingEditBox);
			floatingEditBox = null;
			return true;
		} else if (this.floatingEditBox != null && this.floatingEditBox.keyPressed(pKeyCode, pScanCode, pModifiers)) {
			return true;
		} else if (!selectedTab.hasSearchBar()) {
			if (this.minecraft.options.keyChat.matches(pKeyCode, pScanCode)) {
				this.ignoreTextInput = true;
				this.selectTab(CreativeModeTabs.searchTab());
				return true;
			} else {
				return super.keyPressed(pKeyCode, pScanCode, pModifiers);
			}
		} else {
			boolean flag = !this.isCreativeSlot(this.hoveredSlot) || this.hoveredSlot.hasItem();
			boolean flag1 = InputConstants.getKey(pKeyCode, pScanCode).getNumericKeyValue().isPresent();
			if (flag && flag1 && this.checkHotbarKeyPressed(pKeyCode, pScanCode)) {
				this.ignoreTextInput = true;
				return true;
			} else {
				String s = this.searchBox.getValue();
				if (this.searchBox.keyPressed(pKeyCode, pScanCode, pModifiers)) {
					if (!Objects.equals(s, this.searchBox.getValue())) {
						this.refreshSearchResults();
					}

					return true;
				} else {
					return this.searchBox.isFocused() && this.searchBox.isVisible() && pKeyCode != 256 || super.keyPressed(pKeyCode, pScanCode, pModifiers);
				}
			}
		}
	}

	/**
	 * Called when a keyboard key is released within the GUI element.
	 * <p>
	 * @return {@code true} if the event is consumed, {@code false} otherwise.
	 * @param pKeyCode the key code of the released key.
	 * @param pScanCode the scan code of the released key.
	 * @param pModifiers the keyboard modifiers.
	 */
	public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
		this.ignoreTextInput = false;
		return super.keyReleased(pKeyCode, pScanCode, pModifiers);
	}

	private void refreshSearchResults() {
		if (!selectedTab.hasSearchBar()) return;
		this.menu.items.clear();
		this.visibleTags.clear();
		String s = this.searchBox.getValue();
		if (s.isEmpty()) {
			this.menu.items.addAll(onlyFluidContainers(selectedTab.getDisplayItems().stream()));
		} else {
			ClientPacketListener clientpacketlistener = this.minecraft.getConnection();
			if (clientpacketlistener != null) {
				SessionSearchTrees sessionsearchtrees = clientpacketlistener.searchTrees();
				SearchTree<ItemStack> searchtree;
				if (s.startsWith("#")) {
					s = s.substring(1);
					searchtree = sessionsearchtrees.creativeTagSearch(CreativeModeTabSearchRegistry.getTagSearchKey(selectedTab));
					this.updateVisibleTags(s);
				} else {
					searchtree = sessionsearchtrees.creativeNameSearch(CreativeModeTabSearchRegistry.getNameSearchKey(selectedTab));
				}

				this.menu.items.addAll(onlyFluidContainers(searchtree.search(s.toLowerCase(Locale.ROOT)).stream()));
			}
		}

		this.scrollOffs = 0.0F;
		this.menu.scrollTo(0.0F);
	}

	private void updateVisibleTags(String pSearch) {
		int i = pSearch.indexOf(58);
		Predicate<ResourceLocation> predicate;
		if (i == -1) {
			predicate = (p_98609_) -> p_98609_.getPath().contains(pSearch);
		} else {
			String s = pSearch.substring(0, i).trim();
			String s1 = pSearch.substring(i + 1).trim();
			predicate = (p_98606_) -> p_98606_.getNamespace().contains(s) && p_98606_.getPath().contains(s1);
		}

		BuiltInRegistries.FLUID.getTagNames().filter((p_205410_) -> predicate.test(p_205410_.location())).forEach(this.visibleTags::add);
	}

	protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
		if (selectedTab.showTitle()) {
			com.mojang.blaze3d.systems.RenderSystem.disableBlend();
			pGuiGraphics.drawString(this.font, selectedTab.getDisplayName(), 8, 6, selectedTab.getLabelColor(), false);
		}

	}

	/**
	 * Called when a mouse button is clicked within the GUI element.
	 * <p>
	 * @return {@code true} if the event is consumed, {@code false} otherwise.
	 * @param pMouseX the X coordinate of the mouse.
	 * @param pMouseY the Y coordinate of the mouse.
	 * @param pButton the button that was clicked.
	 */
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		if (pButton == 0) {
			double d0 = pMouseX - (double)this.leftPos;
			double d1 = pMouseY - (double)this.topPos;

			for(CreativeModeTab creativemodetab : currentPage.getVisibleTabs()) {
				if (this.checkTabClicked(creativemodetab, d0, d1)) {
					return true;
				}
			}

			if (selectedTab.getType() != CreativeModeTab.Type.INVENTORY && this.insideScrollbar(pMouseX, pMouseY)) {
				this.scrolling = this.canScroll();
				return true;
			}
		}

		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}

	/**
	 * Called when a mouse button is released within the GUI element.
	 * <p>
	 * @return {@code true} if the event is consumed, {@code false} otherwise.
	 * @param pMouseX the X coordinate of the mouse.
	 * @param pMouseY the Y coordinate of the mouse.
	 * @param pButton the button that was released.
	 */
	public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
		if (pButton == 0) {
			double d0 = pMouseX - (double)this.leftPos;
			double d1 = pMouseY - (double)this.topPos;
			this.scrolling = false;

			for(CreativeModeTab creativemodetab : currentPage.getVisibleTabs()) {
				if (this.checkTabClicked(creativemodetab, d0, d1)) {
					this.selectTab(creativemodetab);
					return true;
				}
			}
		}

		return super.mouseReleased(pMouseX, pMouseY, pButton);
	}

	/**
	 * Returns (if you are not on the inventoryTab) and (the flag isn't set) and (you have more than 1 page of items).
	 */
	private boolean canScroll() {
		return selectedTab.canScroll() && this.menu.canScroll();
	}

	/**
	 * Sets the current creative tab, restructuring the GUI as needed.
	 */
	private void selectTab(CreativeModeTab pTab) {
		if (pTab.getType() == CreativeModeTab.Type.HOTBAR || pTab.getType() == CreativeModeTab.Type.INVENTORY) return;

		CreativeModeTab creativemodetab = selectedTab;
		selectedTab = pTab;
		slotColor = pTab.getSlotColor();
		this.quickCraftSlots.clear();
		this.menu.items.clear();
		this.clearDraggingState();

		if (selectedTab.getType() == CreativeModeTab.Type.CATEGORY) {
			this.menu.items.addAll(onlyFluidContainers(selectedTab.getDisplayItems().stream()));
		}

		if (selectedTab.hasSearchBar()) {
			this.searchBox.setVisible(true);
			this.searchBox.setCanLoseFocus(false);
			this.searchBox.setFocused(true);
			if (creativemodetab != pTab) {
				this.searchBox.setValue("");
			}
			this.searchBox.setWidth(selectedTab.getSearchBarWidth());
			this.searchBox.setX(this.leftPos + (82 /*default left*/ + 89 /*default width*/) - this.searchBox.getWidth());

			this.refreshSearchResults();
		} else {
			this.searchBox.setVisible(false);
			this.searchBox.setCanLoseFocus(true);
			this.searchBox.setFocused(false);
			this.searchBox.setValue("");
		}

		this.scrollOffs = 0.0F;
		this.menu.scrollTo(0.0F);
	}

	/**
	 * Called when the mouse wheel is scrolled within the GUI element.
	 * <p>
	 * @return {@code true} if the event is consumed, {@code false} otherwise.
	 * @param pMouseX the X coordinate of the mouse.
	 * @param pMouseY the Y coordinate of the mouse.
	 * @param pScrollX the amount scrolled on the x-axis.
	 * @param pScrollY the amount scrolled on the y-axis.
	 */
	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
		if (!this.canScroll()) {
			return false;
		} else {
			this.scrollOffs = this.menu.subtractInputFromScroll(this.scrollOffs, pScrollY);
			this.menu.scrollTo(this.scrollOffs);
			return true;
		}
	}

	protected boolean hasClickedOutside(double pMouseX, double pMouseY, int pGuiLeft, int pGuiTop, int pMouseButton) {
		boolean flag = pMouseX < (double)pGuiLeft || pMouseY < (double)pGuiTop || pMouseX >= (double)(pGuiLeft + this.imageWidth) || pMouseY >= (double)(pGuiTop + this.imageHeight);
		this.hasClickedOutside = flag && !this.checkTabClicked(selectedTab, pMouseX, pMouseY);
		return this.hasClickedOutside;
	}

	protected boolean insideScrollbar(double pMouseX, double pMouseY) {
		int i = this.leftPos;
		int j = this.topPos;
		int k = i + 175;
		int l = j + 18;
		int i1 = k + 14;
		int j1 = l + 112;
		return pMouseX >= (double)k && pMouseY >= (double)l && pMouseX < (double)i1 && pMouseY < (double)j1;
	}

	/**
	 * Called when the mouse is dragged within the GUI element.
	 * <p>
	 * @return {@code true} if the event is consumed, {@code false} otherwise.
	 * @param pMouseX the X coordinate of the mouse.
	 * @param pMouseY the Y coordinate of the mouse.
	 * @param pButton the button that is being dragged.
	 * @param pDragX the X distance of the drag.
	 * @param pDragY the Y distance of the drag.
	 */
	public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
		if (this.scrolling) {
			int i = this.topPos + 18;
			int j = i + 112;
			this.scrollOffs = ((float)pMouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
			this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
			this.menu.scrollTo(this.scrollOffs);
			return true;
		} else {
			return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
		}
	}

	/**
	 * Renders the graphical user interface (GUI) element.
	 * @param pGuiGraphics the GuiGraphics object used for rendering.
	 * @param pMouseX the x-coordinate of the mouse cursor.
	 * @param pMouseY the y-coordinate of the mouse cursor.
	 * @param pPartialTick the partial tick time.
	 */
	public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

		for(CreativeModeTab creativemodetab : currentPage.getVisibleTabs()) {
			if (this.checkTabHovering(pGuiGraphics, creativemodetab, pMouseX, pMouseY)) {
				break;
			}
		}

		if (this.pages.size() != 1) {
			Component page = Component.literal(String.format("%d / %d", this.pages.indexOf(this.currentPage) + 1, this.pages.size()));
			pGuiGraphics.pose().pushPose();
			pGuiGraphics.pose().translate(0F, 0F, 300F);
			pGuiGraphics.drawString(font, page.getVisualOrderText(), leftPos + (imageWidth / 2) - (font.width(page) / 2), topPos - 44, -1);
			pGuiGraphics.pose().popPose();
		}

		if (shouldShowSelection) this.renderSelectedItems(pGuiGraphics);

		com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
		if (this.floatingEditBox != null) this.floatingEditBox.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
	}

	@Override
	protected void clearWidgets() {
		super.clearWidgets();
		if (this.pages.size() > 1) {
			addRenderableWidget(Button.builder(Component.literal("<"), b -> setCurrentPage(this.pages.get(Math.max(this.pages.indexOf(this.currentPage) - 1, 0)))).pos(leftPos,  topPos - 50).size(20, 20).build());
			addRenderableWidget(Button.builder(Component.literal(">"), b -> setCurrentPage(this.pages.get(Math.min(this.pages.indexOf(this.currentPage) + 1, this.pages.size() - 1)))).pos(leftPos + imageWidth - 20, topPos - 50).size(20, 20).build());
		}
		addWidget(this.searchBox);
	}

	@Override
	public List<Component> getTooltipFromContainerItem(ItemStack pStack) {
		if (this.floatingEditBox != null || this.hoveredSlot == null || !(this.hoveredSlot instanceof FluidSelectionScreen.CustomCreativeSlot)) return new ArrayList<>();
		boolean selected = isSelected(pStack);

		TooltipFlag.Default flag = this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
		List<Component> list = new ArrayList<>(pStack.getTooltipLines(Item.TooltipContext.EMPTY, this.minecraft.player, flag.asCreative()));

		IFluidHandlerItem handlerItem = pStack.getCapability(Capabilities.FluidHandler.ITEM);
		list.set(0, handlerItem.getFluidInTank(0).getFluid().getFluidType().getDescription());
		list.add(Component.empty());

		onTooltip(list, pStack);
		if (selected) onTooltipSelected(list, pStack); else onTooltipUnselected(list, pStack);
		if (list.get(list.size() - 1).getContents().toString().equals("empty")) list.remove(list.size() - 1);
		return list;
	}

	protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
		for(CreativeModeTab creativemodetab : currentPage.getVisibleTabs()) {
			if (creativemodetab != selectedTab && creativemodetab.getType() != CreativeModeTab.Type.HOTBAR && creativemodetab.getType() != CreativeModeTab.Type.INVENTORY) {
				this.renderTabButton(pGuiGraphics, creativemodetab);
			}
		}

		pGuiGraphics.blit(selectedTab.getBackgroundTexture(), this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		this.searchBox.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
		int j = this.leftPos + 175;
		int k = this.topPos + 18;
		int i = k + 112;
		if (selectedTab.canScroll()) {
			ResourceLocation resourcelocation = selectedTab.getScrollerSprite();
			pGuiGraphics.blitSprite(resourcelocation, j, k + (int)((float)(i - k - 17) * this.scrollOffs), 12, 15);
		}

		if (currentPage.getVisibleTabs().contains(selectedTab)) //Forge: only display tab selection when the selected tab is on the current page
			this.renderTabButton(pGuiGraphics, selectedTab);
	}

	private int getTabX(CreativeModeTab pTab) {
		int i = currentPage.getColumn(pTab);
		int j = 27;
		int k = 27 * i;
		if (pTab.isAlignedRight()) {
			k = this.imageWidth - 27 * (7 - i) + 1;
		}

		return k;
	}

	private int getTabY(CreativeModeTab pTab) {
		int i = 0;
		if (currentPage.isTop(pTab)) {
			i -= 32;
		} else {
			i += this.imageHeight;
		}

		return i;
	}

	protected boolean checkTabClicked(CreativeModeTab pCreativeModeTab, double pRelativeMouseX, double pRelativeMouseY) {
		int i = this.getTabX(pCreativeModeTab);
		int j = this.getTabY(pCreativeModeTab);
		return pRelativeMouseX >= (double)i && pRelativeMouseX <= (double)(i + 26) && pRelativeMouseY >= (double)j && pRelativeMouseY <= (double)(j + 32);
	}

	protected boolean checkTabHovering(GuiGraphics pGuiGraphics, CreativeModeTab pCreativeModeTab, int pMouseX, int pMouseY) {
		if (pCreativeModeTab.getType() == CreativeModeTab.Type.HOTBAR || pCreativeModeTab.getType() == CreativeModeTab.Type.INVENTORY) return false;

		int i = this.getTabX(pCreativeModeTab);
		int j = this.getTabY(pCreativeModeTab);
		if (this.isHovering(i + 3, j + 3, 21, 27, (double)pMouseX, (double)pMouseY)) {
			pGuiGraphics.renderTooltip(this.font, pCreativeModeTab.getDisplayName(), pMouseX, pMouseY);
			return true;
		} else {
			return false;
		}
	}

	protected void renderTabButton(GuiGraphics guiGraphics, CreativeModeTab creativeModeTab) {
		boolean flag = creativeModeTab == selectedTab;
		boolean flag1 = this.currentPage.isTop(creativeModeTab);
		int i = this.currentPage.getColumn(creativeModeTab);
		int j = this.leftPos + this.getTabX(creativeModeTab);
		int k = this.topPos - (flag1 ? 28 : -(this.imageHeight - 4));
		ResourceLocation[] aresourcelocation;
		if (flag1) {
			aresourcelocation = flag ? SELECTED_TOP_TABS : UNSELECTED_TOP_TABS;
		} else {
			aresourcelocation = flag ? SELECTED_BOTTOM_TABS : UNSELECTED_BOTTOM_TABS;
		}

		guiGraphics.blitSprite(aresourcelocation[Mth.clamp(i, 0, aresourcelocation.length)], j, k, 26, 32);
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
		j += 5;
		k += 8 + (flag1 ? 1 : -1);
		ItemStack itemstack = creativeModeTab.getIconItem();
		guiGraphics.renderItem(itemstack, j, k);
		guiGraphics.renderItemDecorations(this.font, itemstack, j, k);
		guiGraphics.pose().popPose();
	}

	/*protected void renderTabButton(GuiGraphics pGuiGraphics, CreativeModeTab pCreativeModeTab) {
		boolean flag = pCreativeModeTab == selectedTab;
		boolean flag1 = currentPage.isTop(pCreativeModeTab);
		int i = currentPage.getColumn(pCreativeModeTab);
		int j = i * 26;
		int k = 0;
		int l = this.leftPos + this.getTabX(pCreativeModeTab);
		int i1 = this.topPos;
		int j1 = 32;
		if (flag) {
			k += 32;
		}

		if (flag1) {
			i1 -= 28;
		} else {
			k += 64;
			i1 += this.imageHeight - 4;
		}

		com.mojang.blaze3d.systems.RenderSystem.enableBlend(); //Forge: Make sure blend is enabled else tabs show a white border.
		pGuiGraphics.blit(pCreativeModeTab.getTabsImage(), l, i1, j, k, 26, 32);
		pGuiGraphics.pose().pushPose();
		pGuiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
		l += 5;
		i1 += 8 + (flag1 ? 1 : -1);
		ItemStack itemstack = pCreativeModeTab.getIconItem();
		pGuiGraphics.renderItem(itemstack, l, i1);
		pGuiGraphics.renderItemDecorations(this.font, itemstack, l, i1);
		pGuiGraphics.pose().popPose();
	}*/

	protected void renderSelectedItems(GuiGraphics pGuiGraphics) {
		for (Slot slot : this.menu.slots) {
			boolean isHandled = Objects.equals(hoveredSlot, slot) && slot.isHighlightable();
			if (isHandled || !isSelected(slot.getItem())) continue;

			Pair<Integer, Integer> pos = getSlotPos(slot);
			renderSlotHighlight(pGuiGraphics, pos.getFirst(), pos.getSecond(), 0, selectedItemColor);
		}
	}

	protected Pair<Integer, Integer> getSlotPos(Slot slot) {
		return Pair.of(slot.x + getGuiLeft(), slot.y + getGuiTop());
	}

	public CreativeTabsScreenPage getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(CreativeTabsScreenPage currentPage) {
		this.currentPage = currentPage;
	}

	static class CustomCreativeSlot extends Slot {
		public CustomCreativeSlot(Container pContainer, int pSlot, int pX, int pY) {
			super(pContainer, pSlot, pX, pY);
		}

		/**
		 * Return whether this slot's stack can be taken from this slot.
		 */
		public boolean mayPickup(Player player) {
			return false;
		}
	}

	public static class ItemPickerMenu extends AbstractContainerMenu {
		/** The list of items in this container. */
		public final NonNullList<ItemStack> items = NonNullList.create();
		public final SimpleContainer CONTAINER = new SimpleContainer(45);

		protected ItemPickerMenu(int pContainerId, Inventory pPlayerInventory) {
			super(null, pContainerId);

			for(int i = 0; i < 5; ++i) {
				for(int j = 0; j < 9; ++j) {
					this.addSlot(new CustomCreativeSlot(this.CONTAINER, i * 9 + j, 9 + j * 18, 18 + i * 18));
				}
			}

			this.scrollTo(0.0F);
		}

		/**
		 * Determines whether supplied player can use this container
		 */
		public boolean stillValid(Player pPlayer) {
			return true;
		}

		protected int calculateRowCount() {
			return Mth.positiveCeilDiv(this.items.size(), 9) - 5;
		}

		protected int getRowIndexForScroll(float pScrollOffs) {
			return Math.max((int)((double)(pScrollOffs * (float)this.calculateRowCount()) + 0.5D), 0);
		}

		protected float getScrollForRowIndex(int pRowIndex) {
			return Mth.clamp((float)pRowIndex / (float)this.calculateRowCount(), 0.0F, 1.0F);
		}

		protected float subtractInputFromScroll(float pScrollOffs, double pInput) {
			return Mth.clamp(pScrollOffs - (float)(pInput / (double)this.calculateRowCount()), 0.0F, 1.0F);
		}

		/**
		 * Updates the gui slot's ItemStacks based on scroll position.
		 */
		public void scrollTo(float pPos) {
			int i = this.getRowIndexForScroll(pPos);

			for(int j = 0; j < 5; ++j) {
				for(int k = 0; k < 9; ++k) {
					int l = k + (j + i) * 9;
					if (l >= 0 && l < this.items.size()) {
						this.CONTAINER.setItem(k + j * 9, this.items.get(l));
					} else {
						this.CONTAINER.setItem(k + j * 9, ItemStack.EMPTY);
					}
				}
			}
		}

		public boolean canScroll() {
			return this.items.size() > 45;
		}

		/**
		 * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
		 * inventory and the other inventory(s).
		 */
		public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
			return ItemStack.EMPTY;
		}

		/**
		 * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in
		 * is null for the initial slot that was double-clicked.
		 */
		public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
			return pSlot.container != this.CONTAINER;
		}

		/**
		 * Returns {@code true} if the player can "drag-spilt" items into this slot. Returns {@code true} by default.
		 * Called to check if the slot can be added to a list of Slots to split the held ItemStack across.
		 */
		public boolean canDragTo(Slot pSlot) {
			return pSlot.container != this.CONTAINER;
		}

		public ItemStack getCarried() {
			return ItemStack.EMPTY;
		}

		public void setCarried(ItemStack pStack) {}
	}

	static class SlotWrapper extends Slot {
		final Slot target;

		public SlotWrapper(Slot pSlot, int pIndex, int pX, int pY) {
			super(pSlot.container, pIndex, pX, pY);
			this.target = pSlot;
		}

		public void onTake(Player pPlayer, ItemStack pStack) {
			this.target.onTake(pPlayer, pStack);
		}

		/**
		 * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
		 */
		public boolean mayPlace(ItemStack pStack) {
			return this.target.mayPlace(pStack);
		}

		/**
		 * Helper function to get the stack in the slot.
		 */
		public ItemStack getItem() {
			return this.target.getItem();
		}

		/**
		 * Returns if this slot contains a stack.
		 */
		public boolean hasItem() {
			return this.target.hasItem();
		}

		public void setByPlayer(ItemStack pStack) {
			this.target.setByPlayer(pStack);
		}

		/**
		 * Helper method to put a stack in the slot.
		 */
		public void set(ItemStack pStack) {
			this.target.set(pStack);
		}

		/**
		 * Called when the stack in a Slot changes
		 */
		public void setChanged() {
			this.target.setChanged();
		}

		/**
		 * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the
		 * case of armor slots)
		 */
		public int getMaxStackSize() {
			return this.target.getMaxStackSize();
		}

		public int getMaxStackSize(ItemStack pStack) {
			return this.target.getMaxStackSize(pStack);
		}

		@Nullable
		public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
			return this.target.getNoItemIcon();
		}

		/**
		 * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
		 * stack.
		 */
		public ItemStack remove(int pAmount) {
			return this.target.remove(pAmount);
		}

		/**
		 * Actually only call when we want to render the white square effect over the slots. Return always True, except
		 * for the armor slot of the Donkey/Mule (we can't interact with the Undead and Skeleton horses)
		 */
		public boolean isActive() {
			return this.target.isActive();
		}

		/**
		 * Return whether this slot's stack can be taken from this slot.
		 */
		public boolean mayPickup(Player pPlayer) {
			return this.target.mayPickup(pPlayer);
		}

		@Override
		public int getSlotIndex() {
			return this.target.getSlotIndex();
		}

		@Override
		public boolean isSameInventory(Slot other) {
			return this.target.isSameInventory(other);
		}

		@Override
		public Slot setBackground(ResourceLocation atlas, ResourceLocation sprite) {
			this.target.setBackground(atlas, sprite);
			return this;
		}
	}
}
