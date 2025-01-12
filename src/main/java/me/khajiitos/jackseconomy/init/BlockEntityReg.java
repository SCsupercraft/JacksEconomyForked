package me.khajiitos.jackseconomy.init;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.blockentity.*;
import me.khajiitos.jackseconomy.create.CreateBlockEntityReg;
import me.khajiitos.jackseconomy.create.CreateCheck;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityReg {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, JacksEconomy.MOD_ID);

    public static final RegistryObject<BlockEntityType<ExporterBlockEntity>> EXPORTER =
            BLOCK_ENTITY_TYPES.register("exporter",
                    () -> BlockEntityType.Builder.of(ExporterBlockEntity::new, ItemBlockReg.EXPORTER.get())
                            .build(null));

    public static final RegistryObject<BlockEntityType<ImporterBlockEntity>> IMPORTER =
            BLOCK_ENTITY_TYPES.register("importer",
                    () -> BlockEntityType.Builder.of(ImporterBlockEntity::new, ItemBlockReg.IMPORTER.get())
                            .build(null));

    public static final RegistryObject<BlockEntityType<MechanicalExporterBlockEntity>> MECHANICAL_EXPORTER =
            CreateCheck.isInstalled() ? CreateBlockEntityReg.MECHANICAL_EXPORTER : null;

    public static final RegistryObject<BlockEntityType<MechanicalImporterBlockEntity>> MECHANICAL_IMPORTER =
            CreateCheck.isInstalled() ? CreateBlockEntityReg.MECHANICAL_IMPORTER : null;

    public static final RegistryObject<BlockEntityType<CurrencyConverterBlockEntity>> CURRENCY_CONVERTER =
            BLOCK_ENTITY_TYPES.register("currency_converter",
                    () -> BlockEntityType.Builder.of(CurrencyConverterBlockEntity::new, ItemBlockReg.CURRENCY_CONVERTER.get())
                            .build(null));

    public static void init(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
