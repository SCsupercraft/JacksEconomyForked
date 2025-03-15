package me.khajiitos.jackseconomy.create;

import com.simibubi.create.infrastructure.config.AllConfigs;

import java.util.HashSet;
import java.util.Set;

public class CreateHelper {
	private static final Set<Runnable> providers = new HashSet<>();
	public static int maxRotationSpeed = 256;
	public static void refresh() {
		if (CreateCheck.isInstalled()) {
			maxRotationSpeed = AllConfigs.server().kinetics.maxRotationSpeed.get();
		}
		providers.forEach(Runnable::run);
	}

	public static void addProvider(Runnable runnable) {
		providers.add(runnable);
	}
}
