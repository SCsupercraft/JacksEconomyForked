package me.khajiitos.jackseconomy.ftbquests;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class RewardsReg {
    public static final RewardType MONEY = register("money", MoneyReward::new, () -> Icons.DOLLAR_BILL);

    public static void register() {

    }

    private static RewardType register(String name, RewardType.Provider provider, Supplier<Icon> iconSupplier) {
        return RewardTypes.register(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, name), provider, iconSupplier);
    }
}
