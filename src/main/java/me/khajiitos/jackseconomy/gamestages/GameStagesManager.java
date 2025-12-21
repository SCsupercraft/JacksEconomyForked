package me.khajiitos.jackseconomy.gamestages;

import me.khajiitos.jackseconomy.util.NewShopUnlocks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class GameStagesManager {
    public static boolean hasGameStage(Player player, String gameStage) {
        if (GameStagesCheck.isInstalled()) {
            return GameStagesIntegration.hasGameStage(player, gameStage);
        }
        return true;
    }

    public static void acknowledgeUnlocks(ServerPlayer serverPlayer, NewShopUnlocks acknowledgedUnlocks) {
        if (GameStagesCheck.isInstalled()) {
            GameStagesIntegration.onUnlocksAcknowledged(serverPlayer, acknowledgedUnlocks);
        }
    }

    public static NewShopUnlocks getNewShopUnlocks(Player player) {
        if (GameStagesCheck.isInstalled()) {
            return GameStagesIntegration.getNewShopUnlocks(player);
        }

        return null;
    }

    public static void init() {
        if (GameStagesCheck.isInstalled()) {
            GameStagesIntegration.init();
        }
    }
}
