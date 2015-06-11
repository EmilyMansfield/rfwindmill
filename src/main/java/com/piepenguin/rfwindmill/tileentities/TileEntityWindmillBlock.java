package com.piepenguin.rfwindmill.tileentities;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
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

/**
 * Tile entity for the {@link com.piepenguin.rfwindmill.blocks.WindmillBlock}
 * class, handles the energy generation, storage, and transfer. Energy produced
 * is dependent on height of the windmill, the rotor material, the free space
 * around the windmill, and the strength of the weather. See the
 * {@link #generateEnergy()} method for more.
 */
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
    private int toGenerate = 0;

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

    /**
     * Update the parent entity, generate RF and transfer as much as possible to
     * connected storage cells if energy is currently stored.
     */
    @Override
    public void updateEntity() {
        super.updateEntity();
        if(!worldObj.isRemote) {
            if(toGenerate > 0) {
                generateHandcrankEnergy();
                --toGenerate;
            }
            else {
                generateEnergy();
            }
            if(storage.getEnergyStored() > 0) {
                transferEnergy();
            }
        }
    }

    /**
     * Reads data from {@code pNbt} that must be synced between client and
     * server, namely the current energy generation.
     * @param pNbt NBT to read from
     */
    public void readSyncableDataFromNBT(NBTTagCompound pNbt) {
        currentEnergyGeneration = pNbt.getFloat(NBT_CURRENT_ENERGY_GENERATION);
    }

    /**
     * Writes data to {@code pNbt} that must be synced between client and
     * server, namely the current energy generation.
     * @param pNbt NBT to write to
     */
    public void writeSyncableDataToNBT(NBTTagCompound pNbt) {
        pNbt.setFloat(NBT_CURRENT_ENERGY_GENERATION, currentEnergyGeneration);
    }

    /**
     * Read the non-syncable data from {@code pNbt}.
     * @param pNbt NBT to read from
     */
    @Override
    public void readFromNBT(NBTTagCompound pNbt) {
        super.readFromNBT(pNbt);
        maximumEnergyGeneration = pNbt.getInteger(NBT_MAXIMUM_ENERGY_GENERATION);
        rotorType = pNbt.getInteger(NBT_ROTOR_TYPE);
        rotorDir = Util.intToDirection(pNbt.getInteger(NBT_ROTOR_DIR));
        readSyncableDataFromNBT(pNbt);
        storage.readFromNBT(pNbt);
    }

    /**
     * Write the non-syncable data to {@code pNbt}.
     * @param pNbt NBT to write to
     */
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

    /**
     * Get the actual current energy production rate in RF/t, which will be zero
     * if there is no rotor attached.
     * @return Actual energy generation in RF/t
     */
    public float getEnergyGeneration() {
        if(!hasRotor()) return 0;
        return getTheoreticalEnergyGeneration();
    }

    /**
     * Get the theoretical energy production rate in RF/t, ignoring the presence
     * of a rotor
     * @return Energy produced in RF/t
     */
    public float getTheoreticalEnergyGeneration() {
        int deltaHeight = maxHeight - minHeight;
        if(deltaHeight <= 0) deltaHeight = 1;

        float heightModifier = (float)Math.min(Math.max(yCoord - minHeight, 0), deltaHeight) / (float)deltaHeight;
        float weatherModifier = 1.0f;
        if(worldObj.isThundering()) {
            weatherModifier = ModConfiguration.getWeatherMultiplierThunder();
        }
        else if(worldObj.isRaining()) {
            weatherModifier = ModConfiguration.getWeatherMultiplierRain();
        }
        return maximumEnergyGeneration * weatherModifier * getTunnelLength() * heightModifier * 0.4f * ModConfiguration.getRotorEnergyMultiplier(rotorType);
    }

    /**
     * Maximum baseline energy that can be produced
     * @return Maximum RF/t
     */
    public int getMaximumEnergyGeneration() {
        return maximumEnergyGeneration;
    }

    /**
     * The current rate of energy production in RF/t
     * @return The current rate of energy production, 0 if no rotor attached
     */
    public float getCurrentEnergyGeneration() {
        return currentEnergyGeneration;
    }

    /**
     * Calculate the energy generation due to the wind and modify the energy.
     */
    private void generateEnergy() {
        currentEnergyGeneration = getEnergyGeneration();
        syncEnergy();
        storage.modifyEnergyStored(currentEnergyGeneration);
    }

    /**
     * Get the energy generation in RF/t at the current tick via the handcrank
     * system instead of wind.
     * @return Energy produced in RF/t
     */
    private float getHandcrankEnergyGeneration() {
        // Handcrank energy is a fixed (small) proportion of the total that
        // can be produced, and is independent of rotor type or environmental
        // factors
        return maximumEnergyGeneration * 0.4f;
    }

    /**
     * Calculate the energy generation due to the player and modify the energy.
     */
    private void generateHandcrankEnergy() {
        currentEnergyGeneration = getHandcrankEnergyGeneration();
        syncEnergy();
        storage.modifyEnergyStored(currentEnergyGeneration);
    }

    /**
     * Mark for handcrank energy generation for the next 4 ticks
     */
    public void handcrank() {
        toGenerate += 4;
    }

    /**
     * Sync the energy generation rate between client and server. (Used in
     * rendering with {@link RenderTileEntityRotorBlock}.)
     */
    private void syncEnergy() {
        // Amount of energy generated has changed so sync with server
        if((int)currentEnergyGeneration != (int)oldEnergyGeneration) {
            oldEnergyGeneration = currentEnergyGeneration;
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            markDirty();
        }
    }

    /**
     * Returns the number of free air blocks in a 1x1x{@code tunnelRange} up to
     * the first non-air block in the given direction and starting at the
     * given coordinate. Gives the length of a 'wind-tunnel' until an obstruction
     * is found.
     * @param pX X coordinate to start measuring from
     * @param pY Y coordinate to start measuring from
     * @param pZ Z coordinate to start measuring from
     * @param pDirection Direction to check the tunnel in
     * @param ignoreFirst If {@code true} then ignore the block at
     * ({@code pX}, {@code pY}, {@code pZ})
     * @return Length of the 'wind-tunnel'
     */
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

    /**
     * Gets the length of a 'wind-tunnel' as per
     * {@link #getTunnelLengthSingleBlock(int, int, int, ForgeDirection, boolean)}
     * but tunnel length is measured separately in two opposite directions and
     * the shortest length taken to be the length of the wind tunnel. The first
     * block is ignored in the given {@code pDirection} so as to ignore the
     * {@link com.piepenguin.rfwindmill.blocks.RotorBlock}.
     * @param pX X coordinate to start measuring from
     * @param pY Y coordinate to start measuring from
     * @param pZ Z coordinate to start measuring from
     * @param pDirection Direction the rotor is facing
     * @param ignoreFirst If {@code true} then ignore the block at
     *                    ({@code pX}, {@code pY}, {@code pZ}). The block is
     *                    never ignored when going in the opposite direction
     *                    however
     * @return Shortest unobstructed length of the two opposing 'wind-tunnel's
     */
    private int getTunnelLengthTwoSided(int pX, int pY, int pZ, ForgeDirection pDirection, boolean ignoreFirst) {
        int rangeA = getTunnelLengthSingleBlock(pX, pY, pZ, pDirection, ignoreFirst);
        // Only ignore block on the side the rotor is on
        int rangeB = getTunnelLengthSingleBlock(pX, pY, pZ, pDirection.getOpposite(), false);

        return Math.min(rangeA, rangeB);
    }

    /**
     * Get the length of a 3x3 tunnel in the same plane as the rotor as per
     * {@link #getTunnelLengthTwoSided(int, int, int, ForgeDirection, boolean)}
     * but take the shortest unobstructed range in the 3x3x{@code tunnelRange}
     * to be the length of the tunnel.
     * @return Length of the 'wind-tunnel'
     */
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

    /**
     * Transfer energy to any blocks demanding energy that are connected to
     * this one.
     */
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

    /**
     * Set the tier of the rotor connected to the corresponding
     * {@link com.piepenguin.rfwindmill.blocks.WindmillBlock}. -1 if no rotor is
     * connected.
     * @param pRotorType Tier of the rotor being attached. -1 if no rotor
     * @param fDir Direction the rotor is facing, i.e. the normal to the face
     *             the rotor is being placed on
     */
    public void setRotor(int pRotorType, ForgeDirection fDir) {
        rotorType = pRotorType;
        rotorDir = fDir;
    }

    public ForgeDirection getRotorDir() {
        return rotorDir;
    }
}
