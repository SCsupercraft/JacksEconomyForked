package me.khajiitos.jackseconomy.data.price;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.util.ComponentUtil;
import me.khajiitos.jackseconomy.util.NBTUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;
import java.util.Objects;

public record FluidDescription(Holder<Fluid> fluid, DataComponentPatch components) {
    public static final Codec<FluidDescription> CODEC = FluidStack.CODEC.xmap(FluidDescription::ofFluid, FluidDescription::createFluidStack);
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidDescription> STREAM_CODEC = FluidStack.STREAM_CODEC.map(FluidDescription::ofFluid, FluidDescription::createFluidStack);

    public FluidDescription(Holder<Fluid> fluid, @Nullable DataComponentPatch components) {
        this.fluid = fluid;

        if (components == null) {
            this.components = DataComponentPatch.builder().build();
        } else {
            this.components = ComponentUtil.clone(components);
        }
    }

    public static @Nullable FluidDescription ofItem(ItemStack itemStack) {
        IFluidHandlerItem cap = itemStack.getCapability(Capabilities.FluidHandler.ITEM);
        return cap != null ? ofFluid(cap.getFluidInTank(0)) : null;
    }

    public static FluidDescription ofFluid(FluidStack fluidStack) {
        return new FluidDescription(fluidStack.getFluidHolder(), fluidStack.getComponentsPatch());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other instanceof FluidDescription fluidDescription) {
            return fluid.equals(fluidDescription.fluid) && components.equals(fluidDescription.components);
        }
        return false;
    }

    public CompoundTag toNbt() {
        return (CompoundTag) createFluidStack().save(Objects.requireNonNull(JacksEconomy.registryAccess()));
    }

    public static @Nullable FluidDescription fromNbt(Tag tag) {
        FluidStack stack = FluidStack.parse(Objects.requireNonNull(JacksEconomy.registryAccess()), tag).orElse(null);
        return stack != null ? ofFluid(stack) : null;
    }

    public FluidStack createFluidStack() {
        return new FluidStack(this.fluid, 1, this.components);
    }

    public JsonObject toJson() {
        JsonElement jsonElement = NBTUtil.nbtToJson(this.toNbt());

        if (jsonElement instanceof JsonObject object) {
            return object;
        } else {
            return new JsonObject();
        }
    }

    public static @Nullable FluidDescription fromJson(JsonObject json) {
        Tag tag = NBTUtil.jsonToNbt(json);

        if (tag instanceof CompoundTag compoundTag) {
            return FluidDescription.fromNbt(compoundTag);
        } else {
            return null;
        }
    }
}