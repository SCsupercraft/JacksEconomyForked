package me.khajiitos.jackseconomy.menu;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import me.khajiitos.jackseconomy.init.ComponentReg;
import me.khajiitos.jackseconomy.item.EmptyTicketItem;
import me.khajiitos.jackseconomy.item.TicketItem;
import me.khajiitos.jackseconomy.util.ItemHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static me.khajiitos.jackseconomy.init.ComponentReg.isGhostItem;
import static me.khajiitos.jackseconomy.init.ComponentReg.removeGhostItem;

public abstract class TicketCreatorMenu extends AbstractContainerMenu {
    public final Container container;

    public TicketCreatorMenu(MenuType<? extends TicketCreatorMenu> menuType, int pContainerId, Inventory inventory) {
        super(menuType, pContainerId);
        this.container = new SimpleContainer(36);

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new ComponentReg.GhostSlot(this.container, col + row * 9, 8 + col * 18, 7 + row * 18));
            }
        }

        this.addPlayerInventory(inventory, 95);
    }

    protected abstract EmptyTicketItem.Type getTicketType();

    public int getContainerSize() {
        return 36;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        int containerSize = this.getContainerSize();
        ItemStack clickedStackCopy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack clickedStack = slot.getItem();
            if (isGhostItem(clickedStack)) {
                clickedStack.setCount(0);
                removeGhostItem(clickedStack);
                return ItemStack.EMPTY;
            }

            clickedStackCopy = clickedStack.copy();
            if (index < containerSize) {
                if (!this.moveItemStackTo(clickedStack, containerSize, containerSize + 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(clickedStack, 0, containerSize, false)) {
                return ItemStack.EMPTY;
            }

            if (clickedStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (clickedStack.getCount() == clickedStackCopy.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, clickedStack);
        }

        return clickedStackCopy;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    public void addPlayerInventory(Inventory playerInv, int yOffset) {
        for(int rowY = 0; rowY < 3; ++rowY) {
            for(int rowX = 0; rowX < 9; ++rowX) {
                this.addSlot(new Slot(playerInv, rowX + rowY * 9 + 9, 8 + rowX * 18, yOffset + rowY * 18));
            }
        }

        for(int rowX = 0; rowX < 9; ++rowX) {
            this.addSlot(new Slot(playerInv, rowX, 8 + rowX * 18, yOffset + 58));
        }
    }

    @Override
    public void removed(Player pPlayer) {
        if (!(pPlayer instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ItemStack emptyTicketItem = pPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        InteractionHand hand = InteractionHand.MAIN_HAND;

        if (!(emptyTicketItem.getItem() instanceof EmptyTicketItem)) {
            emptyTicketItem = pPlayer.getItemInHand(InteractionHand.OFF_HAND);
            hand = InteractionHand.OFF_HAND;

            if (!(emptyTicketItem.getItem() instanceof EmptyTicketItem)) {
                super.removed(pPlayer);
                return;
            }
        }

        if (!container.isEmpty()) {
            ItemStack ticketItem = new ItemStack(this.getTicketType().ticketItem);

            List<ItemDescription> itemDescriptions = new ArrayList<>();

            for (int i = 0; i < this.container.getContainerSize(); i++) {
                ItemStack item = this.container.getItem(i);

                boolean ghost = isGhostItem(item);
                if (ghost)
                    removeGhostItem(item);

                if (!item.isEmpty()) {
                    ItemDescription itemDescription = ItemDescription.ofItem(item);

                    if (!itemDescriptions.contains(itemDescription)) {
                        itemDescriptions.add(itemDescription);
                    }

                    if (Config.returnManifestItems.get() && !ghost) {
                        if (!serverPlayer.getInventory().add(item)) {
                            ItemHelper.dropItem(item, serverPlayer.level(), serverPlayer.blockPosition());
                        }
                    }
                }
            }
            this.container.clearContent();

            TicketItem.setItems(ticketItem, itemDescriptions);
            pPlayer.setItemInHand(hand, ticketItem);

            if (serverPlayer.hasPermissions(4) && serverPlayer.isCreative()) {
                RegistryAccess.Frozen access = JacksEconomy.server.registryAccess();
                ItemInput input = new ItemInput(ticketItem.getItemHolder(), ticketItem.getComponentsPatch());
                String command = "/give @p " + input.serialize(access);

                serverPlayer.sendSystemMessage(Component.translatable("jackseconomy.generate_this_ticket").withStyle(ChatFormatting.GOLD));
                serverPlayer.sendSystemMessage(Component.literal(command).withStyle(ChatFormatting.YELLOW).append(" ").append(Component.translatable("jackseconomy.copy").setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, command)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("jackseconomy.click_to_copy"))).withBold(true).withColor(ChatFormatting.GOLD))));
            }
        }

        super.removed(pPlayer);
    }
}
