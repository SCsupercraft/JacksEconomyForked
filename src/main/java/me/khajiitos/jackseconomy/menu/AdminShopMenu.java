package me.khajiitos.jackseconomy.menu;

import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.init.ContainerReg;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class AdminShopMenu extends AbstractContainerMenu {
    private final @Nullable Merchant merchant;
    public final Inventory inventory;
    private boolean slotsLocked = true;
    public final boolean oneItemCurrencyMode;

    public AdminShopMenu(int pContainerId, Inventory inventory) {
        this(pContainerId, inventory, null);
    }

    public AdminShopMenu(int pContainerId, Inventory inventory, @Nullable Merchant merchant) {
        super(ContainerReg.ADMIN_SHOP_MENU.get(), pContainerId);
        this.merchant = merchant;
        this.inventory = inventory;
        this.oneItemCurrencyMode = Config.oneItemCurrencyMode.get();

        this.addPlayerInventory(inventory, 150);
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        if (this.merchant != null) this.merchant.setTradingPlayer(null);
    }

    public void setSlotsLocked(boolean slotsLocked) {
        this.slotsLocked = slotsLocked;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        if (oneItemCurrencyMode && !Config.oneItemCurrencyMode.get()) {
            return false;
        } else {
            return oneItemCurrencyMode || !Config.oneItemCurrencyMode.get();
        }
    }

    public void addPlayerInventory(Inventory playerInv, int yOffset) {
        for (int rowY = 0; rowY < 3; ++rowY) {
            for (int rowX = 0; rowX < 9; ++rowX) {
                this.addSlot(new ConditionallyLockedSlot(playerInv, rowX + rowY * 9 + 9, 8 + rowX * 18, yOffset + rowY * 18, () -> !this.slotsLocked));
            }
        }

        for (int rowX = 0; rowX < 9; ++rowX) {
            this.addSlot(new ConditionallyLockedSlot(playerInv, rowX, 8 + rowX * 18, yOffset + 58, () -> !this.slotsLocked));
        }
    }

    private static class ConditionallyLockedSlot extends Slot {
        private final Supplier<Boolean> condition;
        public ConditionallyLockedSlot(Container pContainer, int pSlot, int pX, int pY, Supplier<Boolean> condition) {
            super(pContainer, pSlot, pX, pY);
            this.condition = condition;
        }

        @Override
        public boolean mayPickup(Player pPlayer) {
            boolean conditionResult = condition.get();
            return conditionResult && super.mayPickup(pPlayer);
        }

        @Override
        public boolean mayPlace(ItemStack pStack) {
            boolean conditionResult = condition.get();
            return conditionResult && super.mayPlace(pStack);
        }
    }
}
