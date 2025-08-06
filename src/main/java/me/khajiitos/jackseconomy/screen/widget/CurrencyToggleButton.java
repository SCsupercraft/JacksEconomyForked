package me.khajiitos.jackseconomy.screen.widget;

import me.khajiitos.jackseconomy.util.CurrencyType;
import me.khajiitos.jackseconomy.util.ItemHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.function.Consumer;

public class CurrencyToggleButton extends SimpleImageButton {
    private CurrencyType currencyType;
    private final OnChange onChange;

    public CurrencyToggleButton(int pX, int pY, int pWidth, int pHeight, OnChange onChange, CurrencyType currencyType) {
        super(pX, pY, pWidth, pHeight, getImage(currencyType.item), (b) -> {});
        this.currencyType = currencyType;
        this.onChange = onChange;
    }

    public CurrencyToggleButton(int pX, int pY, int pWidth, int pHeight, OnChange onChange, Consumer<List<Component>> onTooltip, CurrencyType currencyType) {
        super(pX, pY, pWidth, pHeight, getImage(currencyType.item), (b) -> {}, onTooltip);
        this.currencyType = currencyType;
        this.onChange = onChange;
    }

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    private static ResourceLocation getImage(Item item) {
        ResourceLocation resourceLocation = ItemHelper.getItemResourceLocation(item);
        if (resourceLocation == null) {
            return null;
        }
        return ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), "textures/item/" + resourceLocation.getPath() + ".png");
    }

    public void setCurrencyType(CurrencyType currencyType) {
        this.currencyType = currencyType;
        this.image = getImage(currencyType.item);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (clicked(pMouseX, pMouseY)) {
            if (pButton == 0) {
                setCurrencyType(currencyType.next());
                onChange.onChange(currencyType);
                return true;
            }
            else if (pButton == 1) {
                setCurrencyType(currencyType.previous());
                onChange.onChange(currencyType);
                return true;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @FunctionalInterface
    public interface OnChange {
        void onChange(CurrencyType newCurrencyType);
    }
}
