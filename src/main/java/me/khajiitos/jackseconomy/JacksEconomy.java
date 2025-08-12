package me.khajiitos.jackseconomy;

import com.mojang.logging.LogUtils;
import me.khajiitos.jackseconomy.create.CreateCheck;
import me.khajiitos.jackseconomy.create.CreateStressProvider;
import me.khajiitos.jackseconomy.curios.CuriosCheck;
import me.khajiitos.jackseconomy.curios.CuriosHandler;
import me.khajiitos.jackseconomy.data.DataSyncConfigurationTask;
import me.khajiitos.jackseconomy.gamestages.GameStagesManager;
import me.khajiitos.jackseconomy.init.*;
import me.khajiitos.jackseconomy.config.ClientConfig;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.listener.ConfigEventListeners;
import me.khajiitos.jackseconomy.listener.OtherEventListeners;
import me.khajiitos.jackseconomy.util.IEnergyCapable;
import me.khajiitos.jackseconomy.util.IFluidCapable;
import me.khajiitos.jackseconomy.util.IItemCapable;
import me.khajiitos.jackseconomy.util.OIMWalletCapabilityWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import org.slf4j.Logger;

@Mod(JacksEconomy.MOD_ID)
public class JacksEconomy {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "jackseconomy";
    public static MinecraftServer server;

    public JacksEconomy(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(new OtherEventListeners());

        AdminShopCommand.init(NeoForge.EVENT_BUS);
        EconomyCommand.init(NeoForge.EVENT_BUS);

        modEventBus.register(this);
        modEventBus.register(new ConfigEventListeners());

        if (CuriosCheck.isInstalled()) {
            CuriosHandler.init();
        }

        ArgumentReg.register(modEventBus);
        ComponentReg.register(modEventBus);
        ItemBlockReg.register(modEventBus);
        BlockEntityReg.register(modEventBus);
        ContainerReg.register(modEventBus);
        Sounds.register(modEventBus);
        Packets.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        if (CreateCheck.isInstalled()) {
            CreateStressProvider.init();
        }

        GameStagesManager.init();
    }

    @SubscribeEvent
    public void registerClientConfigurationTasks(final RegisterConfigurationTasksEvent event) {
        event.register(new DataSyncConfigurationTask(event.getListener()));
    }

    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.ItemHandler.ITEM,
                (itemStack, context) -> OIMWalletCapabilityWrapper.create(itemStack),
                ItemBlockReg.WALLET_ITEM
        );

        registerItemCapable(
                event,
                BlockEntityReg.IMPORTER.get(),
                BlockEntityReg.EXPORTER.get(),
                BlockEntityReg.FLUID_IMPORTER.get(),
                BlockEntityReg.FLUID_EXPORTER.get()
        );

        registerFluidCapable(
                event,
                BlockEntityReg.FLUID_IMPORTER.get(),
                BlockEntityReg.FLUID_EXPORTER.get()
        );

        registerEnergyCapable(
                event,
                BlockEntityReg.IMPORTER.get(),
                BlockEntityReg.EXPORTER.get(),
                BlockEntityReg.FLUID_IMPORTER.get(),
                BlockEntityReg.FLUID_EXPORTER.get()
        );

        if (CreateCheck.isInstalled()) {
            registerItemCapable(
                    event,
                    BlockEntityReg.MECHANICAL_IMPORTER.get(),
                    BlockEntityReg.MECHANICAL_EXPORTER.get(),
                    BlockEntityReg.MECHANICAL_FLUID_IMPORTER.get(),
                    BlockEntityReg.MECHANICAL_FLUID_EXPORTER.get()
            );

            registerFluidCapable(
                    event,
                    BlockEntityReg.MECHANICAL_FLUID_IMPORTER.get(),
                    BlockEntityReg.MECHANICAL_FLUID_EXPORTER.get()
            );
        }
    }

    private void registerItemCapable(RegisterCapabilitiesEvent event, BlockEntityType<?>... blockEntityTypes) {
        for (BlockEntityType<?> type : blockEntityTypes) {
            event.registerBlockEntity(
                    Capabilities.ItemHandler.BLOCK,
                    type,
                    (be, context) -> be instanceof IItemCapable itemCapable ? itemCapable.getItemCapability(context) : null
            );
        }
    }

    private void registerFluidCapable(RegisterCapabilitiesEvent event, BlockEntityType<?>... blockEntityTypes) {
        for (BlockEntityType<?> type : blockEntityTypes) {
            event.registerBlockEntity(
                    Capabilities.FluidHandler.BLOCK,
                    type,
                    (be, context) -> be instanceof IFluidCapable itemCapable ? itemCapable.getFluidCapability(context) : null
            );
        }
    }

    private void registerEnergyCapable(RegisterCapabilitiesEvent event, BlockEntityType<?>... blockEntityTypes) {
        for (BlockEntityType<?> type : blockEntityTypes) {
            event.registerBlockEntity(
                    Capabilities.EnergyStorage.BLOCK,
                    type,
                    (be, context) -> be instanceof IEnergyCapable itemCapable ? itemCapable.getEnergyStorage(context) : null
            );
        }
    }
}
