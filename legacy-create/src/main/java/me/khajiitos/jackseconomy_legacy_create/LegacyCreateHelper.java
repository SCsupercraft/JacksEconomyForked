package me.khajiitos.jackseconomy_legacy_create;

import com.simibubi.create.infrastructure.config.AllConfigs;
import me.khajiitos.jackseconomy.create.CreateHelper;

public class LegacyCreateHelper {
	static void provideValues() {
		CreateHelper.maxRotationSpeed = AllConfigs.server().kinetics.maxRotationSpeed.get();
	}
}
