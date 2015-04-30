package com.piepenguin.rfwindmill.tileentities;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import com.piepenguin.rfwindmill.blocks.RotorBlock;
import com.piepenguin.rfwindmill.blocks.WindmillBlock;
import com.piepenguin.rfwindmill.lib.EnergyStorage;
import com.piepenguin.rfwindmill.lib.Util;
import net.minecraft.block.Block;
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
    private static final String NBT_HAS_ROTOR =  "RFWHasRotor";
    private static final String NBT_ROTOR_DIR = "RFWRotorDir";
    private int maximumEnergyGeneration;
    private boolean hasRotor;
    private ForgeDirection rotorDir;

    public static final String publicName = "tileEntityWindmillBlock";
    private static final String name = "tileEntityWindmillBlock";

    public TileEntityWindmillBlock() {
        this(0, 0, 0);
    }

    public TileEntityWindmillBlock(int pMaximumEnergyGeneration, int pMaximumEnergyTransfer, int pCapacity) {
        storage = new EnergyStorage(pCapacity, pMaximumEnergyTransfer);
        maximumEnergyGeneration = pMaximumEnergyGeneration;
        hasRotor = false;
        rotorDir = ForgeDirection.NORTH;
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
        hasRotor = pNbt.getBoolean(NBT_HAS_ROTOR);
        rotorDir = Util.intToDirection(pNbt.getInteger(NBT_ROTOR_DIR));
        storage.readFromNBT(pNbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound pNbt) {
        super.writeToNBT(pNbt);
        pNbt.setInteger(NBT_MAXIMUM_ENERGY_GENERATION, maximumEnergyGeneration);
        pNbt.setBoolean(NBT_HAS_ROTOR, hasRotor);
        pNbt.setInteger(NBT_ROTOR_DIR, Util.directionToInt(rotorDir));
        storage.writeToNBT(pNbt);
    }

    public float getEnergyGeneration() {
        if(!hasRotor()) return 0;
        return getTheoreticalEnergyGeneration();
    }

    public float getTheoreticalEnergyGeneration() {
        int deltaHeight = maxHeight - minHeight;
        if(deltaHeight <= 0) deltaHeight = 1;

        float heightModifier = (float)Math.min(Math.max(yCoord - minHeight, 0), deltaHeight) / (float)deltaHeight;

        return maximumEnergyGeneration * getTunnelLength() * heightModifier * 0.5f;
    }

    public int getMaximumEnergyGeneration() {
        return maximumEnergyGeneration;
    }

    private void generateEnergy() {
        storage.modifyEnergyStored(getEnergyGeneration());
    }

    private int getTunnelLengthSingleBlock(int pX, int pY, int pZ, ForgeDirection pDirection) {
        for(int i = 1; i <= tunnelRange; ++i) {
            int dx = pX + pDirection.offsetX * i;
            int dy = pY + pDirection.offsetY * i;
            int dz = pZ + pDirection.offsetZ * i;
            // Ignore vertical middle column
            if(dx == 0 && dz == 0) {
                continue;
            }
            Block block = worldObj.getBlock(dx, dy, dz);
            if(block == null || (block.getMaterial() != Material.air &&
                    !(block instanceof RotorBlock) &&
                    !(block instanceof WindmillBlock)
            )) {
                return i-1;
            }
        }

        return tunnelRange;
    }

    private int getTunnelLengthTwoSided(int pX, int pY, int pZ, ForgeDirection pDirection) {
        int rangeA = getTunnelLengthSingleBlock(pX, pY, pZ, pDirection);
        int rangeB = getTunnelLengthSingleBlock(pX, pY, pZ, pDirection.getOpposite());

        return Math.min(rangeA, rangeB);
    }

    private int getTunnelLength() {
        int range = 10;
        // If rotor dir is north or south then check xy plane
        if(rotorDir == ForgeDirection.NORTH || rotorDir == ForgeDirection.SOUTH) {
            for (int x = -1; x <= 1; ++x) {
                for (int y = -1; y <= 1; ++y) {
                    int r = getTunnelLengthTwoSided(xCoord + x, yCoord + y, zCoord, rotorDir);
                    if (r < range) {
                        range = r;
                    }
                }
            }
        }
        // Terrible lack of code reuse, better way?
        else {
            // Check yz plane
            for (int z = -1; z <= 1; ++z) {
                for (int y = -1; y <= 1; ++y) {
                    int r = getTunnelLengthTwoSided(xCoord, yCoord + y, zCoord + z, rotorDir);
                    if (r < range) {
                        range = r;
                    }
                }
            }
        }
        return range;
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
        return true;
    }

    public boolean hasRotor() {
        return hasRotor;
    }

    // fDir points towards the rotor
    public void setRotor(boolean pHasRotor, ForgeDirection fDir) {
        hasRotor = pHasRotor;
        rotorDir = fDir;
    }

    public ForgeDirection getRotorDir() {
        return rotorDir;
    }
}
