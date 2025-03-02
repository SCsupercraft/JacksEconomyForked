package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.blockentity.FluidExporterBlockEntity;
import me.khajiitos.jackseconomy.blockentity.IFluidExporterBlockEntity;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.data.price.FluidDescription;
import me.khajiitos.jackseconomy.item.*;
import me.khajiitos.jackseconomy.menu.IBlockEntityContainer;
import me.khajiitos.jackseconomy.screen.widget.*;
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
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractFluidExporterScreen<S extends IFluidExporterBlockEntity, T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    protected static final ResourceLocation BACKGROUND = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/fluid_exporter.png");
    protected static final ResourceLocation REDSTONE_SELECTION = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/redstone_selection.png");
    protected List<Component> tooltip;
    protected ItemStack ticketItemLastTick;
    protected TicketPreviewWidget<FluidDescription> ticketPreview;
    protected SideConfigWidget sideConfig;

    public AbstractFluidExporterScreen(T pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, Component.empty());
        this.imageHeight = 177;
        this.inventoryLabelY = this.imageHeight - 96;
    }

    private @Nullable IFluidExporterBlockEntity getBlockEntity() {
        if (this.menu instanceof IBlockEntityContainer<?> blockEntityContainer && blockEntityContainer.getBlockEntity() instanceof IFluidExporterBlockEntity fluidExporterBlockEntity) {
            return fluidExporterBlockEntity;
        }

        return null;
    }

    @Override
    protected void init() {
        super.init();

        this.refreshTicketPreview();
        this.refreshSideConfig();

        IFluidExporterBlockEntity blockEntity = this.getBlockEntity();

        if (blockEntity != null) {
            this.addRenderableWidget(new BalanceProgressWidget(this.leftPos + 128, this.topPos + 9, blockEntity::getTotalBalance, () -> BigDecimal.valueOf(Config.maxImporterBalance.get()), tooltip -> {
                this.tooltip = tooltip;
            }));
            this.addRenderableWidget(new RedstoneControlWidget(this.leftPos - 24, this.topPos + 1, blockEntity, tooltip -> this.tooltip = tooltip));
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(BACKGROUND, i, j, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.blit(BACKGROUND, this.leftPos + 41, this.topPos + 21, 14, 24, 204, 0, 28, 48, 256, 256);
    }

    public BigDecimal getCurrencyInOutputSlots() {
        BigDecimal currency = BigDecimal.ZERO;
        for (int i = 0; i < 3; i++) {
            ItemStack itemStack = this.menu.slots.get(i).getItem();
            if (itemStack.getItem() instanceof CurrencyItem coin) {
                currency = currency.add(coin.value.multiply(new BigDecimal(itemStack.getCount())));
            }
        }
        return currency;
    }

    public void refreshTicketPreview() {
        if (this.ticketPreview != null) {
            this.removeWidget(this.ticketPreview);
        }

        IFluidExporterBlockEntity blockEntity = this.getBlockEntity();

        if (blockEntity == null) {
            return;
        }

        ItemStack ticketItem = blockEntity.getItem(6);

        // Unnecessary for golden ticket
        if (ticketItem.getItem() instanceof FluidExporterTicketItem && (!(ticketItem.getItem() instanceof GoldenFluidExporterTicketItem))) {
            List<FluidDescription> fluids = FluidTicketItem.getFluids(ticketItem);
            if (!fluids.isEmpty()) {
                this.ticketPreview = this.addRenderableWidget(new TicketPreviewWidget<>(this.leftPos + 39, this.topPos + 65, false, fluids, null, null, (tooltip) -> this.tooltip = tooltip));
            } else {
                this.ticketPreview = null;
            }
        } else {
            this.ticketPreview = null;
        }
    }

    protected Set<Direction> getAllowedDirections() {
        return Set.of(Direction.DOWN, Direction.WEST, Direction.EAST, Direction.UP, Direction.SOUTH);
    }

    protected @Nullable SideConfig getSideConfig() {
        IFluidExporterBlockEntity blockEntity = this.getBlockEntity();
        return blockEntity != null ? blockEntity.getSideConfig() : new SideConfig();
    }

    public void refreshSideConfig() {
        if (this.sideConfig != null) {
            this.removeWidget(this.sideConfig);
        }

        int x = this.leftPos + this.imageWidth + 8;
        int y = this.topPos;

        if (this.sideConfig == null) {
            this.sideConfig = new SideConfigWidget(x, y, new ResourceLocation(JacksEconomy.MOD_ID, "textures/block/exporter.png"), getAllowedDirections(), this::getSideConfig, tooltip -> this.tooltip = tooltip);
        } else {
            this.sideConfig.setX(x);
            this.sideConfig.setY(y);
        }

        this.addRenderableWidget(this.sideConfig);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {}

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.tooltip = null;
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);

        IFluidExporterBlockEntity blockEntity = this.getBlockEntity();

        BigDecimal currency = (blockEntity == null ? BigDecimal.ZERO : blockEntity.getBalance()).add(getCurrencyInOutputSlots());

        if (blockEntity != null) {
            int pixels = (int)Math.ceil(blockEntity.getProgress() * 24);
            guiGraphics.blit(BACKGROUND, this.leftPos + 41, this.topPos + 21 + (24 - pixels), 14, pixels, 176, (48 - pixels * 2), 28, pixels * 2, 256, 256);
        }

        BigDecimal capacity = BigDecimal.valueOf(Config.maxExporterBalance.get());

        if (!(blockEntity.getItem(6).getItem() instanceof FluidExporterTicketItem)) {
            guiGraphics.drawCenteredString(this.font, Component.translatable("jackseconomy.manifest_required").withStyle(ChatFormatting.YELLOW), this.leftPos + (this.imageWidth / 2), this.topPos - 12, 0xFFFFFFFF);
        } else if (currency.compareTo(capacity) >= 0) {
            guiGraphics.drawCenteredString(this.font, Component.translatable("jackseconomy.max_capacity_reached").withStyle(ChatFormatting.RED), this.leftPos + (this.imageWidth / 2), this.topPos - 12, 0xFFFFFFFF);
        }

        renderTooltipsOrSomething(guiGraphics, pMouseX, pMouseY);

        this.renderTooltip(guiGraphics, pMouseX, pMouseY);

        if (tooltip != null) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(), pMouseX, pMouseY);
        }
    }

    protected void renderTooltipsOrSomething(GuiGraphics guiGraphics, int mouseX, int mouseY) {}

    @Override
    protected void containerTick() {
        super.containerTick();

        IFluidExporterBlockEntity blockEntity = this.getBlockEntity();

        if (blockEntity != null) {
            ItemStack ticketItem = blockEntity.getItem(6);

            if (ticketItem == null && ticketItemLastTick != null || ticketItem != null && ticketItemLastTick == null || ticketItem != null && !ItemStack.isSameItemSameTags(ticketItem, ticketItemLastTick)) {
                this.refreshTicketPreview();
                ticketItemLastTick = ticketItem;
            }
        }

        if (this.ticketPreview != null) {
            this.ticketPreview.tick();
        }
    }
}