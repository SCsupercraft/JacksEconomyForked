package me.khajiitos.jackseconomy.create;

import com.simibubi.create.api.stress.BlockStressValues;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import net.minecraft.world.level.block.Block;

import java.util.function.DoubleSupplier;

public class CreateStressProvider {
	public static void init() {
		BlockStressValues.IMPACTS.registerProvider(CreateStressProvider::getImpact);
	}

	private static DoubleSupplier getImpact(Block block) {
		if (block == ItemBlockReg.MECHANICAL_EXPORTER.get() || block == ItemBlockReg.MECHANICAL_FLUID_EXPORTER.get()) {
			return Config.mechanicalExporterStressPerRPM::get;
		} else if (block == ItemBlockReg.MECHANICAL_IMPORTER.get() || block == ItemBlockReg.MECHANICAL_FLUID_IMPORTER.get()) {
			return Config.mechanicalImporterStressPerRPM::get;
		}
		return null;
	}
}
