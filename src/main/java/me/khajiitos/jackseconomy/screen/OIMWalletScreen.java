package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.JacksEconomyClient;
import me.khajiitos.jackseconomy.config.ClientConfig;
import me.khajiitos.jackseconomy.item.OIMWalletItem;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.menu.OIMWalletMenu;
import me.khajiitos.jackseconomy.screen.widget.TextBox;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class OIMWalletScreen extends AbstractContainerScreen<OIMWalletMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/oim_wallet.png");
    private static final ResourceLocation ID_CARD = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/id_card.png");

    private List<Component> tooltip;
    private boolean tooltipShift = false;
    private final ItemStack itemStack;

    private TextBox balanceTextbox;


    public OIMWalletScreen(OIMWalletMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
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
        balanceTextbox = this.addRenderableWidget(new TextBox(this.leftPos + 70, this.topPos + 10, 101, 15, "", 0xFFBBBBBB));
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

        long balance = OIMWalletItem.getDollars(itemStack);
        this.balanceTextbox.setText("$" + balance);
    }

    private static void drawCenteredStringNoShadow(GuiGraphics guiGraphics, Font pFont, Component pText, int pX, int pY, int pColor) {
        FormattedCharSequence formattedcharsequence = pText.getVisualOrderText();
        guiGraphics.drawString(pFont, formattedcharsequence, pX - pFont.width(formattedcharsequence) / 2, pY, pColor, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);

        guiGraphics.blit(ID_CARD, this.leftPos + 75, this.topPos + 40, 0/*this.getBlitOffset()*/, 0, 0, 91, 44, 91, 44);

        if (Minecraft.getInstance().player != null) {
            RenderSystem.setShaderTexture(0, Minecraft.getInstance().player.getSkinTextureLocation());
            PlayerFaceRenderer.draw(guiGraphics, Minecraft.getInstance().player.getSkinTextureLocation(), this.leftPos + 75 + 4, this.topPos + 40 + 15, 25);
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
}
