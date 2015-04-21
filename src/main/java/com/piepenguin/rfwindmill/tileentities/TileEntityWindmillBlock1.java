package com.piepenguin.rfwindmill.tileentities;

import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityWindmillBlock1 extends TileEntity implements IEnergyProvider {

    private EnergyStorage storage = new EnergyStorage(12000, 1);

    public static final String publicName = "tileEntityWindmillBlock1";
    private String name = "tileEntityWindmillBlock1";

    public String getName() {
        return name;
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if(!worldObj.isRemote) {
            storage.modifyEnergyStored(4);
            if(storage.getEnergyStored() > 100) {
                transferEnergy();
            }
        }
        return;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        storage.readFromNBT(nbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        storage.writeToNBT(nbt);
    }

    private void transferEnergy() {
        for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity tile = getWorldObj().getTileEntity(
                    xCoord + direction.offsetX,
                    yCoord + direction.offsetY,
                    zCoord + direction.offsetZ);
            if(tile instanceof IEnergyReceiver) {
                IEnergyReceiver receiver = (IEnergyReceiver)tile;
                extractEnergy(direction.getOpposite(), receiver.receiveEnergy(direction.getOpposite(), storage.getMaxExtract(), false), false);
            }
        }
        return;
    }

    @Override
    public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
        if(from != ForgeDirection.NORTH) {
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

    @Override
    public int getMaxEnergyStored(ForgeDirection from) {
        return storage.getMaxEnergyStored();
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection from) {
        return from != ForgeDirection.NORTH;
    }
}
