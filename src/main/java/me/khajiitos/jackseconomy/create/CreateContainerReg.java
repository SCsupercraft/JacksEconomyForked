package me.khajiitos.jackseconomy.create;

import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy.menu.MechanicalExporterMenu;
import me.khajiitos.jackseconomy.menu.MechanicalImporterMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CreateContainerReg {
	public static final DeferredRegister<MenuType<?>> MENU_TYPES = ContainerReg.MENU_TYPES;

	public static final RegistryObject<MenuType<MechanicalExporterMenu>> MECHANICAL_EXPORTER_MENU = MENU_TYPES.register("mechanical_exporter", ContainerReg.regBlockMenu(MechanicalExporterMenu::new));
	public static final RegistryObject<MenuType<MechanicalImporterMenu>> MECHANICAL_IMPORTER_MENU = MENU_TYPES.register("mechanical_importer", ContainerReg.regBlockMenu(MechanicalImporterMenu::new));
}
