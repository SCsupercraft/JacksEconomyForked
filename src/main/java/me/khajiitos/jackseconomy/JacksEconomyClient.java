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
import me.khajiitos.jackseconomy.util.NewShopUnlocks;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Mod(value = JacksEconomy.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = JacksEconomy.MOD_ID, value = Dist.CLIENT)
public class JacksEconomyClient {
    public static final KeyMapping OPEN_WALLET = new KeyMapping("key.jackseconomy.open_wallet", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.categories.jackseconomy");
    public static HashMap<ItemDescription, PricesItemPriceInfo> priceInfos = new HashMap<>();
    public static HashMap<FluidDescription, PricesFluidPriceInfo> fluidPriceInfos = new HashMap<>();
    public static HashMap<String, Integer> adminShopColors = new HashMap<>();
    public static HashMap<String, AdminShopData> adminShopData = new HashMap<>();
    public static AdminShopData defaultAdminShopData = null;
    public static Integer defaultAdminShopColor = -1;
    public static BigDecimal balanceDifPopup = null;
    public static long balanceDifPopupStartMillis = -1;

    public JacksEconomyClient(ModContainer container) {
        NeoForge.EVENT_BUS.register(new ClientEventListeners());
        NeoForge.EVENT_BUS.register(new ClientRenderEventListeners());

        container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, screen) -> new ClientConfigScreen(screen));

        if (CreateCheck.isInstalled()) {
            CreatePonder.init();
        }
    }

    public static @Nullable AdminShopData getAdminShopData(@Nullable String name) {
        return name != null ? adminShopData.get(name) : defaultAdminShopData;
    }

    public static AdminShopData getOrComputeAdminShopData(@Nullable String name) {
        if (name == null) {
            if (defaultAdminShopData == null) defaultAdminShopData = new AdminShopData(new LinkedHashMap<>(), new HashMap<>());
            return defaultAdminShopData;
        }
        return adminShopData.computeIfAbsent(
                name, unused -> new AdminShopData(new LinkedHashMap<>(), new HashMap<>()));
    }

    public static void removeEmptyAdminShopData() {
        if (defaultAdminShopData != null && defaultAdminShopData.shopItems.isEmpty() && defaultAdminShopData.sellPrices.isEmpty()) defaultAdminShopData = null;
        List<String> toRemove = new ArrayList<>();
        adminShopData.forEach((name, adminShopData) -> {
            if (adminShopData.shopItems.isEmpty() && adminShopData.sellPrices.isEmpty()) toRemove.add(name);
        });
        toRemove.forEach(adminShopData::remove);
    }

    @SubscribeEvent
    public static void onKeybindRegister(RegisterKeyMappingsEvent e) {
        e.register(OPEN_WALLET);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent e) {
        e.register(ContainerReg.EXPORTER_MENU.get(), ExporterScreen::new);
        e.register(ContainerReg.IMPORTER_MENU.get(), ImporterScreen::new);
        e.register(ContainerReg.FLUID_EXPORTER_MENU.get(), FluidExporterScreen::new);
        e.register(ContainerReg.FLUID_IMPORTER_MENU.get(), FluidImporterScreen::new);
        e.register(ContainerReg.WALLET_MENU.get(), WalletScreen::new);
        e.register(ContainerReg.OIM_WALLET_MENU.get(), OIMWalletScreen::new);
        e.register(ContainerReg.ADMIN_SHOP_MENU.get(), AdminShopScreen::new);
        e.register(ContainerReg.CURRENCY_CONVERTER_MENU.get(), CurrencyConverterScreen::new);
        e.register(ContainerReg.IMPORTER_TICKET_CREATOR_MENU.get(), TicketCreatorScreen::new);
        e.register(ContainerReg.EXPORTER_TICKET_CREATOR_MENU.get(), TicketCreatorScreen::new);
        e.register(ContainerReg.FLUID_IMPORTER_TICKET_CREATOR_MENU.get(), FluidTicketCreatorScreen::new);
        e.register(ContainerReg.FLUID_EXPORTER_TICKET_CREATOR_MENU.get(), FluidTicketCreatorScreen::new);

        e.register(ContainerReg.BULK_FLUID_MENU.get(), BulkFluidScreen::new);
        e.register(ContainerReg.BULK_ITEM_MENU.get(), BulkItemScreen::new);
        e.register(ContainerReg.BULK_ADMIN_SHOP_MENU.get(), BulkAdminShopScreen::new);

        if (CreateCheck.isInstalled()) { CreateClient.registerScreens(e); }
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent e) {
        ItemProperties.register(
                ItemBlockReg.ADMIN_SHOP_ITEM.get(),
                ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "colored"),
                (pStack, pLevel, pEntity, pSeed) -> {
                    CompoundTag tag = pStack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).getUnsafe();
                    String name = tag.contains("adminShopName") ? tag.getString("adminShopName") : null;
                    int color = name != null && JacksEconomyClient.adminShopColors.containsKey(name)
                            ? JacksEconomyClient.adminShopColors.get(name)
                            : JacksEconomyClient.defaultAdminShopColor;

                    return color != -1 ? 1f : 0f;
                }
        );

        if (CreateCheck.isInstalled()) { CreateClient.onClientSetup(e); }
    }

    @SubscribeEvent
    public static void onRegisterBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers e) {
        if (CreateCheck.isInstalled()) { CreateClient.onRegisterBlockEntityRenderers(e); }
    }

    @SubscribeEvent
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

    @SubscribeEvent
    public static void onRegisterItemColorProviders(RegisterColorHandlersEvent.Item registry) {
        ItemColor adminShopColor = (pStack, pTintIndex) -> {
            CompoundTag tag = pStack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).getUnsafe();
            if (pTintIndex != 0) return -1;

            String name = tag.contains("adminShopName") ? tag.getString("adminShopName") : null;
            return name != null && JacksEconomyClient.adminShopColors.containsKey(name)
                    ? JacksEconomyClient.adminShopColors.get(name)
                    : JacksEconomyClient.defaultAdminShopColor;
        };
        registry.register(adminShopColor, ItemBlockReg.ADMIN_SHOP_ITEM.get());
    }

    public record AdminShopData(
            LinkedHashMap<AdminShopScreen.Category, LinkedHashMap<AdminShopScreen.InnerCategory, List<AdminShopScreen.ShopItem>>> shopItems,
            HashMap<ItemDescription, AdminShopScreen.ItemSellabilityInfo> sellPrices
    ) {}
}
