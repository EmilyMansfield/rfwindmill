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
        int[] energyGeneration = ModConfiguration.getWindmillEnergyGeneration();
        int[] energyStorage = ModConfiguration.getWindmillEnergyStorage();
        windmillBlock = new WindmillBlock("windmillBlock", energyGeneration, energyStorage);
        rotorBlock1 = new RotorBlock();
    }
}
