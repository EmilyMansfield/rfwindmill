package com.piepenguin.rfwindmill.lib;

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

    public EnergyPacket(float pEnergyStored, int pLifetime) {
        energyStored = pEnergyStored;
        lifetime = pLifetime;
        currentLifetime = pLifetime;
    }

    public int getTotalLifetime() {
        return lifetime;
    }

    public float getTotalEnergyStored() {
        return energyStored;
    }

    public float getEnergyPerTick() {
        if(lifetime <= 0) {
            return 0;
        }
        else {
            return energyStored / lifetime;
        }
    }

    /**
     * Reduce the lifetime of the packet by one tick.
     * @return true if the packet is depleted, false otherwise
     */
    public boolean deplete() {
        if(currentLifetime > 0) {
            --currentLifetime;
            return false;
        }
        else {
            return true;
        }
    }
}
