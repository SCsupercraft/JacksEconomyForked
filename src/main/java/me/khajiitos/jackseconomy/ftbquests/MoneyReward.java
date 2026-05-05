package me.khajiitos.jackseconomy.ftbquests;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.IconAnimation;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.item.OIMWalletItem;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.packet.WalletBalanceDifPacket;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import me.khajiitos.jackseconomy.util.CurrencyType;
import me.khajiitos.jackseconomy.util.ItemHelper;
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

public class MoneyReward extends Reward {
    private double amount = 1;

    public MoneyReward(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public RewardType getType() {
        return RewardsReg.MONEY;
    }

    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return Component.literal(CurrencyHelper.formatShortened(amount));
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

    @Override
    public void claim(ServerPlayer player, boolean notify) {
        ItemStack stack = CuriosWallet.get(player);
        if (stack.getItem() instanceof WalletItem) {
            BigDecimal oldBalance = WalletItem.getBalance(stack);
            BigDecimal newBalance = CurrencyHelper.addMoney(BigDecimal.valueOf(amount), stack, player);

            if (notify)
                Packets.sendToClient(player, new WalletBalanceDifPacket(newBalance.subtract(oldBalance)));
        } else if (Config.oneItemCurrencyMode.get()) {
            // Should only be $1 bills
            List<ItemStack> items = CurrencyHelper.getCurrencyItems(BigDecimal.valueOf(amount));
            Optional<IItemHandler> handlerOptional = stack.getItem() instanceof OIMWalletItem
                    ? stack.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve()
                    : Optional.empty();

            items.forEach(itemStack -> {
                ItemStack left = itemStack;
                if (handlerOptional.isPresent()) {
                    IItemHandler itemHandler = handlerOptional.get();
                    for (int i = 0; i < itemHandler.getSlots(); i++) {
                        left = itemHandler.insertItem(i, left, false);

                        if (left.isEmpty()) {
                            return;
                        }
                    }
                }

                if (!player.getInventory().add(left)) {
                    ItemHelper.dropItem(left, player.level(), player.blockPosition());
                }
            });
        }
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
