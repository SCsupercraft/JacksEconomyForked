package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.util.AdvancedFluidTank;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class FluidStorageWidget extends AbstractWidget {
    public ResourceLocation BACKGROUND = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/fluid_bar.png");
    private final Minecraft minecraft;
    private final AdvancedFluidTank fluidStorage;
    public FluidStorageWidget(int pX, int pY, AdvancedFluidTank fluidStorage, Minecraft minecraft) {
        super(pX, pY, 15, 65, Component.literal(""));
        this.fluidStorage = fluidStorage;
        this.minecraft = minecraft;
    }

    public boolean isHovered() {
        return this.isHovered;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, BACKGROUND);

        guiGraphics.blit(BACKGROUND, this.getX(), this.getY(), 0, 0, this.width, this.height);

        FluidStack fluidStack = fluidStorage.getFluid();
        if (fluidStack.isEmpty())
            return;

        int fluidHeight = getFluidHeight(fluidStorage);

        IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation stillTexture = fluidTypeExtensions.getStillTexture(fluidStack);
        if (stillTexture == null)
            return;

        TextureAtlasSprite sprite =
                this.minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
        int tintColor = fluidTypeExtensions.getTintColor(fluidStack);

        float alpha = ((tintColor >> 24) & 0xFF) / 255f;
        float red = ((tintColor >> 16) & 0xFF) / 255f;
        float green = ((tintColor >> 8) & 0xFF) / 255f;
        float blue = (tintColor & 0xFF) / 255f;

        guiGraphics.setColor(red, green, blue, alpha);

        int tileWidth = sprite.contents().width();
        int tileHeight = sprite.contents().height();

        int tilesX = (int) Math.ceil((double)this.width / tileWidth);
        int tilesY = (int) Math.ceil((double)fluidHeight / tileHeight);

        for (int x = 0; x < tilesX; x++) {
            for (int y = 0; y < tilesY; y++) {
                int renderX = this.getX() + 1 + (x * tileWidth);
                int renderY = getFluidY(fluidHeight) + (y * tileHeight);

                int widthToDraw = Math.min(tileWidth, this.width - x * tileWidth);
                int heightToDraw = Math.min(tileHeight, fluidHeight - y * tileHeight);

                if (x == tilesX - 1)
                    widthToDraw -= 2;

                float uOffset = sprite.getU0();
                float vOffset = sprite.getV0();
                int uWidth = (int)((widthToDraw / (float)tileWidth) * (sprite.getU1() - uOffset) * 256);
                int vHeight = (int)((heightToDraw / (float)tileHeight) * (sprite.getV1() - vOffset) * 256);

                guiGraphics.blit(sprite.atlasLocation(), renderX, renderY, widthToDraw, heightToDraw,
                        (int)(uOffset * 256), (int)(vOffset * 256), uWidth, vHeight, 256, 256);
            }
        }

        guiGraphics.setColor(1, 1, 1, 1);
    }

    public void appendTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        FluidType fluidType = fluidStorage.getFluid().getFluid().getFluidType();
        List<Component> components = new ArrayList<>();

        Component amount = Component.literal(fluidStorage.getFluidAmount() + "mB/" + fluidStorage.getCapacity() + "mB");
        Component type = fluidType.getDescription().copy().withStyle(
                Style.EMPTY.withColor(ChatFormatting.GRAY)
        );

        components.add(amount);
        if (!fluidType.isAir()) components.add(type);

        guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, components, mouseX, mouseY);
    }

    private int getFluidHeight(IFluidTank tank) {
        // return (48 * (tank.getFluidAmount() / tank.getCapacity()));

        float fluidAmount = fluidStorage.getFluidAmount();
        float capacity = fluidStorage.getCapacity();

        return (int) ((fluidAmount / capacity) * (this.height - 2));
    }

    private int getFluidY(int fluidHeight) {
        return this.getY() + this.height - 1 - fluidHeight;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}
}
