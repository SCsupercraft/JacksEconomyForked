package me.khajiitos.jackseconomy.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class NameableBlockItem extends BlockItem {
	public NameableBlockItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	@Override
	public @NotNull Component getName(@NotNull ItemStack pStack) {
		if (super.getBlock() instanceof NameableBlock nameableBlock) return nameableBlock.getItemName(pStack);
		return super.getName(pStack);
	}

	public interface NameableBlock {
		MutableComponent getItemName(ItemStack stack);
	}
}
