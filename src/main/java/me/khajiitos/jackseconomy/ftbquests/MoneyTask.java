package me.khajiitos.jackseconomy.ftbquests;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.IconAnimation;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.item.CurrencyItem;
import me.khajiitos.jackseconomy.item.OIMWalletItem;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.packet.WalletBalanceDifPacket;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import me.khajiitos.jackseconomy.util.CurrencyType;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MoneyTask extends Task {
    private double amount = 1;

    public MoneyTask(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public TaskType getType() {
        return TasksReg.MONEY;
    }

    @Override
    public long getMaxProgress() {
        // Since progress is a long, multiply by 100 to allow for decimals.
        return (long) Math.floor(amount * 100);
    }

    @Override
    public String formatMaxProgress() {
        return CurrencyHelper.formatShortened(amount);
    }

    @Override
    public String formatProgress(TeamData teamData, long progress) {
        return CurrencyHelper.formatShortened(progressToMoney(progress));
    }

    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return Component.literal(formatMaxProgress());
    }

    @OnlyIn(Dist.CLIENT)
    public Icon getAltIcon() {
        List<Icon> icons = new ArrayList<>();

        for(CurrencyType type : CurrencyType.values()) {
            Icon icon = ItemIcon.getItemIcon(type.item);
            if (!icon.isEmpty()) {
                icons.add(icon);
            }
        }

        if (icons.isEmpty()) {
            return ItemIcon.getItemIcon(FTBQuestsItems.MISSING_ITEM.get());
        } else {
            return IconAnimation.fromList(icons, false);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addDouble("amount", this.amount, (v) -> this.amount = v, 1, 1, Double.MAX_VALUE);
    }

    @OnlyIn(Dist.CLIENT)
    public void addMouseOverText(TooltipList list, TeamData teamData) {
        if (!teamData.isCompleted(this)) {
            list.blankLine();
            list.add(Component.translatable("ftbquests.task.click_to_submit").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
        }
    }

    @Override
    public void submitTask(TeamData teamData, ServerPlayer player, ItemStack craftedItem) {
        if (this.checkTaskSequence(teamData) && !teamData.isCompleted(this)) {
             ItemStack stack = CuriosWallet.get(player);
             double current = progressToMoney(teamData.getProgress(this));
             double needed = amount - current;

             if (stack.is(ItemBlockReg.GOLDEN_WALLET_ITEM.get())) {
                 addProgress(teamData, amount - current);
             } else if (stack.getItem() instanceof WalletItem) {
                 BigDecimal balance = WalletItem.getBalance(stack);
                 BigDecimal needed1 = BigDecimal.valueOf(needed);

                 BigDecimal toAdd = balance.min(needed1).setScale(2, RoundingMode.DOWN);
                 WalletItem.setBalance(stack, balance.subtract(toAdd));

                 addProgress(teamData, toAdd.doubleValue());
                 Packets.sendToClient(player, new WalletBalanceDifPacket(toAdd.negate()));
             } else if (Config.oneItemCurrencyMode.get()) {
                 long neededLong = (long) Math.floor(needed);
                 long left = neededLong;

                 for (ItemStack itemStack: player.getInventory().items) {
                     if (itemStack.getItem() instanceof CurrencyItem currencyItem && !currencyItem.isDisabled()) {
                         int toTake = Math.min(itemStack.getCount(), (int) Math.ceil(left / currencyItem.value.doubleValue()));
                         left -= toTake * currencyItem.value.doubleValue();
                         itemStack.shrink(toTake);

                         if (left <= 0) {
                             break;
                         }
                     }
                 }

                 // money not in the inventory, take from wallet
                 if (left > 0 && stack.getItem() instanceof OIMWalletItem) {
                     Optional<IItemHandler> handlerOptional = stack.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
                     if (handlerOptional.isPresent()) {
                         IItemHandler handler = handlerOptional.get();

                         for (int i = 0; i < handler.getSlots(); i++) {
                             ItemStack itemStack = handler.getStackInSlot(i);

                             if (itemStack.getItem() instanceof CurrencyItem currencyItem && !currencyItem.isDisabled()) {
                                 int toTake = Math.min(itemStack.getCount(), (int) Math.ceil(left / currencyItem.value.doubleValue()));
                                 left -= toTake * currencyItem.value.doubleValue();
                                 handler.extractItem(i, toTake, false);

                                 if (left <= 0) {
                                     break;
                                 }
                             }
                         }
                     }
                 }

                 long added = neededLong - left;
                 teamData.addProgress(this, added * 100);
             }
        }
    }

    private void addProgress(TeamData data, double money) {
        data.addProgress(this, moneyToProgress(money));
    }

    private double progressToMoney(long progress) {
        return (double) progress / 100;
    }

    private long moneyToProgress(double money) {
        return (long) Math.floor(money * 100);
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putDouble("amount", amount);
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        amount = nbt.getDouble("amount");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeDouble(amount);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        amount = buffer.readDouble();
    }
}
