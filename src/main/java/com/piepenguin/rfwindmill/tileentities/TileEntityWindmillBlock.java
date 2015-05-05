package com.piepenguin.rfwindmill.tileentities;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import com.piepenguin.rfwindmill.blocks.RotorBlock;
import com.piepenguin.rfwindmill.blocks.WindmillBlock;
import com.piepenguin.rfwindmill.lib.EnergyStorage;
import com.piepenguin.rfwindmill.lib.ModConfiguration;
import com.piepenguin.rfwindmill.lib.Util;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public final class TileEntityWindmillBlock extends TileEntity implements IEnergyProvider {

    private EnergyStorage storage;
    private static final int tunnelRange = 10;
    private static final int minHeight = 60;
    private static final int maxHeight = 100;
    private static final String NBT_MAXIMUM_ENERGY_GENERATION = "RFWMaximumEnergyGeneration";
    private static final String NBT_ROTOR_TYPE =  "RFWRotorType";
    private static final String NBT_ROTOR_DIR = "RFWRotorDir";
    private static final String NBT_CURRENT_ENERGY_GENERATION = "RFWCurrentEnergyGeneration";
    private int maximumEnergyGeneration;
    private float currentEnergyGeneration;
    private float oldEnergyGeneration = 0.0f;
    private int rotorType; // -1 if no rotor
    private ForgeDirection rotorDir;

    public static final String publicName = "tileEntityWindmillBlock";
    private static final String name = "tileEntityWindmillBlock";

    public TileEntityWindmillBlock() {
        this(0, 0, 0);
    }

    public TileEntityWindmillBlock(int pMaximumEnergyGeneration, int pMaximumEnergyTransfer, int pCapacity) {
        storage = new EnergyStorage(pCapacity, pMaximumEnergyTransfer);
        maximumEnergyGeneration = pMaximumEnergyGeneration;
        rotorType = -1;
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

    public void readSyncableDataFromNBT(NBTTagCompound pNbt) {
        currentEnergyGeneration = pNbt.getFloat(NBT_CURRENT_ENERGY_GENERATION);
    }

    public void writeSyncableDataToNBT(NBTTagCompound pNbt) {
        pNbt.setFloat(NBT_CURRENT_ENERGY_GENERATION, currentEnergyGeneration);
    }

    @Override
    public void readFromNBT(NBTTagCompound pNbt) {
        super.readFromNBT(pNbt);
        maximumEnergyGeneration = pNbt.getInteger(NBT_MAXIMUM_ENERGY_GENERATION);
        rotorType = pNbt.getInteger(NBT_ROTOR_TYPE);
        rotorDir = Util.intToDirection(pNbt.getInteger(NBT_ROTOR_DIR));
        readSyncableDataFromNBT(pNbt);
        storage.readFromNBT(pNbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound pNbt) {
        super.writeToNBT(pNbt);
        pNbt.setInteger(NBT_MAXIMUM_ENERGY_GENERATION, maximumEnergyGeneration);
        pNbt.setInteger(NBT_ROTOR_TYPE, rotorType);
        pNbt.setInteger(NBT_ROTOR_DIR, Util.directionToInt(rotorDir));
        writeSyncableDataToNBT(pNbt);
        storage.writeToNBT(pNbt);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound syncData = new NBTTagCompound();
        writeSyncableDataToNBT(syncData);

        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, syncData);
    }

    @Override
    public void onDataPacket(NetworkManager pNet, S35PacketUpdateTileEntity pPacket) {
        readSyncableDataFromNBT(pPacket.func_148857_g());
    }

    public float getEnergyGeneration() {
        if(!hasRotor()) return 0;
        return getTheoreticalEnergyGeneration();
    }

    public float getTheoreticalEnergyGeneration() {
        int deltaHeight = maxHeight - minHeight;
        if(deltaHeight <= 0) deltaHeight = 1;

        float heightModifier = (float)Math.min(Math.max(yCoord - minHeight, 0), deltaHeight) / (float)deltaHeight;

        return maximumEnergyGeneration * getTunnelLength() * heightModifier * 0.4f * ModConfiguration.getRotorEnergyMultiplier(rotorType);
    }

    public int getMaximumEnergyGeneration() {
        return maximumEnergyGeneration;
    }

    public float getCurrentEnergyGeneration() {
        return currentEnergyGeneration;
    }

    private void generateEnergy() {
        currentEnergyGeneration = getEnergyGeneration();
        // Amount of energy generated has changed so sync with server
        if((int)currentEnergyGeneration != (int)oldEnergyGeneration) {
            oldEnergyGeneration = currentEnergyGeneration;
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            markDirty();
        }
        storage.modifyEnergyStored(currentEnergyGeneration);
    }

    private int getTunnelLengthSingleBlock(int pX, int pY, int pZ, ForgeDirection pDirection, boolean ignoreFirst) {
        for(int i = 1; i <= tunnelRange; ++i) {
            int dx = pX + pDirection.offsetX * i;
            int dy = pY + pDirection.offsetY * i;
            int dz = pZ + pDirection.offsetZ * i;
            // Skip first block if specified
            if(ignoreFirst && i == 1)
            {
                continue;
            }
            Block block = worldObj.getBlock(dx, dy, dz);
            if(block == null || (block.getMaterial() != Material.air)) {
                return i-1;
            }
        }

        return tunnelRange;
    }

    private int getTunnelLengthTwoSided(int pX, int pY, int pZ, ForgeDirection pDirection, boolean ignoreFirst) {
        int rangeA = getTunnelLengthSingleBlock(pX, pY, pZ, pDirection, ignoreFirst);
        // Only ignore block on the side the rotor is on
        int rangeB = getTunnelLengthSingleBlock(pX, pY, pZ, pDirection.getOpposite(), false);

        return Math.min(rangeA, rangeB);
    }

    private int getTunnelLength() {
        int range = tunnelRange;
        // If rotor dir is north or south then check xy plane
        if(rotorDir == ForgeDirection.NORTH || rotorDir == ForgeDirection.SOUTH) {
            for(int x = -1; x <= 1; ++x) {
                for(int y = -1; y <= 1; ++y) {
                    int r = getTunnelLengthTwoSided(xCoord + x, yCoord + y, zCoord, rotorDir, (x == 0 && y == 0));
                    if(r < range) {
                        range = r;
                    }
                }
            }
        }
        // Terrible lack of code reuse, better way?
        else {
            // Check yz plane
            for(int z = -1; z <= 1; ++z) {
                for(int y = -1; y <= 1; ++y) {
                    int r = getTunnelLengthTwoSided(xCoord, yCoord + y, zCoord + z, rotorDir, (z == 0 && y == 0));
                    if(r < range) {
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
        return rotorType >= 0;
    }

    // fDir points towards the rotor
    public void setRotor(int pRotorType, ForgeDirection fDir) {
        rotorType = pRotorType;
        rotorDir = fDir;
    }

    public ForgeDirection getRotorDir() {
        return rotorDir;
    }
}
