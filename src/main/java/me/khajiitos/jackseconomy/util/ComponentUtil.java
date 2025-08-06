package me.khajiitos.jackseconomy.util;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

import java.util.Optional;

public class ComponentUtil {
	public static DataComponentPatch clone(DataComponentPatch components) {
		DataComponentPatch.Builder builder = DataComponentPatch.builder();
		components.entrySet().forEach(entry -> setComponent(builder, entry.getKey(), entry.getValue()));
		return builder.build();
	}

	@SuppressWarnings({ "unchecked", "OptionalUsedAsFieldOrParameterType" })
	private static <T> void setComponent(DataComponentPatch.Builder builder, DataComponentType<?> componentType, Optional<?> optional) {
		optional.ifPresent(o -> builder.set((DataComponentType<T>) componentType, (T) o));
	}
}
