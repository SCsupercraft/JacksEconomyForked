package me.khajiitos.jackseconomy.create;

import me.khajiitos.jackseconomy.block.*;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CreateItemBlockReg {
	public static final DeferredRegister<Block> BLOCKS = ItemBlockReg.BLOCKS;
	public static final DeferredRegister<Item> ITEMS = ItemBlockReg.ITEMS;

	public static final RegistryObject<MechanicalExporterBlock> MECHANICAL_EXPORTER = BLOCKS.register("mechanical_exporter", MechanicalExporterBlock::new);
	public static final RegistryObject<MechanicalImporterBlock> MECHANICAL_IMPORTER = BLOCKS.register("mechanical_importer", MechanicalImporterBlock::new);
	public static final RegistryObject<MechanicalFluidExporterBlock> MECHANICAL_FLUID_EXPORTER = BLOCKS.register("mechanical_fluid_exporter", MechanicalFluidExporterBlock::new);
	public static final RegistryObject<MechanicalFluidImporterBlock> MECHANICAL_FLUID_IMPORTER = BLOCKS.register("mechanical_fluid_importer", MechanicalFluidImporterBlock::new);

	public static final RegistryObject<BlockItem> MECHANICAL_EXPORTER_ITEM = ITEMS.register("mechanical_exporter", () -> new BlockItem(MECHANICAL_EXPORTER.get(), new Item.Properties()));
	public static final RegistryObject<BlockItem> MECHANICAL_IMPORTER_ITEM = ITEMS.register("mechanical_importer", () -> new BlockItem(MECHANICAL_IMPORTER.get(), new Item.Properties()));
	public static final RegistryObject<BlockItem> MECHANICAL_FLUID_EXPORTER_ITEM = ITEMS.register("mechanical_fluid_exporter", () -> new BlockItem(MECHANICAL_FLUID_EXPORTER.get(), new Item.Properties()));
	public static final RegistryObject<BlockItem> MECHANICAL_FLUID_IMPORTER_ITEM = ITEMS.register("mechanical_fluid_importer", () -> new BlockItem(MECHANICAL_FLUID_IMPORTER.get(), new Item.Properties()));
}
