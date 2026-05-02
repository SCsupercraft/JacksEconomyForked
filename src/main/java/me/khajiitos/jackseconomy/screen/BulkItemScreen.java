package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.platform.InputConstants;
import me.khajiitos.jackseconomy.JacksEconomyClient;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.packet.UpdateItemPricesPacket;
import me.khajiitos.jackseconomy.screen.widget.FloatingEditBoxWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class BulkItemScreen extends ItemSelectionScreen<BulkItemScreen.Menu> {
	protected int itemsWithBuyPrices = 0;
	protected int itemsWithSellPrices = 0;

	public BulkItemScreen(Menu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	protected void refreshItemsWith() {
		itemsWithBuyPrices = 0;
		itemsWithSellPrices = 0;
		super.refreshItemsWith();
	}

	@Override
	protected void removeItemsWith(ItemDescription description) {
		if (!JacksEconomyClient.priceInfos.containsKey(description)) return;

		boolean hasBuyPrice = JacksEconomyClient.priceInfos.get(description).importerBuyPrice != -1;
		boolean hasSellPrice = JacksEconomyClient.priceInfos.get(description).sellPrice != -1;

		if (hasBuyPrice) itemsWithBuyPrices--;
		if (hasSellPrice) itemsWithSellPrices--;
	}

	@Override
	protected void addItemsWith(ItemDescription description) {
		if (!JacksEconomyClient.priceInfos.containsKey(description)) return;

		boolean hasBuyPrice = JacksEconomyClient.priceInfos.get(description).importerBuyPrice != -1;
		boolean hasSellPrice = JacksEconomyClient.priceInfos.get(description).sellPrice != -1;

		if (hasBuyPrice) itemsWithBuyPrices++;
		if (hasSellPrice) itemsWithSellPrices++;
	}

	@Override
	protected void onTooltipSelected(List<Component> components, ItemStack stack) {
		super.onTooltipSelected(components, stack);
		components.add(Component.translatable("jackseconomy.bulk_set_sell_price").withStyle(ChatFormatting.AQUA));
		components.add(Component.translatable("jackseconomy.bulk_set_buy_price").withStyle(ChatFormatting.AQUA));
		if (itemsWithSellPrices != 0) components.add(Component.translatable("jackseconomy.bulk_remove_sell_price").withStyle(ChatFormatting.RED));
		if (itemsWithBuyPrices != 0) components.add(Component.translatable("jackseconomy.bulk_remove_buy_price").withStyle(ChatFormatting.RED));
	}

	@Override
	protected void onSelectedSlotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
		boolean ctrl = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL);
		switch (pMouseButton) {
			case 1 -> { // Right-click
				if (ctrl) setImporter(pSlot); else setExporter(pSlot);
				return;
			}
			case 2 -> { // Middle-click
				if (ctrl) removeImporter(pSlot); else removeExporter(pSlot);
				return;
			}
		}
		super.onSelectedSlotClicked(pSlot, pSlotId, pMouseButton, pType);
	}

	private void setExporter(Slot slot) {
		this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, getGuiLeft() + imageWidth / 2, getGuiTop() + imageHeight + 28, imageWidth, 15, FloatingEditBoxWidget.Type.DECIMAL, (value) -> {
			try {
				List<Pair<ItemDescription, Double>> prices = new ArrayList<>();
				double newPrice = Double.parseDouble(value);
				for (ItemStack stack : selectedItems) {
					ItemDescription description = ItemDescription.ofItem(stack);
					prices.add(Pair.of(description, newPrice));
				}
				Packets.sendToServer(new UpdateItemPricesPacket(UpdateItemPricesPacket.Type.EXPORTER, prices));
			} catch (NumberFormatException ignored) {}
			this.removeWidget(this.floatingEditBox);
			this.floatingEditBox = null;
		}));
		this.setFocused(this.floatingEditBox);
	}

	private void setImporter(Slot slot) {
		this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, getGuiLeft() + imageWidth / 2, getGuiTop() + imageHeight + 28, imageWidth, 15, FloatingEditBoxWidget.Type.DECIMAL, (value) -> {
			try {
				List<Pair<ItemDescription, Double>> prices = new ArrayList<>();
				double newPrice = Double.parseDouble(value);
				for (ItemStack stack : selectedItems) {
					ItemDescription description = ItemDescription.ofItem(stack);
					prices.add(Pair.of(description, newPrice));
				}
				Packets.sendToServer(new UpdateItemPricesPacket(UpdateItemPricesPacket.Type.IMPORTER, prices));
			} catch (NumberFormatException ignored) {}
			this.removeWidget(this.floatingEditBox);
			this.floatingEditBox = null;
		}));
		this.setFocused(this.floatingEditBox);
	}

	private void removeExporter(Slot slot) {
		List<Pair<ItemDescription, Double>> prices = new ArrayList<>();
		for (ItemStack stack : selectedItems) {
			ItemDescription description = ItemDescription.ofItem(stack);
			prices.add(Pair.of(description, -1d));
		}
		Packets.sendToServer(new UpdateItemPricesPacket(UpdateItemPricesPacket.Type.EXPORTER, prices));
	}

	private void removeImporter(Slot slot) {
		List<Pair<ItemDescription, Double>> prices = new ArrayList<>();
		for (ItemStack stack : selectedItems) {
			ItemDescription description = ItemDescription.ofItem(stack);
			prices.add(Pair.of(description, -1d));
		}
		Packets.sendToServer(new UpdateItemPricesPacket(UpdateItemPricesPacket.Type.IMPORTER, prices));
	}

	public static class Menu extends ItemPickerMenu {
		public Menu(int pContainerId, Inventory pPlayerInventory) {
			super(pContainerId, pPlayerInventory);
		}

		@Override
		public MenuType<?> getType() {
			return ContainerReg.BULK_ITEM_MENU.get();
		}
	}
}
