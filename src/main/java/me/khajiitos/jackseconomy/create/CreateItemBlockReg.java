package me.khajiitos.jackseconomy.create;

import me.khajiitos.jackseconomy.block.MechanicalExporterBlock;
import me.khajiitos.jackseconomy.block.MechanicalFluidExporterBlock;
import me.khajiitos.jackseconomy.block.MechanicalFluidImporterBlock;
import me.khajiitos.jackseconomy.block.MechanicalImporterBlock;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CreateItemBlockReg {
	public static final DeferredRegister.Blocks BLOCKS = ItemBlockReg.BLOCKS;
	public static final DeferredRegister.Items ITEMS = ItemBlockReg.ITEMS;

	public static final DeferredBlock<MechanicalExporterBlock> MECHANICAL_EXPORTER = BLOCKS.register("mechanical_exporter", MechanicalExporterBlock::new);
	public static final DeferredBlock<MechanicalImporterBlock> MECHANICAL_IMPORTER = BLOCKS.register("mechanical_importer", MechanicalImporterBlock::new);
	public static final DeferredBlock<MechanicalFluidExporterBlock> MECHANICAL_FLUID_EXPORTER = BLOCKS.register("mechanical_fluid_exporter", MechanicalFluidExporterBlock::new);
	public static final DeferredBlock<MechanicalFluidImporterBlock> MECHANICAL_FLUID_IMPORTER = BLOCKS.register("mechanical_fluid_importer", MechanicalFluidImporterBlock::new);

	public static final DeferredItem<BlockItem> MECHANICAL_EXPORTER_ITEM = ITEMS.register("mechanical_exporter", () -> new BlockItem(MECHANICAL_EXPORTER.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> MECHANICAL_IMPORTER_ITEM = ITEMS.register("mechanical_importer", () -> new BlockItem(MECHANICAL_IMPORTER.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> MECHANICAL_FLUID_EXPORTER_ITEM = ITEMS.register("mechanical_fluid_exporter", () -> new BlockItem(MECHANICAL_FLUID_EXPORTER.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> MECHANICAL_FLUID_IMPORTER_ITEM = ITEMS.register("mechanical_fluid_importer", () -> new BlockItem(MECHANICAL_FLUID_IMPORTER.get(), new Item.Properties()));
}
