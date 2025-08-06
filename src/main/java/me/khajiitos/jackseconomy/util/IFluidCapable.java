package me.khajiitos.jackseconomy.util;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public interface IFluidCapable {
	IFluidHandler getFluidCapability(Direction direction);
}
