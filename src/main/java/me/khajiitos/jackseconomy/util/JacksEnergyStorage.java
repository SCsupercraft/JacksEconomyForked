package me.khajiitos.jackseconomy.util;

import net.neoforged.neoforge.energy.EnergyStorage;

public class JacksEnergyStorage extends EnergyStorage {
    public JacksEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }
}
