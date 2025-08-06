package me.khajiitos.jackseconomy.create;

import com.simibubi.create.infrastructure.config.AllConfigs;

public class CreateHelper {
	public static int maxRotationSpeed = 256;
	public static void refresh() {
		if (CreateCheck.isInstalled()) {
			maxRotationSpeed = AllConfigs.server().kinetics.maxRotationSpeed.get();
		}
	}
}
