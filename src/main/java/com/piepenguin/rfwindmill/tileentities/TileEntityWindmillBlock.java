package com.piepenguin.rfwindmill.tileentities;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import com.piepenguin.rfwindmill.lib.EnergyPacket;
import com.piepenguin.rfwindmill.lib.EnergyStorage;
import com.piepenguin.rfwindmill.lib.ModConfiguration;
import com.piepenguin.rfwindmill.lib.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.BitSet;

/**
 * Tile entity for the {@link com.piepenguin.rfwindmill.blocks.WindmillBlock}
 * class, handles the energy generation, storage, and transfer. Energy produced
 * is dependent on height of the windmill, the rotor material, the free space
 * around the windmill, and the strength of the weather..
 */
public final class TileEntityWindmillBlock extends TileEntity implements IEnergyProvider {

    public static final String publicName = "tileEntityWindmillBlock";
    private static final int tunnelRange = 10;
    private static final String NBT_EFFICIENCY = "RFWEfficiency";
    private static final String NBT_ROTOR_TYPE = "RFWRotorType";
    private static final String NBT_ROTOR_DIR = "RFWRotorDir";
    private static final String NBT_CURRENT_ENERGY_GENERATION = "RFWCurrentEnergyGeneration";
    private static final String NBT_CURRENT_ROTOR_SPEED = "RFWCurrentRotorSpeed";
    private static final String name = "tileEntityWindmillBlock";
    private static int windPacketLength = 4;
    private EnergyStorage storage;
    private float currentEnergyGeneration;
    private float oldEnergyGeneration = 0.0f;
    private float currentRotorSpeed;
    private float oldRotorSpeed = 0.0f;
    private int rotorType; // -1 if no rotor
    private ForgeDirection rotorDir;
    private EnergyPacket energyPacket = new EnergyPacket();
    private float efficiency;
    private boolean queuedCrank = false; // Used to smooth cranking

