package me.khajiitos.jackseconomy.blockentity;

import me.khajiitos.jackseconomy.data.price.FluidDescription;

public interface IFluidImporterBlockEntity extends IFluidTransactionMachineBlockEntity {
    void selectFluid(FluidDescription fluidDescription);
    FluidDescription getSelectedFluid();

    void setRoundRobin(boolean enabled);
    boolean isRoundRobinEnabled();
}
