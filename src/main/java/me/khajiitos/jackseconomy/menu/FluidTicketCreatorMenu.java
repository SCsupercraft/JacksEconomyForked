package me.khajiitos.jackseconomy.menu;

import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.data.price.FluidDescription;
import me.khajiitos.jackseconomy.item.EmptyTicketItem;
import me.khajiitos.jackseconomy.item.FluidTicketItem;
import me.khajiitos.jackseconomy.util.ItemHelper;
import net.minecraft.ChatFormatting;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class FluidTicketCreatorMenu extends AbstractContainerMenu {
    public final Container container;

    public FluidTicketCreatorMenu(MenuType<? extends FluidTicketCreatorMenu> menuType, int pContainerId, Inventory inventory) {
        super(menuType, pContainerId);
        this.container = new SimpleContainer(36);

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new FluidSlot(this.container, col + row * 9, 8 + col * 18, 7 + row * 18));
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

            List<FluidDescription> fluidDescriptions = new ArrayList<>();

            for (int i = 0; i < this.container.getContainerSize(); i++) {
                ItemStack item = this.container.getItem(i);
                boolean ghost;

                if (item.getTag() != null) {
                    ghost = item.getTag().getBoolean("jackseconomy_ghost");
                    item.getTag().remove("jackseconomy_ghost");
                } else ghost = false;

                if (!item.isEmpty()) {
                    FluidDescription fluidDescription = FluidDescription.ofFluid(item.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(RuntimeException::new).getFluidInTank(0));

                    if (!fluidDescriptions.contains(fluidDescription)) {
                        fluidDescriptions.add(fluidDescription);
                    }

                    if (Config.returnManifestItems.get() && !ghost) {
                        if (!serverPlayer.getInventory().add(item)) {
                            ItemHelper.dropItem(item, serverPlayer.level(), serverPlayer.blockPosition());
                        }
                    }
                }
            }
            this.container.clearContent();

            FluidTicketItem.setFluids(ticketItem, fluidDescriptions);

            pPlayer.setItemInHand(hand, ticketItem);

            CompoundTag nbt = ticketItem.getOrCreateTag();

            if (serverPlayer.hasPermissions(4) && serverPlayer.isCreative()) {
                String command = "/give @p " + ItemHelper.getItemName(ticketItem.getItem()) + nbt;

                serverPlayer.sendSystemMessage(Component.translatable("jackseconomy.generate_this_ticket").withStyle(ChatFormatting.GOLD));
                serverPlayer.sendSystemMessage(Component.literal(command).withStyle(ChatFormatting.YELLOW).append(" ").append(Component.translatable("jackseconomy.copy").setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, command)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("jackseconomy.click_to_copy"))).withBold(true).withColor(ChatFormatting.GOLD))));
            }
        }

        super.removed(pPlayer);
    }

    public static class FluidSlot extends TicketCreatorMenu.GhostSlot {

        public FluidSlot(Container pContainer, int pSlot, int pX, int pY) {
            super(pContainer, pSlot, pX, pY);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack pStack) {
            return pStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
        }
    }
}
