package me.khajiitos.jackseconomy;

import com.mojang.logging.LogUtils;
import me.khajiitos.jackseconomy.config.ClientConfig;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.create.CreateCheck;
import me.khajiitos.jackseconomy.create.CreatePonder;
import me.khajiitos.jackseconomy.create.CreateStressProvider;
import me.khajiitos.jackseconomy.curios.CuriosCheck;
import me.khajiitos.jackseconomy.curios.CuriosHandler;
import me.khajiitos.jackseconomy.gamestages.GameStagesManager;
import me.khajiitos.jackseconomy.init.*;
import me.khajiitos.jackseconomy.listener.ConfigEventListeners;
import me.khajiitos.jackseconomy.listener.OtherEventListeners;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(JacksEconomy.MOD_ID)
public class JacksEconomy {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "jackseconomy";
    public static MinecraftServer server;

    public JacksEconomy() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ConfigEventListeners());
        MinecraftForge.EVENT_BUS.register(new OtherEventListeners());
        MinecraftForge.EVENT_BUS.addListener(JacksEconomy::onRegisterCommands);

        AdminShopCommand.init(MinecraftForge.EVENT_BUS);

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        if (CuriosCheck.isInstalled()) {
            eventBus.register(CuriosHandler.class);
        }

        ItemBlockReg.init(eventBus);

        BlockEntityReg.init(eventBus);
        ContainerReg.init(eventBus);
        Sounds.init(eventBus);
        Packets.init();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> JacksEconomyClient::init);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        if (CreateCheck.isInstalled()) {
            CreateStressProvider.init();
            CreatePonder.register();
        }

        GameStagesManager.init();
    }

    public static void onRegisterCommands(RegisterCommandsEvent e) {
        PriceCommands.register(e.getDispatcher());
    }
}
