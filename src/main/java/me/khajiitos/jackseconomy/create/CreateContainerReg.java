package me.khajiitos.jackseconomy.create;

import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy.menu.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CreateContainerReg {
	public static final DeferredRegister<MenuType<?>> MENU_TYPES = ContainerReg.MENU_TYPES;

	public static final RegistryObject<MenuType<MechanicalExporterMenu>> MECHANICAL_EXPORTER_MENU = MENU_TYPES.register("mechanical_exporter", ContainerReg.regBlockMenu(MechanicalExporterMenu::new));
	public static final RegistryObject<MenuType<MechanicalImporterMenu>> MECHANICAL_IMPORTER_MENU = MENU_TYPES.register("mechanical_importer", ContainerReg.regBlockMenu(MechanicalImporterMenu::new));
	public static final RegistryObject<MenuType<MechanicalFluidExporterMenu>> MECHANICAL_FLUID_EXPORTER_MENU = MENU_TYPES.register("mechanical_fluid_exporter", ContainerReg.regBlockMenu(MechanicalFluidExporterMenu::new));
	public static final RegistryObject<MenuType<MechanicalFluidImporterMenu>> MECHANICAL_FLUID_IMPORTER_MENU = MENU_TYPES.register("mechanical_fluid_importer", ContainerReg.regBlockMenu(MechanicalFluidImporterMenu::new));
}
