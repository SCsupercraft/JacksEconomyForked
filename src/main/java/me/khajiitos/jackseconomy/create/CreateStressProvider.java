package me.khajiitos.jackseconomy.create;

import com.simibubi.create.content.kinetics.BlockStressValues;
import com.simibubi.create.foundation.utility.Couple;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class CreateStressProvider {
	public static void init() {
		BlockStressValues.registerProvider(JacksEconomy.MOD_ID, new BlockStressValues.IStressValueProvider() {
			@Override
			public double getImpact(Block block) {
				if (block == ItemBlockReg.MECHANICAL_EXPORTER.get() || block == ItemBlockReg.MECHANICAL_FLUID_EXPORTER.get()) {
					return Config.mechanicalExporterStressPerRPM.get();
				} else if (block == ItemBlockReg.MECHANICAL_IMPORTER.get() || block == ItemBlockReg.MECHANICAL_FLUID_IMPORTER.get()) {
					return Config.mechanicalImporterStressPerRPM.get();
				}
				return 0;
			}

			@Override
			public double getCapacity(Block block) {
				return 0;
			}

			@Override
			public boolean hasImpact(Block block) {
				return getImpact(block) != 0;
			}

			@Override
			public boolean hasCapacity(Block block) {
				return false;
			}

			@Nullable
			@Override
			public Couple<Integer> getGeneratedRPM(Block block) {
				return null;
			}
		});
	}
}
