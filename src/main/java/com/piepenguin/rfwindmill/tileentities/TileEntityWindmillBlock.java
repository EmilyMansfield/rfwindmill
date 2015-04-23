package com.piepenguin.rfwindmill.tileentities;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import com.piepenguin.rfwindmill.lib.EnergyStorage;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityWindmillBlock extends TileEntity implements IEnergyProvider {

    private EnergyStorage storage;
    private static final int tunnelRange = 10;
    private static final int minHeight = 60;
    private static final int maxHeight = 100;
    private static final String NBT_MAXIMUM_ENERGY_GENERATION = "RFWMaximumEnergyGeneration";
    private int maximumEnergyGeneration;
    private float fractionalRF = 0.0f;

    public static final String publicName = "tileEntityWindmillBlock";
    private String name = "tileEntityWindmillBlock";

    public TileEntityWindmillBlock() {
        this(0, 0, 0);
    }

    public TileEntityWindmillBlock(int pMaximumEnergyGeneration, int pMaximumEnergyTransfer, int pCapacity) {
        storage = new EnergyStorage(pCapacity, pMaximumEnergyTransfer);
        maximumEnergyGeneration = pMaximumEnergyGeneration;
    }

    public String getName() {
        return name;
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if(!worldObj.isRemote) {
            generateEnergy();
            if(storage.getEnergyStored() > 0) {
                transferEnergy();
            }
        }
        return;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        maximumEnergyGeneration = nbt.getInteger(NBT_MAXIMUM_ENERGY_GENERATION);
        storage.readFromNBT(nbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger(NBT_MAXIMUM_ENERGY_GENERATION, maximumEnergyGeneration);
        storage.writeToNBT(nbt);
    }

    private void generateEnergy() {
        int deltaHeight = maxHeight - minHeight;
        if(deltaHeight <= 0) deltaHeight = 1;

        float heightModifier = (float)Math.min(Math.max(yCoord - minHeight, 0), deltaHeight) / (float)deltaHeight;
        float energyProduced = maximumEnergyGeneration * getTunnelLength() * heightModifier * 0.5f;

        // Dodgy floating point RF handling to smooth out height effect
        fractionalRF += energyProduced - (int)energyProduced;
        if(fractionalRF >= 1.0f)
        {
            fractionalRF -= 1.0f;
            energyProduced += 1.0f;
        }

        storage.modifyEnergyStored((int)energyProduced);

        return;
    }

    private int getTunnelLength() {
        int northRange = tunnelRange;
        int southRange = tunnelRange;
        // North/South is default facing (z axis)
        for(int i = -1; i >= -tunnelRange; --i) {
            if(blockMetadata == 0 || blockMetadata == 2) {
                if(worldObj.getBlock(xCoord, yCoord, zCoord + i).getMaterial() != Material.air) {
                    northRange = -i - 1;
                    break;
                }
            }
            else {
                if(worldObj.getBlock(xCoord + i, yCoord, zCoord).getMaterial() != Material.air) {
                    northRange = -i - 1;
                    break;
                }
            }
        }
        for(int i = 1; i <= tunnelRange; ++i) {
            if(blockMetadata == 0 || blockMetadata == 2) {
                if(worldObj.getBlock(xCoord, yCoord, zCoord + i).getMaterial() != Material.air) {
                    southRange = i-1;
                    break;
                }
            }
            else {
                if(worldObj.getBlock(xCoord + i, yCoord, zCoord).getMaterial() != Material.air) {
                    southRange = i-1;
                    break;
                }
            }
        }
        return Math.min(northRange, southRange);
    }

    private void transferEnergy() {
        for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity tile = getWorldObj().getTileEntity(
                    xCoord + direction.offsetX,
                    yCoord + direction.offsetY,
                    zCoord + direction.offsetZ);
            if(tile instanceof IEnergyReceiver) {
                IEnergyReceiver receiver = (IEnergyReceiver)tile;
                receiver.receiveEnergy(direction.getOpposite(), extractEnergy(direction.getOpposite(), storage.getMaxExtract(), false), false);
            }
        }
        return;
    }

    private ForgeDirection metadataToDirection() {
        switch(blockMetadata) {
            case 0:
                return ForgeDirection.NORTH;
            case 1:
                return ForgeDirection.EAST;
            case 2:
                return ForgeDirection.SOUTH;
            case 3:
                return ForgeDirection.WEST;
            default:
                return ForgeDirection.NORTH;
        }
    }

    @Override
    public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
        if(from != metadataToDirection() && from != metadataToDirection().getOpposite()) {
            return storage.extractEnergy(maxExtract, simulate);
        }
        else {
            return 0;
        }
    }

    @Override
    public int getEnergyStored(ForgeDirection from) {
        return storage.getEnergyStored();
    }

    public int getEnergyStored() {
        return getEnergyStored(ForgeDirection.NORTH);
    }

    public void setEnergyStored(int pEnergy) {
        storage.setEnergyStored(pEnergy);
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from) {
        return storage.getMaxEnergyStored();
    }

    public int getMaxEnergyStored() {
        return getMaxEnergyStored(ForgeDirection.NORTH);
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection from) {
        return from != metadataToDirection() && from != metadataToDirection().getOpposite();
    }
}
