package me.khajiitos.jackseconomy.gamestages;

import me.khajiitos.jackseconomy.util.NewShopUnlocks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

// TODO: Game Stages doesn't support later than 1.20.3, should it be replaced by another mod?
public class GameStagesManager {
    public static boolean hasGameStage(Player player, String gameStage) {
        return true;
    }

    public static void acknowledgeUnlocks(ServerPlayer serverPlayer, NewShopUnlocks acknowledgedUnlocks) {

    }

    public static NewShopUnlocks getNewShopUnlocks(Player player) {
        return null;
    }

    public static void init() {

    }
}
