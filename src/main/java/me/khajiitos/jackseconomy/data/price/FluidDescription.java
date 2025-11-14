package me.khajiitos.jackseconomy.data.price;

import com.mojang.serialization.Codec;
import me.khajiitos.jackseconomy.util.FluidHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;

public record FluidDescription(Fluid fluid, CompoundTag compoundTag) {
    public static final Codec<FluidDescription> CODEC = FluidStack.CODEC.xmap(FluidDescription::ofFluid, FluidDescription::createFluidStack);

    public FluidDescription(Fluid fluid, @Nullable CompoundTag compoundTag) {
        this.fluid = fluid;

        if (compoundTag == null) {
            this.compoundTag = new CompoundTag();
        } else {
            this.compoundTag = compoundTag.copy();
        }
    }

    public static FluidDescription ofItem(ItemStack itemStack) {
        IFluidHandlerItem cap = itemStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(IllegalArgumentException::new);
        return ofFluid(cap.getFluidInTank(0));
    }

    public static FluidDescription ofFluid(FluidStack fluidStack) {
        return new FluidDescription(fluidStack.getFluid(), fluidStack.getTag());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other instanceof FluidDescription fluidDescription) {
            return fluid.equals(fluidDescription.fluid) && compoundTag.equals(fluidDescription.compoundTag);
        }
        return false;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        String fluidName = FluidHelper.getFluidName(this.fluid);

        tag.putString("fluid", fluidName != null ? fluidName : "");

        if (!this.compoundTag.isEmpty()) {
            tag.put("nbt", this.compoundTag.copy());
        }

        return tag;
    }

    public static @Nullable FluidDescription fromNbt(CompoundTag compoundTag) {
        String fluidName = compoundTag.getString("fluid");

        if (fluidName.isEmpty()) {
            return null;
        }

        Fluid fluid = FluidHelper.getFluid(fluidName);

        if (fluid == null) {
            return null;
        }

        CompoundTag tag = compoundTag.getCompound("nbt");

        return new FluidDescription(fluid, tag);
    }

    public FluidStack createFluidStack() {
        FluidStack fluidStack = new FluidStack(this.fluid, 1);

        CompoundTag tag = this.compoundTag();

        if (!tag.isEmpty()) {
            fluidStack.setTag(tag.copy());
        }

        return fluidStack;
    }
}