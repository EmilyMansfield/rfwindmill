package com.piepenguin.rfwindmill.lib;

/**
 * <p>Energy production is handled by producing energy packets, which have a total
 * amount of energy and a lifetime. External factors such as wind or the player
 * create packets and send them to the generators. Over the lifetime of the
 * packet (in ticks) the machine will extract energy based on its efficiency.
 * This way the energy source is decoupled from the machine itself, allowing for
 * an easy energy-efficiency-based system and allowing multiple methods of
 * generation can be handled by a single system.</p>
 * <p/>
 * <p>Machines should not accept new energy packets until they have dealt with
 * their current one in general as packet build-up will lead to a long delay
 * after the machine should no longer be receiving power where it is still
 * processing the existing packets. This could be a feature in some situations
 * (such as nuclear reactors or burning fuel) but is unsuitable for renewable
 * sources.</p>
 * <p/>
 * <p>Assuming a machine takes a single packet as described above the packet
 * length can be used to lessen the load on the server, as the external energy
 * production only has to be calculated after a packet has been depleted.</p>
 */
public class EnergyPacket {

    /**
     * Total amount of energy in RF/t in the packet upon creation.
     * Is not depleted over time.
     */
    private float energyStored;
    /**
     * Lifetime of the packet in ticks. Is not depleted over time
     */
    private int lifetime;
    /**
     * Number of ticks of life left. Is decreased every tick.
     */
    private int currentLifetime;

    public EnergyPacket() {
        this(0, 0);
    }

    public EnergyPacket(float pEnergyStored, int pLifetime) {
        energyStored = pEnergyStored;
        lifetime = pLifetime;
        currentLifetime = pLifetime;
    }

    public int getTotalLifetime() {
        return lifetime;
    }

    public int getLifetime() {
        return currentLifetime;
    }

    public float getTotalEnergyStored() {
        return energyStored;
    }

    /**
     * Get the amount of energy in the packet that should be released per tick.
     *
     * @return 0 if the packet is empty, or an even fraction of total energy otherwise
     */
    public float getEnergyPerTick() {
        if(lifetime <= 0) {
            return 0;
        } else {
            return energyStored / lifetime;
        }
    }

    /**
     * Reduce the lifetime of the packet by one tick.
     *
     * @return true if the packet is depleted, false otherwise
     */
    public boolean deplete() {
        if(currentLifetime > 0) {
            --currentLifetime;
            return false;
        } else {
            return true;
        }
    }
}
