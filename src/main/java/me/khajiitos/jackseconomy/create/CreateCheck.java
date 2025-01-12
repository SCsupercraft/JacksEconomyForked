package me.khajiitos.jackseconomy.create;

public class CreateCheck {
		private static boolean installed;

		static {
			try {
				Class.forName("com.simibubi.create.content.kinetics.BlockStressValues");
				installed = true;
			} catch (ClassNotFoundException e) {
				installed = false;
			}
		}

		public static boolean isInstalled() {
			return installed;
		}
}
