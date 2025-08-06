package me.khajiitos.jackseconomy.init;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.blockentity.*;
import me.khajiitos.jackseconomy.create.CreateBlockEntityReg;
import me.khajiitos.jackseconomy.create.CreateCheck;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockEntityReg {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, JacksEconomy.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExporterBlockEntity>> EXPORTER =
            BLOCK_ENTITY_TYPES.register("exporter",
                    () -> BlockEntityType.Builder.of(ExporterBlockEntity::new, ItemBlockReg.EXPORTER.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ImporterBlockEntity>> IMPORTER =
            BLOCK_ENTITY_TYPES.register("importer",
                    () -> BlockEntityType.Builder.of(ImporterBlockEntity::new, ItemBlockReg.IMPORTER.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidExporterBlockEntity>> FLUID_EXPORTER =
            BLOCK_ENTITY_TYPES.register("fluid_exporter",
                    () -> BlockEntityType.Builder.of(FluidExporterBlockEntity::new, ItemBlockReg.FLUID_EXPORTER.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidImporterBlockEntity>> FLUID_IMPORTER =
            BLOCK_ENTITY_TYPES.register("fluid_importer",
                    () -> BlockEntityType.Builder.of(FluidImporterBlockEntity::new, ItemBlockReg.FLUID_IMPORTER.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MechanicalExporterBlockEntity>> MECHANICAL_EXPORTER =
            CreateCheck.isInstalled() ? CreateBlockEntityReg.MECHANICAL_EXPORTER : null;

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MechanicalImporterBlockEntity>> MECHANICAL_IMPORTER =
            CreateCheck.isInstalled() ? CreateBlockEntityReg.MECHANICAL_IMPORTER : null;

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MechanicalFluidExporterBlockEntity>> MECHANICAL_FLUID_EXPORTER =
            CreateCheck.isInstalled() ? CreateBlockEntityReg.MECHANICAL_FLUID_EXPORTER : null;

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MechanicalFluidImporterBlockEntity>> MECHANICAL_FLUID_IMPORTER =
            CreateCheck.isInstalled() ? CreateBlockEntityReg.MECHANICAL_FLUID_IMPORTER : null;

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CurrencyConverterBlockEntity>> CURRENCY_CONVERTER =
            BLOCK_ENTITY_TYPES.register("currency_converter",
                    () -> BlockEntityType.Builder.of(CurrencyConverterBlockEntity::new, ItemBlockReg.CURRENCY_CONVERTER.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdminShopBlockEntity>> ADMIN_SHOP =
            BLOCK_ENTITY_TYPES.register("admin_shop",
                    () -> BlockEntityType.Builder.of(AdminShopBlockEntity::new, ItemBlockReg.ADMIN_SHOP.get())
                            .build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
