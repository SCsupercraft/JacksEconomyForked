package me.khajiitos.jackseconomy.util;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.energy.IEnergyStorage;

public interface IEnergyCapable {
	IEnergyStorage getEnergyStorage(Direction direction);
}
