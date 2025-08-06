package me.khajiitos.jackseconomy.init;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.argument.AdminShopArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ArgumentReg {
	public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, JacksEconomy.MOD_ID);

	public static final DeferredHolder<ArgumentTypeInfo<?, ?>, AdminShopArgument.Info> ADMINSHOP_ARGUMENT = COMMAND_ARGUMENT_TYPES.register("adminshop", () -> ArgumentTypeInfos.registerByClass(AdminShopArgument.class, new AdminShopArgument.Info()));

	public static void register(IEventBus bus) {
		COMMAND_ARGUMENT_TYPES.register(bus);
	}
}
