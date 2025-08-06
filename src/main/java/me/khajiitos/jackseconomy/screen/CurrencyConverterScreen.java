package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.blockentity.CurrencyConverterBlockEntity;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.menu.CurrencyConverterMenu;
import me.khajiitos.jackseconomy.packet.ChangeCurrencyTypePacket;
import me.khajiitos.jackseconomy.screen.widget.CurrencyToggleButton;
import me.khajiitos.jackseconomy.screen.widget.SideConfigWidget;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import me.khajiitos.jackseconomy.util.CurrencyType;
import me.khajiitos.jackseconomy.util.SideConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static me.khajiitos.jackseconomy.screen.ShoppingCartScreen.BALANCE_PROGRESS;


public class CurrencyConverterScreen extends AbstractContainerScreen<CurrencyConverterMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "textures/gui/currency_converter.png");
    private CurrencyType currencyType;
    private final CurrencyConverterBlockEntity blockEntity;
    private List<Component> tooltip;
    protected SideConfigWidget sideConfig;

    public CurrencyConverterScreen(CurrencyConverterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, Component.empty());
        this.imageHeight = 177;
        this.inventoryLabelY = this.imageHeight - 96;

        this.currencyType = pMenu.blockEntity.selectedCurrencyType;
        this.blockEntity = pMenu.blockEntity;
    }

    protected @Nullable SideConfig getSideConfig() {
        CurrencyConverterBlockEntity blockEntity = this.getBlockEntity();
        return blockEntity != null ? blockEntity.getSideConfig() : new SideConfig();
    }

    private CurrencyConverterBlockEntity getBlockEntity() {
        return this.menu.blockEntity;
    }

    public void refreshSideConfig() {
        if (this.sideConfig != null) {
            this.removeWidget(this.sideConfig);
        }

        int x = this.leftPos + this.imageWidth + 8;
        int y = this.topPos;

        if (this.sideConfig == null) {
            this.sideConfig = new SideConfigWidget(x, y, ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "textures/block/currency_converter.png"), getAllowedDirections(), this::getSideConfig, tooltip -> this.tooltip = tooltip);
        } else {
            this.sideConfig.setX(x);
            this.sideConfig.setY(y);
        }

        this.addRenderableWidget(this.sideConfig);
    }

    protected Set<Direction> getAllowedDirections() {
        return Set.of(Direction.DOWN, Direction.WEST, Direction.EAST, Direction.UP, Direction.SOUTH);
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(new CurrencyToggleButton(this.leftPos + 70, this.topPos + 38, 18, 18, newCurrencyType -> {
            this.currencyType = newCurrencyType;
            PacketDistributor.sendToServer(new ChangeCurrencyTypePacket(newCurrencyType));
        }, (a) -> {
            this.tooltip = List.of(this.currencyType.item.getDescription());
        }, this.currencyType));

        this.refreshSideConfig();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        tooltip = null;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(BACKGROUND, this.leftPos, (this.height - this.imageHeight) / 2, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(guiGraphics, pMouseX, pMouseY);

        BigDecimal currency = blockEntity == null ? BigDecimal.ZERO : blockEntity.getTotalBalance();

        BigDecimal capacity = BigDecimal.valueOf(Config.maxCurrencyConverterBalance.get());
        double progress = currency.divide(capacity, RoundingMode.DOWN).min(BigDecimal.ONE).doubleValue();

        int startX = (this.width - 51) / 2;
        int startY = this.height / 2 - 81;

        guiGraphics.blit(BALANCE_PROGRESS, startX, startY, 0/*this.getBlitOffset()*/, 0, 0, 51, 5, 256, 256);
        guiGraphics.blit(BALANCE_PROGRESS, startX, startY, 0/*this.getBlitOffset()*/, 0, 5, ((int)(51 * progress)), 5, 256, 256);

        if (currency.compareTo(capacity) >= 0) {
            guiGraphics.drawCenteredString(this.font, Component.translatable("jackseconomy.max_capacity_reached").withStyle(ChatFormatting.RED), this.leftPos + (this.imageWidth / 2), this.topPos - 12, 0xFFFFFFFF);
        }

        if (pMouseX >= startX && pMouseX <= startX + 51 && pMouseY >= startY && pMouseY <= startY + 5) {
            tooltip = List.of(Component.translatable("jackseconomy.balance", Component.literal(CurrencyHelper.format(currency)).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.YELLOW), Component.translatable("jackseconomy.max_storage", Component.literal(CurrencyHelper.format(capacity)).withStyle(ChatFormatting.GOLD), Component.literal((int)(progress * 100) + "%").withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.YELLOW));
        }

        if (tooltip != null) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(), pMouseX, pMouseY);
        }
    }
}