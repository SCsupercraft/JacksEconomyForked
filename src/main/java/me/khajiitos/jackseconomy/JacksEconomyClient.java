package me.khajiitos.jackseconomy;

import com.mojang.blaze3d.platform.InputConstants;
import me.khajiitos.jackseconomy.blockentity.AdminShopBlockEntity;
import me.khajiitos.jackseconomy.create.CreateCheck;
import me.khajiitos.jackseconomy.create.CreateClient;
import me.khajiitos.jackseconomy.create.CreatePonder;
import me.khajiitos.jackseconomy.data.price.FluidDescription;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import me.khajiitos.jackseconomy.data.price.PricesFluidPriceInfo;
import me.khajiitos.jackseconomy.data.price.PricesItemPriceInfo;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import me.khajiitos.jackseconomy.listener.ClientEventListeners;
import me.khajiitos.jackseconomy.listener.ClientRenderEventListeners;
import me.khajiitos.jackseconomy.screen.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
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
    public static HashMap<String, Integer> adminShopColors = new HashMap<>();
    public static Integer defaultAdminShopColor = -1;
    public static BigDecimal balanceDifPopup = null;
    public static long balanceDifPopupStartMillis = -1;

    public static void init() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.register(new ClientEventListeners());
        MinecraftForge.EVENT_BUS.register(new ClientRenderEventListeners());

        eventBus.addListener(JacksEconomyClient::onClientSetup);
        eventBus.addListener(JacksEconomyClient::onKeybindRegister);
        eventBus.addListener(JacksEconomyClient::onRegisterBlockEntityRenderers);
        eventBus.addListener(JacksEconomyClient::onRegisterBlockColorProviders);
        eventBus.addListener(JacksEconomyClient::onRegisterItemColorProviders);

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

        MenuScreens.register(ContainerReg.BULK_FLUID_MENU.get(), BulkFluidScreen::new);
        MenuScreens.register(ContainerReg.BULK_ITEM_MENU.get(), BulkItemScreen::new);
        MenuScreens.register(ContainerReg.BULK_ADMIN_SHOP_MENU.get(), BulkAdminShopScreen::new);

        ItemProperties.register(
                ItemBlockReg.ADMIN_SHOP_ITEM.get(),
                new ResourceLocation(JacksEconomy.MOD_ID, "colored"),
                (pStack, pLevel, pEntity, pSeed) -> {
                    CompoundTag tag = BlockItem.getBlockEntityData(pStack);
                    String name = tag != null && tag.contains("adminShopName") ? tag.getString("adminShopName") : null;
                    int color = name != null && JacksEconomyClient.adminShopColors.containsKey(name)
                            ? JacksEconomyClient.adminShopColors.get(name)
                            : JacksEconomyClient.defaultAdminShopColor;

                    return color != -1 ? 1f : 0f;
                }
        );

        if (CreateCheck.isInstalled()) { CreateClient.onClientSetup(e); }
    }

    public static void onKeybindRegister(RegisterKeyMappingsEvent e) {
        e.register(OPEN_WALLET);
    }

    public static void onRegisterBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers e) {
        if (CreateCheck.isInstalled()) { CreateClient.onRegisterBlockEntityRenderers(e); }
    }

    public static void onRegisterBlockColorProviders(RegisterColorHandlersEvent.Block registry) {
        BlockColor adminShopColor = (pState, pLevel, pPos, pTintIndex) -> {
            if (pTintIndex != 0 || pLevel == null || pPos == null) return -1;

            AdminShopBlockEntity blockEntity = pLevel.getBlockEntity(pPos, BlockEntityReg.ADMIN_SHOP.get()).orElse(null);
            if (blockEntity == null) return -1;

            String name = blockEntity.getName();
            return name != null && JacksEconomyClient.adminShopColors.containsKey(name)
                    ? JacksEconomyClient.adminShopColors.get(name)
                    : JacksEconomyClient.defaultAdminShopColor;
        };
        registry.register(adminShopColor, ItemBlockReg.ADMIN_SHOP.get());
    }

    public static void onRegisterItemColorProviders(RegisterColorHandlersEvent.Item registry) {
        ItemColor adminShopColor = (pStack, pTintIndex) -> {
            CompoundTag tag = BlockItem.getBlockEntityData(pStack);
            if (pTintIndex != 0) return -1;

            String name = tag != null && tag.contains("adminShopName") ? tag.getString("adminShopName") : null;
            return name != null && JacksEconomyClient.adminShopColors.containsKey(name)
                    ? JacksEconomyClient.adminShopColors.get(name)
                    : JacksEconomyClient.defaultAdminShopColor;
        };
        registry.register(adminShopColor, ItemBlockReg.ADMIN_SHOP_ITEM.get());
    }
}
