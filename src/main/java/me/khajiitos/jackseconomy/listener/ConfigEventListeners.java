package me.khajiitos.jackseconomy.listener;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.config.ClientConfig;
import me.khajiitos.jackseconomy.config.Config;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;

public class ConfigEventListeners {
    @SubscribeEvent
    public void onConfigLoad(ModConfigEvent.Loading e) {
        if (e.getConfig().getSpec() == Config.SPEC) {
            Config.SPEC.acceptConfig(e.getConfig().getLoadedConfig());
        } else if (e.getConfig().getSpec() == ClientConfig.SPEC) {
            ClientConfig.SPEC.acceptConfig(e.getConfig().getLoadedConfig());
        }
    }

    @SubscribeEvent
    public void onConfigReload(ModConfigEvent.Reloading e) {
        if (e.getConfig().getSpec() == Config.SPEC) {
            Config.SPEC.acceptConfig(e.getConfig().getLoadedConfig());
            JacksEconomy.LOGGER.info("Server config reloaded!");
        } else if (e.getConfig().getSpec() == ClientConfig.SPEC) {
            ClientConfig.SPEC.acceptConfig(e.getConfig().getLoadedConfig());
            JacksEconomy.LOGGER.info("Client config reloaded!");
        }
    }
}
