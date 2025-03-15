package me.khajiitos.jackseconomy_legacy_create;

import me.khajiitos.jackseconomy.create.CreateCheck;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class LegacyCreateClient {
    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(LegacyCreateClient::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(LegacyCreateClient::onRegisterBlockEntityRenderers);

        if (CreateCheck.isLegacyInstalled()) {
            CreatePonder.register();
        }
    }

    public static void onClientSetup(FMLClientSetupEvent e) {
        if (CreateCheck.isLegacyInstalled()) {
            CreateClient.onClientSetup(e);
        }
    }

    public static void onRegisterBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers e) {
        if (CreateCheck.isLegacyInstalled()) {
            CreateClient.onRegisterBlockEntityRenderers(e);
        }
    }
}