    private int wheelConfiguration = -1;
    private float wheelSpeed = 0.0f;

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
                updateRotationPerTick();
            } else if(queuedCrank && energyPacket.getLifetime() <= 0) {
                queuedCrank = false;
                energyPacket = getEnergyPacketFromHand();
                extractFromEnergyPacket(energyPacket);
                updateRotationPerTick();
            } else {
                // No energy left so attempt to generate a packet from the wind
                if(hasRotor()) {
                    // No point generating a new packet if it can't be used
                    energyPacket = getEnergyPacketFromWind();
                    extractFromEnergyPacket(energyPacket);
                    updateRotationPerTick();
                } else if(hasCrank()) {
                    // No energy and no queue, so crank should stop
                    currentEnergyGeneration = 0.0f;
                    currentRotorSpeed = 0.0f;
                    syncEnergy();
                    syncSpeed();
                } else if(hasWheel()) {
                    energyPacket = getEnergyPacketFromLiquid(getFlow());
                    extractFromEnergyPacket(energyPacket);
                    updateRotationPerTick();
                }
            }
            if(storage.getEnergyStored() > 0) {
                transferEnergy();
            }
        }
    }

    /**
     * Reads data from {@code pNbt} that must be synced between client and
     * server, namely the current energy generation.
     *
     * @param pNbt NBT to read from
     */
    public void readSyncableDataFromNBT(NBTTagCompound pNbt) {
        currentEnergyGeneration = pNbt.getFloat(NBT_CURRENT_ENERGY_GENERATION);
        currentRotorSpeed = pNbt.getFloat(NBT_CURRENT_ROTOR_SPEED);
    }

    /**
     * Writes data to {@code pNbt} that must be synced between client and
     * server, namely the current energy generation.
     *
     * @param pNbt NBT to write to
     */
    public void writeSyncableDataToNBT(NBTTagCompound pNbt) {
        pNbt.setFloat(NBT_CURRENT_ENERGY_GENERATION, currentEnergyGeneration);
        pNbt.setFloat(NBT_CURRENT_ROTOR_SPEED, currentRotorSpeed);
    }

    /**
     * Read the non-syncable data from {@code pNbt}.
     *
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
     *
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
     * <p/>
     * Equations are based on real-world mechanics of wind turbines. All turbines
     * have a maximum theoretical output ratio of C = 0.59, the Betz limit, but
     * C changes depending on the rotor material and generator efficiency.
     * Modifiers for C are given below.
     * <p/>
     * The total power available to the windmill is given by
     * <p>
     * P = 1/2 &rho; &pi; r^2 v^3 C
     * </p>
     * where &rho; = 1.2 is the air density, r is the radius of the rotor blades,
     * and v is the wind velocity.
     * <p/>
     * Wind velocity depends on height, and follows
     * a power law depending on a reference height z_r and wind speed v_r
     * <p>
     * v = v_r ((z - 64)/z_r)^&alpha;
     * </p>
     * where z is the height above the surface (defined as z = 64), and &alpha;
     * is a coefficient approximately equal to 1/7 over land and 1/10 over open
     * water.
     * TODO: Implement a != 1/7 everywhere
     * The reference value are taken to be z_r = 10, and v_r normally distributed
     * between 0 and 9.4.
     * TODO: Implement normal distribution instead of fixing v_r
     * <p/>
     * Physical power values are in Watts, and calculating the highest possible
     * power output gives approximately 6000W. Conveniently, 300RF/t is an
     * acceptable upper generation bound, giving 1J = 1RF.
     *
     * @return Energy packet containing energy from the wind.
     */
    private EnergyPacket getEnergyPacketFromWind() {
        final float density = 1.2f;
        final float betzLimit = 0.59f;
        final float referenceSpeed = 9.4f;
        final float referenceHeight = 10.0f;
        final float radius = 1.5f;

        double heightModifier = referenceSpeed
                * Math.pow(Math.max(yCoord - 64, 0) / referenceHeight, 1.0 / 7.0);

        /* TODO: Use a more sophisticated weather model? */
        float weatherModifier = 1.0f;
        if(worldObj.isThundering()) {
            weatherModifier = ModConfiguration.getWeatherMultiplierThunder();
        } else if(worldObj.isRaining()) {
            weatherModifier = ModConfiguration.getWeatherMultiplierRain();
        }

        /* RF per second */
        double power = 0.5f * density * betzLimit * radius * radius * Math.PI
                * Math.pow(heightModifier, 3) * getTunnelLength() / (float) tunnelRange
                * weatherModifier;

        if(power < 0.01) {
            return new EnergyPacket(0, 0);
        } else {
            return new EnergyPacket((int) (power / 20.0f * windPacketLength), windPacketLength);
        }
    }

    /**
     * Calculates the number of degrees per tick that the attached rotor should
     * rotate at based on the current wind power.
     */
    public void updateRotationPerTick() {
        final float radius = 1.5f;
        final float width = 0.25f;
        final float referenceSpeed = 9.4f;
        final float referenceHeight = 10.0f;

        if(hasCrank()) {
            currentRotorSpeed = energyPacket.getLifetime() > 0 ? 18.0f : 0.0f;
        } else if(hasRotor()) {
            double heightModifier = referenceSpeed
                    * Math.pow(Math.max(yCoord - 64, 0) / referenceHeight, 1.0 / 7.0);
            double angularVelocity = heightModifier / Math.sqrt(radius * radius + 0.25 * width * width);
            double lengthMultiplier = getTunnelLength() / 10.0;

            currentRotorSpeed = (float) ((lengthMultiplier > 0.0 ? lengthMultiplier : 0.0)
                    * angularVelocity / (40.0 * Math.PI) * 360.0);
        }
        if(hasWheel()) {
            // wheelSpeed is calculated when calculating the energy packet
            // because it depends on the fluid
            currentRotorSpeed = 3.0f * 1.9f * wheelSpeed / 60.0f * 360.0f / 20.0f;
        } else {
            currentRotorSpeed = 0.0f;
        }
    }

    /**
     * The current rotor angular velocity in degrees per tick
     *
     * @return Angular velocity of the rotor
     */
    public float getCurrentRotorSpeed() {
        return currentRotorSpeed;
    }

    /**
     * Create a new energy packet from hand power.
     * Calculates the energy coming from a player rotating the rotor and creates
     * a new energy packet containing that energy.
     *
     * @return Energy packet containing energy from the player
     */
    private EnergyPacket getEnergyPacketFromHand() {
        return new EnergyPacket(
                Util.ticksPerClick() * ModConfiguration.getHandcrankPower(),
                Util.ticksPerClick());
    }

    public void handcrank() {
        if(energyPacket.getLifetime() <= 0) {
            energyPacket = getEnergyPacketFromHand();
        } else {
            queuedCrank = true;
        }
    }

    /**
     * Calculate the energy that can be extracted from the energy packet
     * limited by the efficiency of the system. Takes into account the efficiency
     * of the turbine and of the rotor. Does not modify the packet.
     *
     * @param pEnergyPacket Energy packet to calculate from
     * @return Extractable energy in {@code pEnergyPacket} in RF/t
     */
    private float getExtractableEnergyFromPacket(EnergyPacket pEnergyPacket) {
        float totalEfficiency = 1.0f;
        if(hasRotor()) {
            totalEfficiency *= ModConfiguration.getRotorEfficiency(rotorType);
            totalEfficiency *= efficiency;
        } else if(hasCrank() && getCurrentRotorSpeed() > 0.01) {
            // Be slightly more forgiving with efficiency, so that the low
            // tier machines generate a usable amount of RF/t but the high tier
            // machines don't generate unrealistic amounts
            totalEfficiency *= (efficiency + 0.9f) / 2.0f;
        } else if(hasWheel()) {
            totalEfficiency *= 0.5f;
        } else {
            totalEfficiency = 0.0f;
        }
        return pEnergyPacket.getEnergyPerTick() * totalEfficiency;
    }

    /**
     * Takes an energy packet and extracts energy from it. Modifies the lifetime
     * of the packet accordingly.
     *
     * @param pEnergyPacket Packet to extract from
     */
    private void extractFromEnergyPacket(EnergyPacket pEnergyPacket) {
        currentEnergyGeneration = getExtractableEnergyFromPacket(pEnergyPacket);
        pEnergyPacket.deplete();
        syncEnergy();
        syncSpeed();
        storage.modifyEnergyStored(currentEnergyGeneration);
    }

    /**
     * The current rate of energy production in RF/t
     *
     * @return The current rate of energy production, 0 if no rotor attached
     */
    public float getCurrentEnergyGeneration() {
        return currentEnergyGeneration;
    }

    /**
     * Sync the energy generation rate between client and server
     */
    private void syncEnergy() {
        // Amount of energy generated has changed so sync with server
        if(Math.abs(currentEnergyGeneration - oldEnergyGeneration) > 0.01) {
            oldEnergyGeneration = currentEnergyGeneration;
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            markDirty();
        }
    }

    /**
     * Sync the rotor speed between client and server. (Used in rendering with
     * {@link RenderTileEntityRotorBlock}.)
     */
    private void syncSpeed() {
        if(Math.abs(currentRotorSpeed - oldRotorSpeed) > 0.01) {
            oldRotorSpeed = currentRotorSpeed;
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            markDirty();
        }
    }

    /**
     * Returns the number of free air blocks in a 1x1x{@code tunnelRange} up to
     * the first non-air block in the given direction and starting at the
     * given coordinate. Gives the length of a 'wind-tunnel' until an obstruction
     * is found.
     *
     * @param pX         X coordinate to start measuring from
     * @param pY         Y coordinate to start measuring from
     * @param pZ         Z coordinate to start measuring from
     * @param pDirection Direction to check the tunnel in
     * @param toSkip     Number of blocks in {@code pDirection} to ignore
     * @return Length of the 'wind-tunnel'
     */
    private int getTunnelLengthSingleBlock(int pX, int pY, int pZ, ForgeDirection pDirection, int toSkip) {
        for(int i = toSkip; i < tunnelRange; ++i) {
            // Skip specified number of blocks
            int dx = pX + pDirection.offsetX * i;
            int dy = pY + pDirection.offsetY * i;
            int dz = pZ + pDirection.offsetZ * i;
            Block block = worldObj.getBlock(dx, dy, dz);
            if(block == null || (block.getMaterial() != Material.air)) {
                return i - 1;
            }
        }

        return tunnelRange;
    }

    /**
     * Gets the length of a 'wind-tunnel' as per
     * {@link #getTunnelLengthSingleBlock(int, int, int, ForgeDirection, int)}
     * but tunnel length is measured separately in two opposite directions and
     * the shortest length taken to be the length of the wind tunnel. The first
     * block is ignored in the given {@code pDirection} so as to ignore the
     * {@link com.piepenguin.rfwindmill.blocks.RotorBlock}.
     *
     * @param pX         X coordinate to start measuring from
     * @param pY         Y coordinate to start measuring from
     * @param pZ         Z coordinate to start measuring from
     * @param pDirection Direction the rotor is facing
     * @param isCenter   {@code true} if the tunnel starts on the windmill center
     * @return Shortest unobstructed length of the two opposing 'wind-tunnel's
     */
    private int getTunnelLengthTwoSided(int pX, int pY, int pZ, ForgeDirection pDirection, boolean isCenter) {
        if(isCenter) {
            return getTunnelLengthSingleBlock(pX, pY, pZ, pDirection, 2);
        } else {
            return Math.min(
                    getTunnelLengthSingleBlock(pX, pY, pZ, pDirection, 1),
                    getTunnelLengthSingleBlock(pX, pY, pZ, pDirection.getOpposite(), 0)
            );
        }
    }

    /**
     * Get the length of a 3x3 tunnel in the same plane as the rotor as per
     * {@link #getTunnelLengthTwoSided(int, int, int, ForgeDirection, boolean)}
     * but take the shortest unobstructed range in the 3x3x{@code tunnelRange}
     * to be the length of the tunnel.
     *
     * @return Length of the 'wind-tunnel'
     */
    private int getTunnelLength() {
        int range = tunnelRange;
        // If rotor dir is north or south then check xy plane
        if(rotorDir == ForgeDirection.NORTH || rotorDir == ForgeDirection.SOUTH) {
            for(int x = -1; x <= 1; ++x) {
                for(int y = -1; y <= 1; ++y) {
                    int r = getTunnelLengthTwoSided(xCoord + x, yCoord + y, zCoord, rotorDir, x == 0);
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
                    int r = getTunnelLengthTwoSided(xCoord, yCoord + y, zCoord + z, rotorDir, z == 0);
                    if(r < range) {
                        range = r;
                    }
                }
            }
        }
        return range;
    }

    public class FluidFlow {
        public BitSet pattern = new BitSet(5 * 5);
        public float speed = 0.0f;
        public Fluid type = null;

        // Get and set commands which use indexing consistent with that
        // of a binary number defined from left to right, ie
        // 0b00000_00010 <==> set(1, 3)
        void set(int du, int dv) {
            pattern.set(5 * (dv + 2) + (du + 2));
        }

        boolean get(int du, int dv) {
            return pattern.get(5 * (dv + 2) + (du + 2));
        }
    }

    /**
     * Get the 5x5 flow pattern
     *
     * @param pXY
     * @return
     */
    private FluidFlow getFlowPatternPlane(boolean pXY) {
        FluidFlow flow = new FluidFlow();

        // du and dv are plane coordinates
        for(int du = -2; du <= 2; ++du) {
            for(int dv = 2; dv >= -2; --dv) {
                // Map plane coordinates to space coordinates depending on
                // plane orientation. Note that dy := dv always, and that the
                // dv loop goes from high to low. This means the search space
                // is traversed from top to bottom and negative to positive.
                int dx = (pXY ? du : 0) + rotorDir.offsetX;
                int dy = dv + rotorDir.offsetY;
                int dz = (pXY ? 0 : du) + rotorDir.offsetY;

                Block b = worldObj.getBlock(xCoord + dx, yCoord + dy, zCoord + dz);
                Fluid f = FluidRegistry.lookupFluidForBlock(b);
                if(f != null) {
                    if(flow.type != null) {
                        if(f == flow.type) flow.set(du, dv);
//                        else return null;
                    } else {
                        flow.type = f;
                        // According to the wiki, flowing water pushes mobs at
                        // a speed of 1.39m/s and has a viscosity of 1000. Lava
                        // has a viscosity of 6000, so it seems reasonable to
                        // calculate the fluid speed as 1390/viscosity
                        flow.speed = 1390.0f / f.getViscosity();
                        flow.set(du, dv);
                    }
                }
            }
        }

        return flow;
    }

    private FluidFlow getFlow() {
        return getFlowPatternPlane(rotorDir == ForgeDirection.NORTH || rotorDir == ForgeDirection.SOUTH);
    }

    private EnergyPacket getEnergyPacketFromLiquid(FluidFlow flow) {
        if(flow == null) return new EnergyPacket(0, 0);

        // Basic idea is to XOR the flow with each of the sample flows and then
        // check then number of true bits. cardinality(f ^ sample)
        // is close to 0 if the flows agree, and large otherwise.
        // For simplicity, let 'close to 0' mean < 3.
        final BitSet[] sampleFlows = {
                // Undershot
                BitSet.valueOf(new long[]{0b00000_00000_00000_00000_11111}),
                // Overshot right
                BitSet.valueOf(new long[]{0b00001_00001_00001_00001_00001}),
                // Overshot left
                BitSet.valueOf(new long[]{0b10000_10000_10000_10000_10000}),
                // Overshot carry right
                BitSet.valueOf(new long[]{0b00001_00001_00001_10000_11111}),
                // Overshot carry left
                BitSet.valueOf(new long[]{0b10000_10000_10000_10000_11111}),
                // Breastshot right
                BitSet.valueOf(new long[]{0b00000_00000_00001_00111_11111}),
                // Breastshot left
                BitSet.valueOf(new long[]{0b00000_00000_10000_11100_11111})
        };

        // Find a suitable sample and calculate energy. This could be done with
        // a simple iterator and lambda combination, but Java7 is cripplingly
        // unprepared for functional programming, so we'll do this the old
        // fashioned procedural way.
        // TODO: Rewrite entire mod in Scala. Or maybe just rewrite some in Kotlin.
        for(int i = 0; i < sampleFlows.length; ++i) {
            BitSet tmp = (BitSet) flow.pattern.clone();
            tmp.xor(sampleFlows[i]); // No chaining, methods edit in place...
            if(tmp.cardinality() < 3) {
                wheelConfiguration = i;
                wheelSpeed = flow.speed;
                switch(i) {
                    case 0:
                        // Undershot
                        // Uses KE of water with low efficiency. KE is
                        // 0.5mv^2 with m=Vv so E=0.5Vv^3
                        // Efficiency is 70% (see Müller)
                        return new EnergyPacket(
                                0.7f * 0.5f * 1.0f * (float) Math.pow(flow.speed, 3.0) * windPacketLength,
                                windPacketLength);
                    case 1:
                        // Overshot right
                        // Uses only falling water so low energy, but high
                        // efficiency. Head is 5m. One block travels 18m in 5s
                        // so flow rate is 3.6m^3/s. For a bucket capacity of V
                        // total power is 5*3.6*V*9.8/20 RF/t.
                        // Assume buckets hold a litre, for balance.
                        // Efficiency for overshot is 85% (see Müller)
                        return new EnergyPacket(
                                0.85f * 5.0f * 3.6f * 1.0f * 9.8f / 20.0f * windPacketLength,
                                windPacketLength);
                    case 2:
                        // Overshot left
                        return new EnergyPacket(
                                0.85f * 5.0f * 3.6f * 1.0f * 9.8f / 20.0f * windPacketLength,
                                windPacketLength);
                    case 3:
                        // Overshot carry right
                        // Uses falling water and and a small amount of KE
                        return new EnergyPacket(
                                0.85f * 5.0f * 3.6f * 1.0f * 9.8f / 20.0f * windPacketLength
                                        + 0.1f * 0.5f * 1.0f * (float) Math.pow(flow.speed, 3.0) * windPacketLength,
                                windPacketLength);
                    case 4:
                        // Overshot carry left
                        return new EnergyPacket(
                                0.85f * 5.0f * 3.6f * 1.0f * 9.8f / 20.0f
                                        + 0.1f * 0.5f * 1.0f * (float) Math.pow(flow.speed, 3.0) * windPacketLength,
                                windPacketLength);
                    case 5:
                        // Breastshot right
                        // Similar to overshot carry but with a smaller head
                        // height, more KE, and slightly reduced efficiency
                        return new EnergyPacket(
                                0.80f * 3.0f * 3.6f * 1.0f * 9.8f / 20.0f * windPacketLength
                                        + 0.35f * 0.5f * 1.0f * (float) Math.pow(flow.speed, 3.0) * windPacketLength,
                                windPacketLength);
                    case 6:
                        // Breastshot left
                        return new EnergyPacket(
                                0.80f * 3.0f * 3.6f * 1.0f * 9.8f / 20.0f * windPacketLength
                                        + 0.35f * 0.5f * 1.0f * (float) Math.pow(flow.speed, 3.0) * windPacketLength,
                                windPacketLength);
                }
            }
        }
        // Invalid configuration so don't produce any energy
        wheelConfiguration = -1;
        wheelSpeed = 0.0f;
        return new EnergyPacket(0, 0);
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
                IEnergyReceiver receiver = (IEnergyReceiver) tile;
                extractEnergy(direction.getOpposite(), receiver.receiveEnergy(direction.getOpposite(), storage.getExtract(), false), false);
            }
        }
    }

    @Override
    public int extractEnergy(ForgeDirection pFrom, int pMaxExtract, boolean pSimulate) {
        if(canConnectEnergy(pFrom)) {
            return storage.extractEnergy(pMaxExtract, pSimulate);
        } else {
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
        return rotorType >= 0 && rotorType < 4;
    }

    public boolean hasCrank() {
        return rotorType == 4;
    }

    public boolean hasWheel() {
        return rotorType > 4;
    }

    public boolean hasAttachment() {
        return hasRotor() || hasCrank() || hasWheel();
    }

    public String getWheelConfiguration() {
        switch(wheelConfiguration) {
            default:
            case -1:
                return "";
            case 0:
                return "(Undershot)";
            case 1:
            case 2:
            case 3:
            case 4:
                return "(Overshot)";
            case 5:
            case 6:
                return "(Breastshot)";
        }
    }

    /**
     * Set the tier of the rotor connected to the corresponding
     * {@link com.piepenguin.rfwindmill.blocks.WindmillBlock}. -1 if no rotor is
     * connected.
     *
     * @param pRotorType Tier of the rotor being attached. -1 if no rotor
     * @param fDir       Direction the rotor is facing, i.e. the normal to the face
     *                   the rotor is being placed on
     */
    public void setRotor(int pRotorType, ForgeDirection fDir) {
        rotorType = pRotorType;
        rotorDir = fDir;
    }

    public ForgeDirection getRotorDir() {
        return rotorDir;
    }
}
