package com.piepenguin.rfwindmill.lib;

import cofh.api.energy.IEnergyStorage;
import net.minecraft.nbt.NBTTagCompound;

public class EnergyStorage implements IEnergyStorage {

    protected int energy;
    protected int capacity;
    protected int maxReceive;
    protected int maxExtract;

    public static final String NBT_ENERGY = "ESEnergy";
    public static final String NBT_CAPACITY = "ESCapacity";
    public static final String NBT_MAX_RECEIVE = "ESMaxReceive";
    public static final String NBT_MAX_EXTRACT = "ESMaxExtract";

    public EnergyStorage() {
        this(0, 0, 0);
    }

    public EnergyStorage(int pCapacity, int pMaxReceive, int pMaxExtract) {
        capacity = pCapacity;
        maxReceive = pMaxReceive;
        maxExtract = pMaxExtract;
    }

    public EnergyStorage(int pCapacity, int pMaxTransfer) {
        this(pCapacity, pMaxTransfer, pMaxTransfer);
    }

    public EnergyStorage(int pCapacity) {
        this(pCapacity, pCapacity, pCapacity);
    }

    public void readFromNBT(NBTTagCompound pNbt) {
        setMaxEnergyStored(pNbt.getInteger(NBT_CAPACITY));
        setMaxReceive(pNbt.getInteger(NBT_MAX_RECEIVE));
        setMaxExtract(pNbt.getInteger(NBT_MAX_EXTRACT));
        setEnergyStored(pNbt.getInteger(NBT_ENERGY));
    }

    public void writeToNBT(NBTTagCompound pNbt) {
        if(energy < 0) {
            energy = 0;
        }
        pNbt.setInteger(NBT_CAPACITY, getMaxEnergyStored());
        pNbt.setInteger(NBT_MAX_RECEIVE, getMaxReceive());
        pNbt.setInteger(NBT_MAX_EXTRACT, getMaxExtract());
        pNbt.setInteger(NBT_ENERGY, getEnergyStored());
    }

    @Override
    public int receiveEnergy(int pMaxReceive, boolean pSimulate) {
        int energyReceived = Math.min(capacity - energy, Math.min(maxReceive, pMaxReceive));

        if(!pSimulate) {
            energy += energyReceived;
        }

        return energyReceived;
    }

    @Override
    public int extractEnergy(int pMaxExtract, boolean pSimulate) {
        int energyExtracted = Math.min(energy, Math.min(maxExtract, pMaxExtract));

        if(!pSimulate) {
            energy -= energyExtracted;
        }

        return energyExtracted;
    }

    public void modifyEnergyStored(int pEnergy) {
        energy += pEnergy;

        if(energy > capacity) {
            energy = capacity;
        }
        else if(energy < 0) {
            energy = 0;
        }
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    public void setEnergyStored(int pEnergy) {
        energy = pEnergy;
        if(energy > capacity) {
            energy = capacity;
        }
        else if(energy < 0) {
            energy = 0;
        }
    }

    public int getExtract() {
        return Math.min(maxExtract, energy);
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }

    public void setMaxEnergyStored(int pCapacity) {
        this.capacity = pCapacity;
    }

    public int getMaxExtract() {
        return maxExtract;
    }

    public void setMaxExtract(int pMaxExtract) {
        maxExtract = pMaxExtract;
    }

    public int getMaxReceive() {
        return maxReceive;
    }

    public void setMaxReceive(int pMaxReceive) {
        maxReceive = pMaxReceive;
    }

    public void setMaxTransfer(int pMaxTransfer) {
        setMaxExtract(pMaxTransfer);
        setMaxReceive(pMaxTransfer);
    }

}

