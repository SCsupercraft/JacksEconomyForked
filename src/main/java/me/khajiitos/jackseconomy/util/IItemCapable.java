package me.khajiitos.jackseconomy.util;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.items.IItemHandler;

public interface IItemCapable {
	IItemHandler getItemCapability(Direction direction);
}
