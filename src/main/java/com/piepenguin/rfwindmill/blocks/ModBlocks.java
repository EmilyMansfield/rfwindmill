package com.piepenguin.rfwindmill.blocks;

import com.piepenguin.rfwindmill.lib.ModConfiguration;
import net.minecraft.block.Block;

public class ModBlocks {

    public static Block windmillBlock1;
    public static Block windmillBlock2;
    public static Block windmillBlock3;
    public static Block windmillBlock4;

    public static void init() {
        int[] energyGeneration = ModConfiguration.getWindmillEnergyGeneration();
        int[] energyStorage = ModConfiguration.getWindmillEnergyStorage();
        windmillBlock1 = new WindmillBlock("windmillBlock1", energyGeneration[0], energyStorage[0]);
        windmillBlock2 = new WindmillBlock("windmillBlock2", energyGeneration[1], energyStorage[1]);
        windmillBlock3 = new WindmillBlock("windmillBlock3", energyGeneration[2], energyStorage[2]);
        windmillBlock4 = new WindmillBlock("windmillBlock4", energyGeneration[3], energyStorage[3]);
    }
}
