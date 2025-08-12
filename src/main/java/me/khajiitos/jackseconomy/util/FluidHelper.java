package me.khajiitos.jackseconomy.util;

import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nullable;

public class FluidHelper {
    public static @Nullable Fluid getFluid(String name) {
        try {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(name);
            Fluid fluid = JacksEconomy.registryAccess().registryOrThrow(Registries.FLUID).get(resourceLocation);

            if (fluid == null) {
                JacksEconomy.LOGGER.info("Invalid fluid: " + name);
            }

            return fluid;
        } catch (ResourceLocationException e) {
            JacksEconomy.LOGGER.warn("Invalid resource location: " + name);
        }

        return null;
    }

    public static @Nullable ResourceLocation getFluidResourceLocation(Fluid fluid) {
        return JacksEconomy.registryAccess().registryOrThrow(Registries.FLUID).getKey(fluid);
    }

    public static String getItemName(Fluid fluid) {
        ResourceLocation resourceLocation = getFluidResourceLocation(fluid);
        return resourceLocation != null ? resourceLocation.toString() : null;
    }

    public static Holder<Fluid> getHolder(Fluid fluid) {
        return JacksEconomy.registryAccess().registryOrThrow(Registries.FLUID).getHolder(getFluidResourceLocation(fluid)).orElseThrow();
    }
}
