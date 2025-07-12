package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.util.AdvancedFluidTank;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
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
    protected void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.setShaderTexture(0, BACKGROUND);

        guiGraphics.blit(BACKGROUND, this.getX(), this.getY(), 0, 0, this.width, this.height);

        RenderSystem.enableBlend();

        FluidStack fluidStack = fluidStorage.getFluid();
        if (fluidStack.isEmpty())
            return;

        long fluidAmount = fluidStorage.getFluidAmount();
        long capacity = fluidStorage.getCapacity();
        long scaledAmount = (fluidAmount * (height - 2)) / capacity;
        if (scaledAmount < 1) scaledAmount = 1;

        IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation stillTexture = fluidTypeExtensions.getStillTexture(fluidStack);
        if (stillTexture == null)
            return;

        TextureAtlasSprite sprite =
                this.minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
        int tintColor = fluidTypeExtensions.getTintColor(fluidStack);

        drawTiledSprite(guiGraphics, width - 2, height - 2, tintColor, scaledAmount, sprite, getX() + 1, getY() + 1);

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    /**
     * Taken from <a href="https://github.com/mezz/JustEnoughItems/blob/1.20.1/Library/src/main/java/mezz/jei/library/render/FluidTankRenderer.java#L133">JEI</a>
     */
    private static void setGLColorFromInt(int color) {
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float alpha = ((color >> 24) & 0xFF) / 255F;

        RenderSystem.setShaderColor(red, green, blue, alpha);
    }

    /**
     * Taken from <a href="https://github.com/mezz/JustEnoughItems/blob/1.20.1/Library/src/main/java/mezz/jei/library/render/FluidTankRenderer.java#L105">JEI</a>
     * Slightly modified by SCsupercraft
     */
    private static void drawTiledSprite(GuiGraphics guiGraphics, final int tiledWidth, final int tiledHeight, int color, long scaledAmount, TextureAtlasSprite sprite, int posX, int posY) {
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        Matrix4f matrix = guiGraphics.pose().last().pose();
        setGLColorFromInt(color);

        final int xTileCount = tiledWidth / 16;
        final int xRemainder = tiledWidth - (xTileCount * 16);
        final long yTileCount = scaledAmount / 16;
        final long yRemainder = scaledAmount - (yTileCount * 16);

        final int yStart = tiledHeight + posY;

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int width = (xTile == xTileCount) ? xRemainder : 16;
                long height = (yTile == 0) ? yRemainder : 16;
                int x = posX + (xTile * 16);
                int y = (int) (yStart - ((yTile) * 16) - (yTile == 0 ? 16 : yRemainder));
                if (width > 0 && height > 0) {
                    long maskTop = 16 - height;
                    int maskRight = 16 - width;

                    drawTextureWithMasking(matrix, x, y, sprite, maskTop, maskRight, 100);
                }
            }
        }
    }

    /**
     * Taken from <a href="https://github.com/mezz/JustEnoughItems/blob/1.20.1/Library/src/main/java/mezz/jei/library/render/FluidTankRenderer.java#L142">JEI</a>
     */
    private static void drawTextureWithMasking(Matrix4f matrix, float xCoord, float yCoord, TextureAtlasSprite textureSprite, long maskTop, long maskRight, float zLevel) {
        float uMin = textureSprite.getU0();
        float uMax = textureSprite.getU1();
        float vMin = textureSprite.getV0();
        float vMax = textureSprite.getV1();
        uMax = uMax - (maskRight / 16F * (uMax - uMin));
        vMax = vMax - (maskTop / 16F * (vMax - vMin));

        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix, xCoord, yCoord + 16, zLevel).uv(uMin, vMax).endVertex();
        bufferBuilder.vertex(matrix, xCoord + 16 - maskRight, yCoord + 16, zLevel).uv(uMax, vMax).endVertex();
        bufferBuilder.vertex(matrix, xCoord + 16 - maskRight, yCoord + maskTop, zLevel).uv(uMax, vMin).endVertex();
        bufferBuilder.vertex(matrix, xCoord, yCoord + maskTop, zLevel).uv(uMin, vMin).endVertex();
        tessellator.end();
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

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}
}
