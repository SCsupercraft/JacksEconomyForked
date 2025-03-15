package me.khajiitos.jackseconomy.create;

public class CreateCheck {
		private static boolean installed;
		private static boolean legacyInstalled;

		static {
			try {
				Class.forName("com.simibubi.create.api.stress.BlockStressValues");
				installed = true;
			} catch (ClassNotFoundException e) {
				installed = false;
			}
			try {
				Class.forName("com.simibubi.create.content.kinetics.BlockStressValues");
				legacyInstalled = true;
			} catch (ClassNotFoundException e) {
				legacyInstalled = false;
			}
		}

		public static boolean isInstalled() {
			return installed;
		}
		public static boolean isLegacyInstalled() {
			return legacyInstalled;
		}
		public static boolean isAnyInstalled() {
			return isInstalled() || isLegacyInstalled();
		}
}
