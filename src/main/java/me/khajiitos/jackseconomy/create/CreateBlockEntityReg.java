package me.khajiitos.jackseconomy.create;

import me.khajiitos.jackseconomy.blockentity.*;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CreateBlockEntityReg {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = BlockEntityReg.BLOCK_ENTITY_TYPES;

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MechanicalExporterBlockEntity>> MECHANICAL_EXPORTER =
			BLOCK_ENTITY_TYPES.register("mechanical_exporter",
					() -> BlockEntityType.Builder.of(MechanicalExporterBlockEntity::new, ItemBlockReg.MECHANICAL_EXPORTER.get())
							.build(null)
			);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MechanicalImporterBlockEntity>> MECHANICAL_IMPORTER =
			BLOCK_ENTITY_TYPES.register("mechanical_importer",
					() -> BlockEntityType.Builder.of(MechanicalImporterBlockEntity::new, ItemBlockReg.MECHANICAL_IMPORTER.get())
							.build(null)
			);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MechanicalFluidExporterBlockEntity>> MECHANICAL_FLUID_EXPORTER =
			BLOCK_ENTITY_TYPES.register("mechanical_fluid_exporter",
					() -> BlockEntityType.Builder.of(MechanicalFluidExporterBlockEntity::new, ItemBlockReg.MECHANICAL_FLUID_EXPORTER.get())
							.build(null)
			);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MechanicalFluidImporterBlockEntity>> MECHANICAL_FLUID_IMPORTER =
			BLOCK_ENTITY_TYPES.register("mechanical_fluid_importer",
					() -> BlockEntityType.Builder.of(MechanicalFluidImporterBlockEntity::new, ItemBlockReg.MECHANICAL_FLUID_IMPORTER.get())
							.build(null)
			);
}
