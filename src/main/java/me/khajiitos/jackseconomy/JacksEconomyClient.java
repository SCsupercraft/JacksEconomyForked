package me.khajiitos.jackseconomy;

import com.mojang.blaze3d.platform.InputConstants;
import me.khajiitos.jackseconomy.create.CreateCheck;
import me.khajiitos.jackseconomy.create.CreateClient;
import me.khajiitos.jackseconomy.create.CreatePonder;
import me.khajiitos.jackseconomy.data.price.FluidDescription;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import me.khajiitos.jackseconomy.data.price.PricesFluidPriceInfo;
import me.khajiitos.jackseconomy.data.price.PricesItemPriceInfo;
import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy.listener.ClientEventListeners;
import me.khajiitos.jackseconomy.listener.ClientRenderEventListeners;
import me.khajiitos.jackseconomy.screen.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

import java.math.BigDecimal;
import java.util.HashMap;

public class JacksEconomyClient {
    public static final KeyMapping OPEN_WALLET = new KeyMapping("key.jackseconomy.open_wallet", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.categories.jackseconomy");
    public static HashMap<ItemDescription, PricesItemPriceInfo> priceInfos = new HashMap<>();
    public static HashMap<FluidDescription, PricesFluidPriceInfo> fluidPriceInfos = new HashMap<>();
    public static BigDecimal balanceDifPopup = null;
    public static long balanceDifPopupStartMillis = -1;

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new ClientEventListeners());
        MinecraftForge.EVENT_BUS.register(new ClientRenderEventListeners());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(JacksEconomyClient::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(JacksEconomyClient::onKeybindRegister);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(JacksEconomyClient::onRegisterBlockEntityRenderers);

        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(ClientConfigScreen::new));

        if (CreateCheck.isInstalled()) { CreatePonder.init(); }
    }

    public static void onClientSetup(FMLClientSetupEvent e) {
        MenuScreens.register(ContainerReg.EXPORTER_MENU.get(), ExporterScreen::new);
        MenuScreens.register(ContainerReg.IMPORTER_MENU.get(), ImporterScreen::new);
        MenuScreens.register(ContainerReg.FLUID_EXPORTER_MENU.get(), FluidExporterScreen::new);
        MenuScreens.register(ContainerReg.FLUID_IMPORTER_MENU.get(), FluidImporterScreen::new);
        MenuScreens.register(ContainerReg.WALLET_MENU.get(), WalletScreen::new);
        MenuScreens.register(ContainerReg.OIM_WALLET_MENU.get(), OIMWalletScreen::new);
        MenuScreens.register(ContainerReg.ADMIN_SHOP_MENU.get(), AdminShopScreen::new);
        MenuScreens.register(ContainerReg.CURRENCY_CONVERTER_MENU.get(), CurrencyConverterScreen::new);
        MenuScreens.register(ContainerReg.IMPORTER_TICKET_CREATOR_MENU.get(), TicketCreatorScreen::new);
        MenuScreens.register(ContainerReg.EXPORTER_TICKET_CREATOR_MENU.get(), TicketCreatorScreen::new);
        MenuScreens.register(ContainerReg.FLUID_IMPORTER_TICKET_CREATOR_MENU.get(), FluidTicketCreatorScreen::new);
        MenuScreens.register(ContainerReg.FLUID_EXPORTER_TICKET_CREATOR_MENU.get(), FluidTicketCreatorScreen::new);

        if (CreateCheck.isInstalled()) { CreateClient.onClientSetup(e); }
    }

    public static void onKeybindRegister(RegisterKeyMappingsEvent e) {
        e.register(OPEN_WALLET);
    }

    public static void onRegisterBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers e) {
        if (CreateCheck.isInstalled()) { CreateClient.onRegisterBlockEntityRenderers(e); }
    }
}
