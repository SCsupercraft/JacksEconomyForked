package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import me.khajiitos.jackseconomy.gamestages.GameStagesManager;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import me.khajiitos.jackseconomy.init.Sounds;
import me.khajiitos.jackseconomy.item.GoldenWalletItem;
import me.khajiitos.jackseconomy.item.OIMWalletItem;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.menu.AdminShopMenu;
import me.khajiitos.jackseconomy.packet.AdminShopPurchasePacket;
import me.khajiitos.jackseconomy.screen.widget.BetterScrollPanel;
import me.khajiitos.jackseconomy.screen.widget.ShoppingCartEntry;
import me.khajiitos.jackseconomy.screen.widget.ShoppingCartSellEntry;
import me.khajiitos.jackseconomy.screen.widget.TextWidget;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ShoppingCartScreen extends AbstractContainerScreen<AdminShopMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "textures/gui/shopping_cart.png");
    private static final ResourceLocation NO_WALLET = ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "textures/gui/no_wallet.png");
    protected static final ResourceLocation BALANCE_PROGRESS = ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "textures/gui/balance_progress.png");

    public final AdminShopScreen parent;
    private BetterScrollPanel shoppingCartPanel;
    private List<Component> tooltip;
    private Button purchaseButton;
    private final boolean oneItemCurrencyMode;

    public ShoppingCartScreen(AdminShopMenu pMenu, Inventory pPlayerInventory, AdminShopScreen parent) {
        super(pMenu, pPlayerInventory, Component.empty());
        this.parent = parent;
        this.imageHeight = 232;
        this.inventoryLabelY = this.imageHeight - 94;
        this.oneItemCurrencyMode = parent.oneItemCurrencyMode;
    }

    public BigDecimal getShoppingCartValue() {
        BigDecimal value = BigDecimal.ZERO;
        for (Map.Entry<AdminShopScreen.CategorizedShopItem, Integer> items : this.parent.shoppingCart.entrySet()) {
            value = value.add(BigDecimal.valueOf(items.getKey().price()).multiply(new BigDecimal(items.getValue())));
        }
        return value;
    }

    public BigDecimal getSoldValue() {
        BigDecimal value = BigDecimal.ZERO;
        for (Map.Entry<ItemDescription, Integer> item : this.parent.itemsToSell.entrySet()) {
            BigDecimal itemValue = BigDecimal.valueOf(this.parent.sellPrices.get(item.getKey()).worth());
            value = value.add(itemValue.multiply(new BigDecimal(item.getValue())));
        }
        return value;
    }

    public void addPurchaseButton() {
        ItemStack wallet = CuriosWallet.get(Minecraft.getInstance().player);
        BigDecimal toPay = getShoppingCartValue().subtract(getSoldValue());
        boolean canAfford;

        if (wallet.getItem() instanceof GoldenWalletItem) {
            canAfford = true;
        } else if (this.oneItemCurrencyMode) {
            canAfford = OIMWalletItem.getTotalDollars(wallet, Minecraft.getInstance().player) >= toPay.longValue();
        } else {
            canAfford = !wallet.isEmpty() && WalletItem.getBalance(wallet).compareTo(toPay) >= 0;
        }

        MutableComponent buttonName;
        if (!this.parent.itemsToSell.isEmpty()) {
            if (this.parent.shoppingCart.isEmpty()) {
                buttonName = Component.translatable("jackseconomy.sell");
            } else {
                buttonName = Component.translatable("jackseconomy.purchase_and_sell");
            }
        } else {
            buttonName = Component.translatable("jackseconomy.purchase");
        }

        purchaseButton = this.addRenderableWidget(Button.builder(buttonName.withStyle((canAfford && !(this.parent.shoppingCart.isEmpty() && this.parent.itemsToSell.isEmpty())) ? ChatFormatting.GREEN : ChatFormatting.RED), b -> {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(Sounds.CHECKOUT.get(), 1.0F));

            Map<AdminShopPurchasePacket.ShopItemDescription, Integer> map = new HashMap<>();
            Map<ItemDescription, Integer> sellMap = new LinkedHashMap<>(this.parent.itemsToSell);
            this.parent.shoppingCart.forEach((shopItem, amount) -> map.put(new AdminShopPurchasePacket.ShopItemDescription(shopItem.itemDescription(), shopItem.slot(), shopItem.category()), amount));
            PacketDistributor.sendToServer(new AdminShopPurchasePacket(map, sellMap, Optional.ofNullable(parent.adminShopName)));

            this.parent.shoppingCart.clear();
            this.parent.itemsToSell.clear();
            this.clearWidgets();
            this.init();
        }).bounds(this.leftPos + 82, this.topPos + 125, 87, 20).build());

        if (!canAfford || (this.parent.shoppingCart.isEmpty() && this.parent.itemsToSell.isEmpty())) {
            purchaseButton.active = false;
        }
    }

    @Override
    protected void init() {
        super.init();
        this.shoppingCartPanel = this.addRenderableWidget(new BetterScrollPanel(this.minecraft, this.leftPos + 7, this.topPos + 12, 162, 109));

        if (!parent.shoppingCart.isEmpty()) {
            this.shoppingCartPanel.children.add(new TextWidget(0, 0, 162, Component.translatable("jackseconomy.buying").withStyle(ChatFormatting.GRAY)));
        }

        for (Map.Entry<AdminShopScreen.CategorizedShopItem, Integer> shoppingCartEntry : parent.shoppingCart.entrySet()) {
            this.shoppingCartPanel.children.add(new ShoppingCartEntry(0, 0, 162, 20, this.oneItemCurrencyMode, shoppingCartEntry, () -> {
                this.removeWidget(purchaseButton);
                this.addPurchaseButton();
            }, () -> {
                parent.shoppingCart.remove(shoppingCartEntry.getKey());
                this.clearWidgets();
                this.init();
            }, this.shoppingCartPanel));
        }

        if (!parent.itemsToSell.isEmpty()) {
            this.shoppingCartPanel.children.add(new TextWidget(0, 0, 162, Component.translatable("jackseconomy.selling").withStyle(ChatFormatting.GRAY)));
        }

        for (Map.Entry<ItemDescription, Integer> itemToSell : parent.itemsToSell.entrySet()) {
            assert Minecraft.getInstance().player != null;
            this.shoppingCartPanel.children.add(new ShoppingCartSellEntry(0, 0, 162, 20, this.oneItemCurrencyMode, itemToSell, parent.sellPrices.get(itemToSell.getKey()).worth(), Minecraft.getInstance().player.getInventory(), () -> {
                this.parent.reduceSellItemsIfMissing();
                this.removeWidget(purchaseButton);
                this.addPurchaseButton();
            }, () -> {
                parent.itemsToSell.remove(itemToSell.getKey());
                this.clearWidgets();
                this.init();
            }, this.shoppingCartPanel));
        }

        if (shoppingCartPanel.isScrollbarDisplayed()) {
            for (AbstractWidget child: shoppingCartPanel.children) {
                child.setWidth(156);
            }
        }

        this.addPurchaseButton();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShaderTexture(0, BACKGROUND);
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(BACKGROUND, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        tooltip = null;
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
        AdminShopScreen.renderDollarSignForSellableItems(guiGraphics, this.leftPos, this.topPos, this.getMenu().slots, this.parent.itemsToSell, this.parent.sellPrices);

        if (purchaseButton.isHovered()) {
            ItemStack wallet = CuriosWallet.get(Minecraft.getInstance().player);
            BigDecimal shoppingCartValue = getShoppingCartValue();
            BigDecimal soldValue = getSoldValue();
            BigDecimal totalValue = shoppingCartValue.subtract(soldValue);
            boolean canAfford;

            if (wallet.getItem() instanceof GoldenWalletItem) {
                canAfford = true;
            } else if (this.oneItemCurrencyMode) {
                canAfford = OIMWalletItem.getTotalDollars(wallet, Minecraft.getInstance().player) >= totalValue.longValue();
            } else {
                canAfford = !wallet.isEmpty() && WalletItem.getBalance(wallet).compareTo(totalValue) >= 0;
            }

            if (wallet.isEmpty() && !this.oneItemCurrencyMode) {
                tooltip = List.of(Component.translatable("jackseconomy.no_wallet").withStyle(ChatFormatting.YELLOW));
            } else if (!canAfford) {
                tooltip = List.of(Component.translatable("jackseconomy.cant_afford").withStyle(ChatFormatting.RED));
            } else if ((this.parent.shoppingCart.isEmpty() && this.parent.itemsToSell.isEmpty())) {
                tooltip = List.of(Component.translatable("jackseconomy.shopping_cart_empty").withStyle(ChatFormatting.YELLOW));
            } else {
                tooltip = new ArrayList<>();
                if (!this.parent.shoppingCart.isEmpty()) {
                    tooltip.add(Component.translatable("jackseconomy.bought_items", Component.literal(Config.oneItemCurrencyMode.get() ? "$" + shoppingCartValue.longValue() : CurrencyHelper.format(shoppingCartValue)).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.DARK_AQUA));
                }

                if (!this.parent.itemsToSell.isEmpty()) {
                    tooltip.add(Component.translatable("jackseconomy.sold_items", Component.literal(Config.oneItemCurrencyMode.get() ? "$" + soldValue.longValue() : CurrencyHelper.format(soldValue)).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.DARK_AQUA));
                }

                tooltip.add(Component.translatable("jackseconomy.total", Component.literal(Config.oneItemCurrencyMode.get() ? "$" + totalValue.longValue() : CurrencyHelper.format(totalValue)).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.DARK_AQUA));

                if (!this.parent.shoppingCart.isEmpty()) {
                    tooltip.add(Component.literal(" "));

                    tooltip.add(Component.translatable("jackseconomy.buying").withStyle(ChatFormatting.GRAY));
                    this.parent.shoppingCart.forEach(((shopItem, amount) -> {
                        tooltip.add(shopItem.itemDescription().item().value().getDescription().copy().withStyle(ChatFormatting.BLUE).append(Component.literal(" x" + amount).withStyle(ChatFormatting.BLUE)));
                    }));
                }

                if (!this.parent.itemsToSell.isEmpty()) {
                    tooltip.add(Component.literal(" "));

                    tooltip.add(Component.translatable("jackseconomy.selling").withStyle(ChatFormatting.GRAY));
                    this.parent.itemsToSell.forEach(((itemDescription, amount) -> {
                        tooltip.add(itemDescription.item().value().getDescription().copy().withStyle(ChatFormatting.BLUE).append(Component.literal(" x" + amount).withStyle(ChatFormatting.BLUE)));
                    }));
                }
            }
        }

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
        } else if (wallet == null || !(wallet.getItem() instanceof GoldenWalletItem)) {
            Component component = Component.translatable("jackseconomy.no_wallet").withStyle(ChatFormatting.DARK_RED);
            int width = this.font.width(component);
            guiGraphics.fill(this.leftPos + 181, this.topPos + 5, this.leftPos + 209 + width, this.topPos + 25, 0xFF4c4c4c);
            guiGraphics.fill(this.leftPos + 182, this.topPos + 6, this.leftPos + 208 + width, this.topPos + 24, 0xFFc6c6c6);
            guiGraphics.blit(NO_WALLET, this.leftPos + 183, this.topPos + 8, 0, 0, 0, 16, 16, 16, 16);
            guiGraphics.drawString(Minecraft.getInstance().font, component, this.leftPos + 203, this.topPos + 13, 0xFFFFFFFF, false);
        }

        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack itemstack = this.hoveredSlot.getItem();

            List<Component> tooltip = new ArrayList<>(this.getTooltipFromContainerItem(itemstack));

            ItemDescription itemDescription = ItemDescription.ofItem(itemstack);
            AdminShopScreen.ItemSellabilityInfo info = parent.sellPrices.get(itemDescription);
            String stage = info != null ? info.stage() : null;
            double price = info != null ? info.worth() : -1.0;

            if (!Config.disableAdminShopSelling.get()) {
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
                        tooltip.add(Component.translatable("jackseconomy.to_sell", Component.literal(String.valueOf(parent.itemsToSell.getOrDefault(itemDescription, 0))).withStyle(ChatFormatting.AQUA)));
                    } else {
                        tooltip.add(Component.translatable("jackseconomy.no_sell_price").withStyle(ChatFormatting.GRAY));
                    }
                }
            }

            guiGraphics.renderTooltip(this.font, tooltip, itemstack.getTooltipImage(), itemstack, pMouseX, pMouseY);
        }

        if (tooltip != null) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(), pMouseX, pMouseY);
        }
    }

    @Override
    protected void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, @NotNull ClickType pType) {
        // pSlot CAN BE GODDAMN NULL
        if (!Config.disableAdminShopSelling.get() && pSlot != null && (pType == ClickType.QUICK_MOVE || pType == ClickType.SWAP || pType == ClickType.PICKUP) && (pMouseButton == 0 || pMouseButton == 1)) {
            ItemDescription itemDescription = ItemDescription.ofItem(pSlot.getItem());
            AdminShopScreen.ItemSellabilityInfo info = parent.sellPrices.get(itemDescription);
            String stage = info != null ? info.stage() : null;

            if (stage != null && !GameStagesManager.hasGameStage(Minecraft.getInstance().player, stage)) {
                return;
            }

            if (parent.sellPrices.containsKey(itemDescription)) {
                int oldAmount = parent.itemsToSell.getOrDefault(itemDescription, 0);

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
                    parent.itemsToSell.put(itemDescription, Math.max(0, oldAmount + count));
                } else {
                    parent.itemsToSell.remove(itemDescription);
                }

                if (!parent.reduceSellItemsIfMissing()) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pMouseButton == 1 ? 0.75F : 1.25F));
                    this.clearWidgets();
                    this.init();
                }
            }
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        if (this.parent.reduceSellItemsIfMissing()) {
            this.clearWidgets();
            this.init();
        }
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.screen = parent;
        this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        return this.shoppingCartPanel.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)
                || super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }
}
