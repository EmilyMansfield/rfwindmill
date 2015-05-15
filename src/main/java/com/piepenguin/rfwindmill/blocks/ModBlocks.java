package com.piepenguin.rfwindmill.blocks;

import com.piepenguin.rfwindmill.lib.ModConfiguration;
import net.minecraft.block.Block;

public class ModBlocks {

    public static Block windmillBlock;
    public static Block rotorBlock1;

    public static void init() {
        int[] energyGeneration = ModConfiguration.getWindmillEnergyGeneration();
        int[] energyStorage = ModConfiguration.getWindmillEnergyStorage();
        windmillBlock = new WindmillBlock("windmillBlock", energyGeneration, energyStorage);
        rotorBlock1 = new RotorBlock();
    }
}
