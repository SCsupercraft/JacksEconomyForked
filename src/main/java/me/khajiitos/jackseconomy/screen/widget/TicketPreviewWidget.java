package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.data.price.FluidDescription;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TicketPreviewWidget<T> extends AbstractWidget {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "textures/gui/ticket_slot_preview.png");
    private static final ResourceLocation ROUND_ROBIN = ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "textures/gui/round_robin.png");
    private final boolean openable;
    private boolean open = false;
    private final List<T> items;
    private int tickCount;
    private final T selectedDescription;
    private final boolean roundRobin;
    private final Consumer<T> onSelect;
    private final Consumer<Boolean> onSetRoundRobin;
    private final Consumer<List<Component>> onTooltip;

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = openable && open;
    }

    public TicketPreviewWidget(int pX, int pY, boolean openable, List<T> items, @Nullable T selectedDescription, boolean roundRobin, @Nullable Consumer<T> onSelect, @Nullable Consumer<Boolean> onSetRoundRobin, @Nullable Consumer<List<Component>> onTooltip) {
        super(pX, pY, 18, 18, Component.empty());

        this.openable = openable;
        this.items = items;
        this.selectedDescription = selectedDescription;
        this.roundRobin = roundRobin;
        this.onSelect = onSelect;
        this.onSetRoundRobin = onSetRoundRobin;
        this.onTooltip = onTooltip;

        if (items.isEmpty() || !(isItemTicket() || isFluidTicket()))
            throw new RuntimeException("Invalid ticket type!");
    }

    private boolean isItemTicket() {
        return items.get(0) instanceof ItemDescription;
    }

    private boolean isFluidTicket() {
        return items.get(0) instanceof FluidDescription;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (open) {
            this.width = (items.size() + 1) * 18;
            guiGraphics.fill(getX() - 1, getY() - 1, getX() + width + 1, getY() + height + 1, 0xFF444444);

            int x = this.getX();
            guiGraphics.blit(BACKGROUND, x, this.getY(), 0, this.roundRobin ? 18 : 0, 0, 18, 18, 36, 18);
            guiGraphics.blit(ROUND_ROBIN, x + 1, this.getY() + 1, 0, 0, 0, 16, 16, 16, 16);

            if (pMouseX >= x + 1 && pMouseX <= x + 17 && pMouseY >= getY() + 1 && pMouseY <= getY() + 17) {
                AbstractContainerScreen.renderSlotHighlight(guiGraphics, x + 1, getY() + 1, 0);

                if (this.onTooltip != null) {
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(Component.translatable("jackseconomy.round_robin"));
                    if (roundRobin)
                        tooltip.add(Component.translatable("jackseconomy.selected").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_GRAY));
                    this.onTooltip.accept(tooltip);
                }
            }

            x += 18;
            for (T description : items) {
                guiGraphics.blit(BACKGROUND, x, this.getY(), 0/*this.getBlitOffset()*/, description.equals(this.selectedDescription) ? 18 : 0, 0, 18, 18, 36, 18);

                ItemStack itemStack = isItemTicket() ? ((ItemDescription) description).createItemStack() : new ItemStack(((FluidDescription) description).fluid().value().getBucket(), 1);
                if (isFluidTicket()) {
                    itemStack.set(DataComponents.CUSTOM_NAME, ((FluidDescription) description).fluid().value().getFluidType().getDescription().copy());
                }
                guiGraphics.renderItem(itemStack, x + 1, this.getY() + 1);

                if (pMouseX >= x + 1 && pMouseX <= x + 17 && pMouseY >= getY() + 1 && pMouseY <= getY() + 17) {
                    AbstractContainerScreen.renderSlotHighlight(guiGraphics, x + 1, getY() + 1, 0/*this.getBlitOffset()*/);

                    if (this.onTooltip != null) {
                        List<Component> tooltip = itemStack.getTooltipLines(Item.TooltipContext.EMPTY, Minecraft.getInstance().player, TooltipFlag.Default.NORMAL);
                        if (description.equals(this.selectedDescription)) {
                            tooltip.add(Component.literal(" "));
                            tooltip.add(Component.translatable("jackseconomy.selected").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_GRAY));
                        }
                        this.onTooltip.accept(tooltip);
                    }
                }

                x += 18;
            }
        } else {
            this.width = 18;
            RenderSystem.setShaderTexture(0, BACKGROUND);
            guiGraphics.blit(BACKGROUND, this.getX(), this.getY(), this.width, 0, 0, 18, 18, 36, 18);

            if (!items.isEmpty()) {
                T description = openable ? (items.stream().anyMatch(desc -> desc.equals(this.selectedDescription)) ? this.selectedDescription : items.get(0)) : items.get((tickCount / 20) % items.size());

                ItemStack itemStack = isItemTicket() ? ((ItemDescription) description).createItemStack() : new ItemStack(((FluidDescription) description).fluid().value().getBucket(), 1);
                if (isFluidTicket()) {
                    itemStack.set(DataComponents.CUSTOM_NAME, ((FluidDescription)description).fluid().value().getFluidType().getDescription().copy().setStyle(Style.EMPTY.withItalic(false)));
                }
                guiGraphics.renderItem(itemStack, this.getX() + 1, this.getY() + 1);

                if (pMouseX >= getX() + 1 && pMouseX <= getX() + 17 && pMouseY >= getY() + 1 && pMouseY <= getY() + 17) {
                    AbstractContainerScreen.renderSlotHighlight(guiGraphics, getX() + 1, getY() + 1, 0/*this.getBlitOffset()*/);
                    this.onTooltip.accept(itemStack.getTooltipLines(Item.TooltipContext.EMPTY, Minecraft.getInstance().player, TooltipFlag.Default.NORMAL));
                }
            }

        }
    }

    public void tick() {
        this.tickCount++;
    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        if (this.openable && !this.open) {
            this.open = true;
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.open && !this.isHovered) {
            this.open = false;
            return true;
        }

        if (this.open) {
            int x = this.getX();
            if (this.onSetRoundRobin != null
                    && pMouseX >= x + 1
                    && pMouseX <= x + 17
                    && pMouseY >= getY() + 1
                    && pMouseY <= getY() + 17
            ) {
                this.onSetRoundRobin.accept(!roundRobin);
                return true;
            }
            x += 18;
            if (this.onSelect != null) {;
                for (T description: items) {
                    if (pMouseX >= x + 1 && pMouseX <= x + 17 && pMouseY >= getY() + 1 && pMouseY <= getY() + 17) {
                        this.onSelect.accept(description);
                        return true;
                    }
                    x += 18;
                }
            }
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}
}
