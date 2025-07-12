package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.blockentity.IFluidImporterBlockEntity;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.data.price.FluidDescription;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.item.FluidImporterTicketItem;
import me.khajiitos.jackseconomy.item.FluidTicketItem;
import me.khajiitos.jackseconomy.menu.IBlockEntityContainer;
import me.khajiitos.jackseconomy.packet.ChangeSelectedFluidPacket;
import me.khajiitos.jackseconomy.screen.widget.BalanceProgressWidget;
import me.khajiitos.jackseconomy.screen.widget.RedstoneControlWidget;
import me.khajiitos.jackseconomy.screen.widget.SideConfigWidget;
import me.khajiitos.jackseconomy.screen.widget.TicketPreviewWidget;
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

public abstract class AbstractFluidImporterScreen<S extends IFluidImporterBlockEntity, T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/fluid_importer.png");
    private static final ResourceLocation REDSTONE_SELECTION = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/redstone_selection.png");
    protected TicketPreviewWidget<FluidDescription> ticketPreview;
    protected List<Component> tooltip;
    protected ItemStack ticketItemLastTick;
    protected SideConfigWidget sideConfig;


    public AbstractFluidImporterScreen(T pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, Component.empty());
        this.imageHeight = 177;
        this.inventoryLabelY = this.imageHeight - 96;
    }

    private @Nullable IFluidImporterBlockEntity getBlockEntity() {
        if (this.menu instanceof IBlockEntityContainer<?> blockEntityContainer && blockEntityContainer.getBlockEntity() instanceof IFluidImporterBlockEntity importerBlockEntity) {
            return importerBlockEntity;
        }

        return null;
    }

    @Override
    protected void init() {
        super.init();
        this.refreshTicketPreview();
        this.refreshSideConfig();

        IFluidImporterBlockEntity blockEntity = this.getBlockEntity();

        if (blockEntity != null) {
            this.addRenderableWidget(new BalanceProgressWidget(this.leftPos + 128, this.topPos + 9, blockEntity::getTotalBalance, () -> BigDecimal.valueOf(Config.maxImporterBalance.get()), tooltip -> this.tooltip = tooltip));
            this.addRenderableWidget(new RedstoneControlWidget(this.leftPos - 24, this.topPos + 1, blockEntity, tooltip -> this.tooltip = tooltip));
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {}

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(BACKGROUND, i, j, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.blit(BACKGROUND, this.leftPos + 39, this.topPos + 26, 18, 13, 212, 0, 36, 26, 256, 256);
    }

    public void refreshTicketPreview() {
        if (this.ticketPreview != null) {
            this.removeWidget(this.ticketPreview);
        }

        IFluidImporterBlockEntity blockEntity = this.getBlockEntity();

        if (blockEntity == null) {
            return;
        }

        ItemStack ticketItem = blockEntity.getItem(3);

        if (ticketItem.getItem() instanceof FluidImporterTicketItem) {
            List<FluidDescription> items = FluidTicketItem.getFluids(ticketItem);
            if (!items.isEmpty()) {
                this.ticketPreview = this.addRenderableWidget(new TicketPreviewWidget<FluidDescription>(this.leftPos + 39, this.topPos + 65, true, items, blockEntity.getSelectedFluid(), (newFluidDescription) -> {
                    blockEntity.selectFluid(newFluidDescription);
                    Packets.sendToServer(new ChangeSelectedFluidPacket(newFluidDescription));
                    this.refreshTicketPreview();
                }, tooltip -> this.tooltip = tooltip));
            } else {
                this.ticketPreview = null;
            }
        } else {
            this.ticketPreview = null;
        }
    }

    protected @Nullable SideConfig getSideConfig() {
        IFluidImporterBlockEntity blockEntity = this.getBlockEntity();
        return blockEntity != null ? blockEntity.getSideConfig() : new SideConfig();
    }

    protected Set<Direction> getAllowedDirections() {
        return Set.of(Direction.DOWN, Direction.WEST, Direction.EAST, Direction.UP, Direction.SOUTH);
    }

    public void refreshSideConfig() {
        if (this.sideConfig != null) {
            this.removeWidget(this.sideConfig);
        }

        int x = this.leftPos + this.imageWidth + 8;
        int y = this.topPos;

        if (this.sideConfig == null) {
            this.sideConfig = new SideConfigWidget(x, y, new ResourceLocation(JacksEconomy.MOD_ID, "textures/block/importer.png"), getAllowedDirections(), this::getSideConfig, tooltip -> this.tooltip = tooltip);
        } else {
            this.sideConfig.setX(x);
            this.sideConfig.setY(y);
        }

        this.addRenderableWidget(this.sideConfig);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        tooltip = null;
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);

        IFluidImporterBlockEntity blockEntity = this.getBlockEntity();
        BigDecimal currency = blockEntity == null ? BigDecimal.ZERO : blockEntity.getBalance();

        if (blockEntity != null) {
            int pixels = (int)(blockEntity.getProgress() * 18);
            guiGraphics.blit(BACKGROUND, this.leftPos + 39, this.topPos + 26, pixels, 13, 176, 0, pixels * 2, 26, 256, 256);
        }

        BigDecimal capacity = BigDecimal.valueOf(Config.maxImporterBalance.get());

        if (!(blockEntity.getItem(3).getItem() instanceof FluidImporterTicketItem)) {
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

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        int j = (this.height - this.imageHeight) / 2;
        int startXRedstone = this.leftPos - 53;

        boolean hoveredTicketPreview = this.ticketPreview != null && pMouseX >= this.ticketPreview.getX() && pMouseX <= this.ticketPreview.getX() + this.ticketPreview.getWidth() && pMouseY >= this.ticketPreview.getY() && pMouseY <= this.ticketPreview.getY() + this.ticketPreview.getHeight();

        if (hoveredTicketPreview && this.ticketPreview.mouseClicked(pMouseX, pMouseY, pButton)) {
            return true;
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    protected void containerTick() {
        if (this.ticketPreview != null) {
            this.ticketPreview.tick();
        }

        IFluidImporterBlockEntity blockEntity = this.getBlockEntity();

        if (blockEntity != null) {
            ItemStack ticketItem = blockEntity.getItem(3);

            if (ticketItem == null && ticketItemLastTick != null || ticketItem != null && ticketItemLastTick == null || ticketItem != null && !ItemStack.isSameItemSameTags(ticketItem, ticketItemLastTick)) {
                this.refreshTicketPreview();
                ticketItemLastTick = ticketItem;
            }
        }

        super.containerTick();
    }

    protected void renderTooltipsOrSomething(GuiGraphics guiGraphics, int mouseX, int mouseY) {

    }

    @Override
    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        // Stupid way to prevent slot hovers when ticket preview is open
        if (this.ticketPreview != null && this.ticketPreview.isOpen() && pWidth == 16 && pHeight == 16) {
            if (pMouseX >= this.ticketPreview.getX() && pMouseX <= this.ticketPreview.getX() + this.ticketPreview.getWidth() && pMouseY >= this.ticketPreview.getY() && pMouseY <= this.ticketPreview.getY() + this.ticketPreview.getHeight()) {
                return false;
            }
        }
        return super.isHovering(pX, pY, pWidth, pHeight, pMouseX, pMouseY);
    }
}
