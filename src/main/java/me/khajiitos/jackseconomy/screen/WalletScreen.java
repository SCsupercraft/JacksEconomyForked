package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.JacksEconomyClient;
import me.khajiitos.jackseconomy.config.ClientConfig;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.menu.WalletMenu;
import me.khajiitos.jackseconomy.packet.CreateCheckPacket;
import me.khajiitos.jackseconomy.packet.DepositAllPacket;
import me.khajiitos.jackseconomy.packet.WithdrawBalanceSpecificPacket;
import me.khajiitos.jackseconomy.screen.widget.CheckCreatorWidget;
import me.khajiitos.jackseconomy.screen.widget.SimpleButton;
import me.khajiitos.jackseconomy.screen.widget.TextBox;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import me.khajiitos.jackseconomy.util.CurrencyType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static me.khajiitos.jackseconomy.screen.ShoppingCartScreen.BALANCE_PROGRESS;

public class WalletScreen extends AbstractContainerScreen<WalletMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/wallet.png");
    private static final ResourceLocation ADMIN_SHOP_ICON = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/admin_shop_icon.png");
    private static final ResourceLocation ID_CARD = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/id_card.png");

    private List<Component> tooltip;
    private boolean tooltipShift = false;
    private final List<ClickableCurrencyItem> clickableCurrencyItems = new ArrayList<>();
    private final ItemStack itemStack;

    private TextBox balanceTextbox;


    public WalletScreen(WalletMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.itemStack = pMenu.getItemStack();
        this.imageHeight = 198;

        this.titleLabelY = -100;
        this.inventoryLabelY = -100;
    }

    public BigDecimal getBalance() {
        return WalletItem.getBalance(this.itemStack);
    }

    @Override
    protected void init() {
        super.init();

        this.addClickableCurrencyItems();

        this.addRenderableWidget(new CheckCreatorWidget(this.leftPos - 21, this.topPos + 1, value -> {
            if (value.compareTo(BigDecimal.ZERO) > 0 && value.compareTo(WalletItem.getBalance(itemStack)) <= 0) {
                Packets.sendToServer(new CreateCheckPacket(value));
            }
        }, tooltip -> this.tooltip = tooltip));

        balanceTextbox = this.addRenderableWidget(new TextBox(this.leftPos + 70, this.topPos + 10, 101, 15, "", 0xFFBBBBBB));

        this.addRenderableWidget(new SimpleButton(this.leftPos + 6, this.topPos + 90, 56, 16, Component.translatable("jackseconomy.deposit_all"), b -> {
            Packets.sendToServer(new DepositAllPacket());
        }));
    }

    private void addClickableCurrencyItems() {
        this.clickableCurrencyItems.clear();

        this.clickableCurrencyItems.add(new ClickableCurrencyItem(this.leftPos + 2, this.topPos + 14, 16, 16, CurrencyType.PENNY));
        this.clickableCurrencyItems.add(new ClickableCurrencyItem(this.leftPos + 18, this.topPos + 14, 16, 16, CurrencyType.NICKEL));
        this.clickableCurrencyItems.add(new ClickableCurrencyItem(this.leftPos + 34, this.topPos + 14, 16, 16, CurrencyType.DIME));
        this.clickableCurrencyItems.add(new ClickableCurrencyItem(this.leftPos + 50, this.topPos + 14, 16, 16, CurrencyType.QUARTER));

        this.clickableCurrencyItems.add(new ClickableCurrencyItem(this.leftPos + 3, this.topPos - 4, 9, 16, CurrencyType.DOLLAR_BILL));
        this.clickableCurrencyItems.add(new ClickableCurrencyItem(this.leftPos + 12, this.topPos - 4, 9, 16, CurrencyType.FIVE_DOLLAR_BILL));
        this.clickableCurrencyItems.add(new ClickableCurrencyItem(this.leftPos + 21, this.topPos - 4, 9, 16, CurrencyType.TEN_DOLLAR_BILL));
        this.clickableCurrencyItems.add(new ClickableCurrencyItem(this.leftPos + 30, this.topPos - 4, 9, 16, CurrencyType.TWENTY_DOLLAR_BILL));
        this.clickableCurrencyItems.add(new ClickableCurrencyItem(this.leftPos + 39, this.topPos - 4, 9, 16, CurrencyType.FIFTY_DOLLAR_BILL));
        this.clickableCurrencyItems.add(new ClickableCurrencyItem(this.leftPos + 48, this.topPos - 4, 9, 16, CurrencyType.HUNDRED_DOLLAR_BILL));
        this.clickableCurrencyItems.add(new ClickableCurrencyItem(this.leftPos + 57, this.topPos - 4, 9, 16, CurrencyType.THOUSAND_DOLLAR_BILL));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        tooltip = null;
        tooltipShift = false;
        this.renderBackground(guiGraphics);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        guiGraphics.blit(BACKGROUND, this.leftPos, (this.height - this.imageHeight) / 2, 0, 0, this.imageWidth, this.imageHeight);

        //for (int i = 0; i < 4; i++) {
        //    guiGraphics.renderItem(new ItemStack(CoinType.values()[i].item), this.leftPos + 134, this.topPos + 34 + 18 * i);
        //}

        BigDecimal balance = WalletItem.getBalance(itemStack);
        this.balanceTextbox.setText(CurrencyHelper.format(balance));
    }

    private static void drawCenteredStringNoShadow(GuiGraphics guiGraphics, Font pFont, Component pText, int pX, int pY, int pColor) {
        FormattedCharSequence formattedcharsequence = pText.getVisualOrderText();
        guiGraphics.drawString(pFont, formattedcharsequence, (int)(pX - pFont.width(formattedcharsequence) / 2), (int)pY, pColor, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);

        guiGraphics.blit(ID_CARD, this.leftPos + 75, this.topPos + 40, 0/*this.getBlitOffset()*/, 0, 0, 91, 44, 91, 44);

        if (Minecraft.getInstance().player != null) {
            RenderSystem.setShaderTexture(0, Minecraft.getInstance().player.getSkinTextureLocation());
            PlayerFaceRenderer.draw(guiGraphics, Minecraft.getInstance().player.getSkinTextureLocation(), this.leftPos + 75 + 4, this.topPos + 40 + 15, 25);
        }

        if (this.itemStack.getItem() instanceof WalletItem walletItem && WalletItem.getBalance(itemStack).compareTo(BigDecimal.valueOf(walletItem.getCapacity())) > 0) {
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable("jackseconomy.capacity_overflow_reached").withStyle(ChatFormatting.RED), this.width / 2, this.topPos - 15, 0xFFFFFFFF);
        }

        Component header = Component.translatable("jackseconomy.item_owner", Component.literal(Minecraft.getInstance().player.getScoreboardName()), this.itemStack.getItem().getDescription());

        int headerWidth = Minecraft.getInstance().font.width(header);
        int space = 82;

        float scale;
        if (headerWidth > space) {
            scale = (float)space / headerWidth;
        } else {
            scale = 1.f;
        }

        if (scale != 1.f) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(scale, scale, scale);
        }

        guiGraphics.drawString(Minecraft.getInstance().font, header, (int)((this.leftPos + 78) / scale), (int)((this.topPos + 43) / scale), 0xFF000000, false);

        if (scale != 1.f) {
            guiGraphics.pose().popPose();
        }

        float contentScale = 0.6f;
        float contentScaleInv = 1.f / contentScale;

        AtomicInteger lineCount = new AtomicInteger();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(contentScale, contentScale, contentScale);

        Minecraft.getInstance().font.getSplitter().splitLines(Component.translatable("jackseconomy.thanks_for_using"), (int)(57 * contentScaleInv), Style.EMPTY, (line, idk) -> {
            int count = lineCount.getAndAdd(1);
            guiGraphics.drawString(Minecraft.getInstance().font, line.getString(), (int)((this.leftPos + 108) * contentScaleInv), (int)((this.topPos + 59 + count * 6) * contentScaleInv), 0xFF000000, false);
        });

        guiGraphics.pose().popPose();

        if (itemStack.getItem() instanceof WalletItem walletItem) {
            BigDecimal balance = WalletItem.getBalance(itemStack);
            BigDecimal capacity = BigDecimal.valueOf(walletItem.getCapacity());
            double progress = balance.divide(capacity, RoundingMode.DOWN).min(BigDecimal.ONE).doubleValue();

            guiGraphics.blit(BALANCE_PROGRESS, this.leftPos + 70, this.topPos + 28, 0/*this.getBlitOffset()*/, 0, 65, 101, 5, 256, 256);
            guiGraphics.blit(BALANCE_PROGRESS, this.leftPos + 70, this.topPos + 28, 0/*this.getBlitOffset()*/, 0, 70, ((int)(101 * progress)), 5, 256, 256);

            if (pMouseX >= this.leftPos + 70 && pMouseX <= this.leftPos + 70 + 101 && pMouseY >= this.topPos + 28 && pMouseY <= this.topPos + 28 + 5) {
                tooltip = List.of(Component.translatable("jackseconomy.max_storage", Component.literal(CurrencyHelper.format(capacity)).withStyle(ChatFormatting.GOLD), Component.literal((int)(progress * 100) + "%").withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.YELLOW));
            }
        }

        //guiGraphics.renderTooltip(Minecraft.getInstance().font, this.tooltip, Optional.empty(), pMouseX, pMouseY);

        guiGraphics.blit(BACKGROUND, this.leftPos + 3, this.topPos + 6, 176, 0, 65, 4);
        guiGraphics.blit(BACKGROUND, this.leftPos + 3, this.topPos + 21, 176, 0, 65, 4);

        // Render hovered items last because they're larger and need to be drawn over others
        for (ClickableCurrencyItem item : this.clickableCurrencyItems.stream().sorted(Comparator.comparing(item -> (pMouseX > item.x() && pMouseX <= item.x() + item.width && pMouseY > item.y && pMouseY <= item.y + item.height) ? 1 : 0)).toList()) {
            boolean hovered = pMouseX > item.x() && pMouseX <= item.x() + item.width && pMouseY > item.y && pMouseY <= item.y + item.height;

            if (hovered) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().scale(1.25f, 1.25f, 1.25f);
                guiGraphics.pose().translate(-1.0, -2.5, 0.0);
                RenderSystem.setShaderColor(1.25f, 1.25f, 1.25f, 1.f);

                boolean shiftHeld = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT);
                boolean ctrlHeld = !shiftHeld && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL);
                boolean nothingHeld = !shiftHeld && !ctrlHeld;
                this.tooltip = List.of(
                        item.currencyType.item.getDescription(),
                        Component.translatable("jackseconomy.click_to_withdraw").withStyle(ChatFormatting.GRAY),
                        Component.translatable("jackseconomy.shift_coins").withStyle(shiftHeld ? ChatFormatting.AQUA : ChatFormatting.GRAY),
                        Component.translatable("jackseconomy.ctrl_coins").withStyle(ctrlHeld ? ChatFormatting.AQUA : ChatFormatting.GRAY),
                        Component.translatable("jackseconomy.normal_coins").withStyle(nothingHeld ? ChatFormatting.AQUA : ChatFormatting.GRAY)
                );
                this.tooltipShift = true;
            }

            guiGraphics.blit(item.getTexture(), (int)(item.x() * (hovered ? (1.f / 1.25f) : 1.f)), (int)(item.y * (hovered ? (1.f / 1.25f) : 1.f)), 0, 0, item.width, item.height, item.width, item.height);

            if (hovered) {
                guiGraphics.pose().popPose();
                RenderSystem.setShaderColor(1.f, 1.f, 1.f, 1.f);
            }
        }

        RenderSystem.setShaderTexture(0, BACKGROUND);
        guiGraphics.blit(BACKGROUND, this.leftPos + 3, this.topPos + 10, 176, 4, 67, 5);
        guiGraphics.blit(BACKGROUND, this.leftPos + 3, this.topPos + 25, 176, 4, 67, 5);

        if (JacksEconomyClient.balanceDifPopup != null) {
            long timeDelta = System.currentTimeMillis() - JacksEconomyClient.balanceDifPopupStartMillis;
            long maxTimeDelta = (long)(ClientConfig.balanceChangePopupTime.get() * 1000);

            int alpha;

            if (timeDelta > maxTimeDelta) {
                JacksEconomyClient.balanceDifPopup = null;
                JacksEconomyClient.balanceDifPopupStartMillis = -1;
                return;
            }

            if (timeDelta < 500) {
                alpha = (int)(255 * Mth.lerp(timeDelta / 500.0, 0.0, 1.0));
            } else if (timeDelta >= maxTimeDelta - 500) {
                alpha = (int)(255 * Mth.lerp((maxTimeDelta - timeDelta) / 500.0, 0.0, 1.0));
            } else {
                alpha = 255;
            }

            if (alpha < 0 || alpha > 256) {
                int a = 1;
            }

            String balanceDif = CurrencyHelper.formatShortened(JacksEconomyClient.balanceDifPopup);
            boolean positive = JacksEconomyClient.balanceDifPopup.compareTo(BigDecimal.ZERO) > 0;

            Component balanceDifComponent = Component.literal((positive ? "(+" : "(") + balanceDif + ")").withStyle(positive ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_RED);

            drawCenteredStringNoShadow(guiGraphics, Minecraft.getInstance().font, balanceDifComponent, this.leftPos + 120, this.topPos + 2, 0x00FFFFFF | (alpha << 24));
        }

        this.renderTooltip(guiGraphics, pMouseX, pMouseY);

        if (tooltip != null) {
            if (tooltipShift) {
                int maxWidth = tooltip.stream().map(a -> Minecraft.getInstance().font.width(a)).max(Comparator.naturalOrder()).orElse(0);
                guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(), pMouseX - maxWidth / 2 - 10, this.topPos + 48);
            } else {
                guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(), pMouseX, pMouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {

        if (pButton == 0) {
            for (ClickableCurrencyItem item : this.clickableCurrencyItems) {
                if (pMouseX > item.x() && pMouseX <= item.x() + item.width && pMouseY > item.y && pMouseY <= item.y + item.height) {
                    BigDecimal count;

                    BigDecimal balance = getBalance();
                    BigDecimal worth = item.currencyType.worth;

                    if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                        count = BigDecimal.valueOf(64).min(balance.divide(worth, RoundingMode.DOWN));
                    } else if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)) {
                        count = BigDecimal.valueOf(10).min(balance.divide(worth, RoundingMode.DOWN));
                    } else {
                        count = BigDecimal.valueOf(1).min(balance.divide(worth, RoundingMode.DOWN));
                    }

                    if (count.compareTo(BigDecimal.ZERO) > 0) {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.F));
                        Packets.sendToServer(new WithdrawBalanceSpecificPacket(count, item.currencyType));
                        return true;
                    }

                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 0.9F));
                    return false;
                }
            }
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    private record ClickableCurrencyItem(int x, int y, int width, int height, CurrencyType currencyType) {
        private static final ResourceLocation PENNY_TEXTURE = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/wallet_items/penny.png");
        private static final ResourceLocation NICKEL_TEXTURE = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/wallet_items/nickel.png");
        private static final ResourceLocation DIME_TEXTURE = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/wallet_items/dime.png");
        private static final ResourceLocation QUARTER_TEXTURE = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/wallet_items/quarter.png");
        private static final ResourceLocation DOLLAR_BILL_TEXTURE = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/wallet_items/dollar_bill.png");
        private static final ResourceLocation FIVE_DOLLAR_BILL_TEXTURE = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/wallet_items/five_dollar_bill.png");
        private static final ResourceLocation TEN_DOLLAR_BILL_TEXTURE = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/wallet_items/ten_dollar_bill.png");
        private static final ResourceLocation TWENTY_DOLLAR_BILL_TEXTURE = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/wallet_items/twenty_dollar_bill.png");
        private static final ResourceLocation FIFTY_DOLLAR_BILL_TEXTURE = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/wallet_items/fifty_dollar_bill.png");
        private static final ResourceLocation HUNDRED_DOLLAR_BILL_TEXTURE = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/wallet_items/hundred_dollar_bill.png");
        private static final ResourceLocation THOUSAND_DOLLAR_BILL_TEXTURE = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/wallet_items/thousand_dollar_bill.png");

        public ResourceLocation getTexture() {
            return switch (this.currencyType) {
                case PENNY -> PENNY_TEXTURE;
                case NICKEL -> NICKEL_TEXTURE;
                case DIME -> DIME_TEXTURE;
                case QUARTER -> QUARTER_TEXTURE;
                case DOLLAR_BILL -> DOLLAR_BILL_TEXTURE;
                case FIVE_DOLLAR_BILL -> FIVE_DOLLAR_BILL_TEXTURE;
                case TEN_DOLLAR_BILL -> TEN_DOLLAR_BILL_TEXTURE;
                case TWENTY_DOLLAR_BILL -> TWENTY_DOLLAR_BILL_TEXTURE;
                case FIFTY_DOLLAR_BILL -> FIFTY_DOLLAR_BILL_TEXTURE;
                case HUNDRED_DOLLAR_BILL -> HUNDRED_DOLLAR_BILL_TEXTURE;
                case THOUSAND_DOLLAR_BILL -> THOUSAND_DOLLAR_BILL_TEXTURE;
            };
        }
    }
}
