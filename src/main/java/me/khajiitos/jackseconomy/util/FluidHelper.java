package me.khajiitos.jackseconomy.util;

import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class FluidHelper {
    public static @Nullable Fluid getFluid(String name) {
        try {
            ResourceLocation resourceLocation = new ResourceLocation(name);
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(resourceLocation);

            if (fluid == null) {
                JacksEconomy.LOGGER.info("Invalid fluid: {}", name);
            }

            return fluid;
        } catch (ResourceLocationException e) {
            JacksEconomy.LOGGER.warn("Invalid resource location: {}", name);
        }

        return null;
    }

    public static String getFluidName(Fluid fluid) {
        ResourceLocation resourceLocation = ForgeRegistries.FLUIDS.getKey(fluid);

        if (resourceLocation != null) {
            return resourceLocation.toString();
        }

        return null;
    }
}
