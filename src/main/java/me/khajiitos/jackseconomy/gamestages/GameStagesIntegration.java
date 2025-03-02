package me.khajiitos.jackseconomy.gamestages;

import me.khajiitos.jackseconomy.data.price.AdminShopItemPriceInfo;
import me.khajiitos.jackseconomy.data.price.PriceManager;
import me.khajiitos.jackseconomy.util.NewShopUnlocks;
import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.data.IStageData;
import net.darkhax.gamestages.event.GameStageEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.UUID;

class GameStagesIntegration {
    private static final HashMap<UUID, NewShopUnlocks> newShopUnlocks = new HashMap<>();

    static boolean hasGameStage(Player player, String gameStage) {
        IStageData stageData = GameStageHelper.getPlayerData(player);
        return stageData != null && stageData.hasStage(gameStage);
    }

    static void init() {
        MinecraftForge.EVENT_BUS.addListener(GameStagesIntegration::onGameStageAdded);
    }

    static NewShopUnlocks getNewShopUnlocks(Player player) {
        return newShopUnlocks.get(player.getUUID());
    }

    static void onUnlocksAcknowledged(ServerPlayer player, NewShopUnlocks acknowledgedUnlocks) {
        if (newShopUnlocks.containsKey(player.getUUID())) {
            NewShopUnlocks shopUnlocks = newShopUnlocks.get(player.getUUID());
            shopUnlocks.reduce(acknowledgedUnlocks);

            if (shopUnlocks.unlockedItems.isEmpty() && shopUnlocks.unlockedCategories.isEmpty()) {
                newShopUnlocks.remove(player.getUUID());
            }
        }
    }

    private static void onGameStageAdded(GameStageEvent.Added e) {
        /*
        {
            "PlayerUUID": {
                "items": [{"slot": 1, category: "General:Gems"}],
                "categories": ["General", General:Gems"]
            }
        }

        {
            "acknowledgements": {
                "items": [{"slot": 1, category: "General:Gems"}],
                "categories": ["General", General:Gems"]
            }
        }
         */

        if (e.getEntity() instanceof ServerPlayer serverPlayer) {
            NewShopUnlocks unlocks = new NewShopUnlocks();
            for (PriceManager.ItemPriceEntry itemPriceEntry : PriceManager.getItemPriceInfos()) {
                if (itemPriceEntry.itemPriceInfo() instanceof AdminShopItemPriceInfo adminShopItemPriceInfo) {
                    if (e.getStageName().equals(adminShopItemPriceInfo.adminShopStage) && adminShopItemPriceInfo.category != null) {
                        NewShopUnlocks.Item item = new NewShopUnlocks.Item(adminShopItemPriceInfo.adminShopSlot, adminShopItemPriceInfo.category);

                        String mainCategory = adminShopItemPriceInfo.category.split(":")[0];

                        unlocks.unlockedCategories.add(mainCategory);
                        unlocks.unlockedCategories.add(adminShopItemPriceInfo.category);
                        unlocks.unlockedItems.add(item);
                    }
                }
            }

            if (newShopUnlocks.containsKey(serverPlayer.getUUID())) {
                newShopUnlocks.get(serverPlayer.getUUID()).merge(unlocks);
            } else {
                newShopUnlocks.put(serverPlayer.getUUID(), unlocks);
            }
        }
    }
}