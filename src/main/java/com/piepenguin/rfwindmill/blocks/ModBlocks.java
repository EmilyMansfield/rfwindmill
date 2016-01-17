package com.piepenguin.rfwindmill.blocks;

import com.piepenguin.rfwindmill.lib.ModConfiguration;
import net.minecraft.block.Block;

/**
 * Creates {@link Block} instances of all the blocks in the mod.
 */
public class ModBlocks {

    public static Block windmillBlock;
    public static Block rotorBlock1;

    /**
     * Create {@link Block} instances of all the blocks in the mod.
     */
    public static void init() {
        float[] efficiency = new float[4];
        for(int i = 0; i < efficiency.length; ++i) {
            efficiency[i] = ModConfiguration.getWindmillEfficiency(i);
        }
        int[] energyStorage = ModConfiguration.getWindmillEnergyStorage();
        windmillBlock = new WindmillBlock("windmillBlock", efficiency, energyStorage);
        rotorBlock1 = new RotorBlock();
    }
}
