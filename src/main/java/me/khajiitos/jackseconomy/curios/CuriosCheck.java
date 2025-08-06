package me.khajiitos.jackseconomy.curios;

public class CuriosCheck {
	private static boolean installed;

	static {
		try {
			Class.forName("top.theillusivec4.curios.api.CuriosApi");
			installed = true;
		} catch (ClassNotFoundException e) {
			installed = false;
		}
	}

	public static boolean isInstalled() {
		return installed;
	}
}
