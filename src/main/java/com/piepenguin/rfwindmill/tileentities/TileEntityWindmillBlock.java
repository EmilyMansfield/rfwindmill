package com.piepenguin.rfwindmill.tileentities;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import com.piepenguin.rfwindmill.lib.EnergyPacket;
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
 * around the windmill, and the strength of the weather..
 */
public final class TileEntityWindmillBlock extends TileEntity implements IEnergyProvider {

    private EnergyStorage storage;
    private static final int tunnelRange = 10;
    private static final int minHeight = 60;
    private static final int maxHeight = 100;
    private static final String NBT_EFFICIENCY = "RFWEfficiency";
    private static final String NBT_ROTOR_TYPE =  "RFWRotorType";
    private static final String NBT_ROTOR_DIR = "RFWRotorDir";
    private static final String NBT_CURRENT_ENERGY_GENERATION = "RFWCurrentEnergyGeneration";
    private float currentEnergyGeneration;
    private float oldEnergyGeneration = 0.0f;
    private int rotorType; // -1 if no rotor
    private ForgeDirection rotorDir;
    private EnergyPacket energyPacket = new EnergyPacket();
    private float efficiency;
    private static float windGenerationBase = 40.0f;
    private static int windPacketLength = 4;
    public static final String publicName = "tileEntityWindmillBlock";
    private static final String name = "tileEntityWindmillBlock";

    public TileEntityWindmillBlock() {
        this(0, 0, 0);
    }

    public TileEntityWindmillBlock(float pEfficiency, int pMaximumEnergyTransfer, int pCapacity) {
        storage = new EnergyStorage(pCapacity, pMaximumEnergyTransfer);
        efficiency = pEfficiency;
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
            // Energy left in the packet so utilise it
            if(energyPacket.getLifetime() > 0) {
                extractFromEnergyPacket(energyPacket);
            }
            // No energy left so attempt to generate a packet from the wind
            else {
                energyPacket = getEnergyPacketFromWind();
                extractFromEnergyPacket(energyPacket);
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
        efficiency = pNbt.getFloat(NBT_EFFICIENCY);
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
        pNbt.setFloat(NBT_EFFICIENCY, efficiency);
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
     * Create a new energy packet from the wind.
     * Calculates the energy in the wind that is accessible to the windmill and
     * creates a new energy packet containing that energy. Takes into account
     * the height, weather, and wind tunnel length.
     * @return Energy packet containing energy from the wind.
     */
    private EnergyPacket getEnergyPacketFromWind() {
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
        float energy = windGenerationBase * heightModifier * getTunnelLength() * weatherModifier;
        if(energy < 0.01) {
            return new EnergyPacket(0, 0);
        }
        else {
            return new EnergyPacket(energy, windPacketLength);
        }
    }

    /**
     * Create a new energy packet from hand power.
     * Calculates the energy coming from a player rotating the rotor and creates
     * a new energy packet containing that energy.
     * @return Energy packet containing energy from the player
     */
    private EnergyPacket getEnergyPacketFromHand() {
        return new EnergyPacket(windGenerationBase * ModConfiguration.getHandcrankEnergyMultiplier(), Util.ticksPerClick());
    }

    public void handcrank() {
        if(energyPacket.getLifetime() <= 0) {
            energyPacket = getEnergyPacketFromHand();
        }
    }

    /**
     * Calculate the energy that can be extracted from the energy packet
     * limited by the efficiency of the system. Takes into account the efficiency
     * of the turbine and of the rotor. Does not modify the packet.
     * @param pEnergyPacket Energy packet to calculate from
     * @return Extractable energy in {@code pEnergyPacket} in RF/t
     */
    private float getExtractableEnergyFromPacket(EnergyPacket pEnergyPacket) {
        float totalEfficiency = 1.0f;
        if(!hasRotor()) {
            totalEfficiency = 0.0f;
        }
        else {
            totalEfficiency *= ModConfiguration.getRotorEnergyMultiplier(rotorType);
            totalEfficiency *= efficiency;
        }
        return pEnergyPacket.getEnergyPerTick() * totalEfficiency;
    }

    /**
     * Takes an energy packet and extracts energy from it. Modifies the lifetime
     * of the packet accordingly.
     * @param pEnergyPacket Packet to extract from
     */
    private void extractFromEnergyPacket(EnergyPacket pEnergyPacket){
        currentEnergyGeneration = getExtractableEnergyFromPacket(pEnergyPacket);
        pEnergyPacket.deplete();
        syncEnergy();
        storage.modifyEnergyStored(currentEnergyGeneration);
    }

    /**
     * The current rate of energy production in RF/t
     * @return The current rate of energy production, 0 if no rotor attached
     */
    public float getCurrentEnergyGeneration() {
        return currentEnergyGeneration;
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
