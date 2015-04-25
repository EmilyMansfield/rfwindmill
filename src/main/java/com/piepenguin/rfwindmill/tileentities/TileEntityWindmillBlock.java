package com.piepenguin.rfwindmill.tileentities;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import com.piepenguin.rfwindmill.lib.EnergyStorage;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public final class TileEntityWindmillBlock extends TileEntity implements IEnergyProvider {

    private EnergyStorage storage;
    private static final int tunnelRange = 10;
    private static final int minHeight = 60;
    private static final int maxHeight = 100;
    private static final String NBT_MAXIMUM_ENERGY_GENERATION = "RFWMaximumEnergyGeneration";
    private int maximumEnergyGeneration;
    private float fractionalRF = 0.0f;

    public static final String publicName = "tileEntityWindmillBlock";
    private static final String name = "tileEntityWindmillBlock";

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
    }

    @Override
    public void readFromNBT(NBTTagCompound pNbt) {
        super.readFromNBT(pNbt);
        maximumEnergyGeneration = pNbt.getInteger(NBT_MAXIMUM_ENERGY_GENERATION);
        storage.readFromNBT(pNbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound pNbt) {
        super.writeToNBT(pNbt);
        pNbt.setInteger(NBT_MAXIMUM_ENERGY_GENERATION, maximumEnergyGeneration);
        storage.writeToNBT(pNbt);
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
    }

    private int getTunnelOneSidedLength(ForgeDirection pDirection) {
        for(int i = 1; i <= tunnelRange; ++i) {
            if(worldObj.getBlock(
                    xCoord + pDirection.offsetX * i,
                    yCoord + pDirection.offsetY * i,
                    zCoord + pDirection.offsetZ * i).getMaterial() != Material.air) {
                return i-1;
            }
        }

        return tunnelRange;
    }

    private int getTunnelLength() {
        int rangeA = getTunnelOneSidedLength(metadataToDirection());
        int rangeB = getTunnelOneSidedLength(metadataToDirection().getOpposite());

        return Math.min(rangeA, rangeB);
    }

    private void transferEnergy() {
        for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity tile = getWorldObj().getTileEntity(
                    xCoord + direction.offsetX,
                    yCoord + direction.offsetY,
                    zCoord + direction.offsetZ);
            if(tile instanceof IEnergyReceiver) {
                IEnergyReceiver receiver = (IEnergyReceiver)tile;
                extractEnergy(direction.getOpposite(), receiver.receiveEnergy(direction.getOpposite(), storage.getExtract(), false), false);
            }
        }
    }

    private ForgeDirection metadataToDirection() {
        switch(getBlockMetadata()) {
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
    public int extractEnergy(ForgeDirection pFrom, int pMaxExtract, boolean pSimulate) {
        if(canConnectEnergy(pFrom)) {
            return storage.extractEnergy(pMaxExtract, pSimulate);
        }
        else {
            return 0;
        }
    }

    @Override
    public int getEnergyStored(ForgeDirection pFrom) {
        return storage.getEnergyStored();
    }

    public int getEnergyStored() {
        return getEnergyStored(ForgeDirection.NORTH);
    }

    public void setEnergyStored(int pEnergy) {
        storage.setEnergyStored(pEnergy);
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection pFrom) {
        return storage.getMaxEnergyStored();
    }

    public int getMaxEnergyStored() {
        return getMaxEnergyStored(ForgeDirection.NORTH);
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection pFrom) {
        return pFrom != metadataToDirection() && pFrom != metadataToDirection().getOpposite();
    }
}
