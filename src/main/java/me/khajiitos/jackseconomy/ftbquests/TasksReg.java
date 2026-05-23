package me.khajiitos.jackseconomy.ftbquests;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class TasksReg {
    public static final TaskType MONEY = register("money", MoneyTask::new, () -> Icons.DOLLAR_BILL);

    public static void register() {

    }

    private static TaskType register(String name, TaskType.Provider provider, Supplier<Icon> iconSupplier) {
        return TaskTypes.register(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, name), provider, iconSupplier);
    }
}
