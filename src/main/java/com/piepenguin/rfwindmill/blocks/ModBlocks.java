package com.piepenguin.rfwindmill.blocks;

import net.minecraft.block.Block;

public class ModBlocks {

    public static Block windmillBlock1;
    public static Block windmillBlock2;
    public static Block windmillBlock3;
    public static Block windmillBlock4;

    public static void init() {
        windmillBlock1 = new WindmillBlock("windmillBlock1", 1, 16000);
        windmillBlock2 = new WindmillBlock("windmillBlock2", 3, 32000);
        windmillBlock3 = new WindmillBlock("windmillBlock3", 6, 48000);
        windmillBlock4 = new WindmillBlock("windmillBlock4", 9, 64000);
    }
}
