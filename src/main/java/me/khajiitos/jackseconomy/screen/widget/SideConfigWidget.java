package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.packet.UpdateSideConfigPacket;
import me.khajiitos.jackseconomy.util.SideConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SideConfigWidget extends AbstractWidget {
    private static final ResourceLocation IMAGE = ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "textures/gui/side_config.png");
    private final ResourceLocation faceTexture;
    private static final ResourceLocation NONE_TEXTURE = ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "textures/block/machine.png");
    private static final ResourceLocation INPUT_TEXTURE = ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "textures/block/input.png");
    private static final ResourceLocation OUTPUT_TEXTURE = ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "textures/block/output.png");
    private static final ResourceLocation REJECTION_OUTPUT_TEXTURE = ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "textures/block/rejection_output.png");
    private boolean open;
    private final Set<Direction> allowedDirections;
    private final Supplier<SideConfig> sideConfigSupplier;
    private final Consumer<List<Component>> onTooltip;

    public SideConfigWidget(int pX, int pY, ResourceLocation faceTexture, Set<Direction> allowedDirections, Supplier<SideConfig> sideConfigSupplier, Consumer<List<Component>> onTooltip) {
        super(pX, pY, 16, 16, Component.empty());

        this.faceTexture = faceTexture;
        this.allowedDirections = allowedDirections;
        this.sideConfigSupplier = sideConfigSupplier;
        this.onTooltip = onTooltip;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}

    protected SideConfig.Value getValue(Direction direction) {
        return this.sideConfigSupplier.get().getValue(direction);
    }

    protected ResourceLocation getSideTexture(Direction direction) {
        if (direction == Direction.NORTH) {
            return faceTexture;
        }

        return switch (this.sideConfigSupplier.get().getValue(direction)) {
            case NONE -> NONE_TEXTURE;
            case INPUT -> INPUT_TEXTURE;
            case OUTPUT -> OUTPUT_TEXTURE;
            case REJECTION_OUTPUT -> REJECTION_OUTPUT_TEXTURE;
        };
    }

    protected Pair<Integer, Integer> getDirectionOffsets(Direction direction) {
        return switch (direction) {
            case DOWN -> Pair.of(40, 60);
            case UP -> Pair.of(40, 20);
            case SOUTH -> Pair.of(60, 60);
            case EAST -> Pair.of(20, 40);
            case WEST -> Pair.of(60, 40);
            case NORTH -> Pair.of(40, 40);
        };
    }
    
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.open) {
            this.width = 96;
            this.height = 96;
        } else {
            this.width = 16;
            this.height = 16;
        }

        boolean buttonHovered = pMouseX >= this.getX() && pMouseX <= this.getX() + 16 && pMouseY >= this.getY() && pMouseY <= this.getY() + 16;

        guiGraphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, 0xFF666666);
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF333333);

        guiGraphics.blit(IMAGE, this.getX(), this.getY(), 0/*this.getBlitOffset()*/, 0, buttonHovered ? 16 : 0, 16, 16, 16, 32);
        if (open) {
            guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("jackseconomy.configuration").withStyle(ChatFormatting.YELLOW), this.getX() + 18, this.getY() + 4, 0xFFFFFFFF);

            for (Direction direction : this.allowedDirections) {
                Pair<Integer, Integer> offsets = getDirectionOffsets(direction);
                int xOffset = offsets.getFirst(), yOffset = offsets.getSecond();

                if (pMouseX >= this.getX() + xOffset && pMouseX <= this.getX() + xOffset + 16 && pMouseY >= this.getY() + yOffset && pMouseY <= this.getY() + yOffset + 16) {
                    RenderSystem.setShaderColor(1.5f, 1.5f, 1.5f, 1.f);

                    onTooltip.accept(List.of(Component.translatable("jackseconomy.direction." + direction.getName()).withStyle(ChatFormatting.YELLOW), Component.translatable("jackseconomy.sidevalue." + getValue(direction).name().toLowerCase())));
                }

                guiGraphics.blit(getSideTexture(direction), this.getX() + xOffset, this.getY() + yOffset, 0/*this.getBlitOffset()*/, 0, 0, 16, 16, 16, 16);
                RenderSystem.setShaderColor(1.f, 1.f, 1.f, 1.f);

            }

            Pair<Integer, Integer> offsets = getDirectionOffsets(Direction.NORTH);
            guiGraphics.blit(getSideTexture(Direction.NORTH), this.getX() + offsets.getFirst(), this.getY() + offsets.getSecond(), 0/*this.getBlitOffset()*/, 0, 0, 16, 16, 16, 16);
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton != 0 && pButton != 1) {
            return super.mouseClicked(pMouseX, pMouseY, pButton);
        }

        if (pMouseX >= this.getX() && pMouseX <= this.getX() + 16 && pMouseY >= this.getY() && pMouseY <= this.getY() + 16) {
            this.open = !this.open;
            return false;
        }

        for (Direction direction : this.allowedDirections) {
            Pair<Integer, Integer> offsets = getDirectionOffsets(direction);
            int xOffset = offsets.getFirst(), yOffset = offsets.getSecond();

            if (pMouseX >= this.getX() + xOffset && pMouseX <= this.getX() + xOffset + 16 && pMouseY >= this.getY() + yOffset && pMouseY <= this.getY() + yOffset + 16) {
                this.sideConfigSupplier.get().switchValue(direction, pButton == 0);
                PacketDistributor.sendToServer(new UpdateSideConfigPacket(this.sideConfigSupplier.get().getIntValues()));
                return false;
            }
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }
}
