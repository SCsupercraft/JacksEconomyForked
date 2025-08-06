package me.khajiitos.jackseconomy.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface IDisablable {
	boolean isDisabled();

	default List<Component> getDisabledTooltip() {
		return List.of(Component.translatable("jackseconomy.item_disabled").withStyle(ChatFormatting.RED));
	}
}
