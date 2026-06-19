package me.khajiitos.jackseconomy.listener;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.data.AdminShopColorManager;
import me.khajiitos.jackseconomy.data.PurchaseManager;
import me.khajiitos.jackseconomy.data.StockMarketManager;
import me.khajiitos.jackseconomy.data.price.PriceManager;
import me.khajiitos.jackseconomy.menu.WalletMenu;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class OtherEventListeners {
    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent e) {
        // Ensure the field is populated before the server starts to avoid errors
        JacksEconomy.server = e.getServer();
    }

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent e) {
		PriceManager.load();
		AdminShopColorManager.load();
        if (!FMLEnvironment.production) StockMarketManager.load();
		PurchaseManager.load();
	}

	@SubscribeEvent
	public void onServerStarted(ServerStartedEvent e) {
        // Work in progress, only enable in a development environment.
        if (!FMLEnvironment.production) JacksEconomy.server.addTickable(StockMarketManager::tick);
	}

	@SubscribeEvent
	public void onServerStopped(ServerStoppedEvent e) {
        if (!FMLEnvironment.production) StockMarketManager.save();
		PurchaseManager.save();

		JacksEconomy.server = null;
	}

    @SubscribeEvent
    public void onTick(ServerTickEvent.Pre e) {
		e.getServer().getPlayerList().getPlayers().forEach(serverPlayer -> {
			if (serverPlayer.containerMenu instanceof WalletMenu walletMenu) {
				walletMenu.tick();
			}
		});
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer serverPlayer) {
            PriceManager.sendDataToPlayer(serverPlayer, true);
            AdminShopColorManager.updatePlayer(serverPlayer);
        }
    }
}
