package me.khajiitos.jackseconomy.ftbquests;

public class FtbQuestsCheck {
    private static boolean installed;

    static {
        try {
            Class.forName("dev.ftb.mods.ftbquests.quest.reward.RewardType");
            installed = true;
        } catch (ClassNotFoundException e) {
            installed = false;
        }
    }

    public static boolean isInstalled() {
			return installed;
		}
}
