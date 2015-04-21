package com.piepenguin.rfwindmill.blocks;

import net.minecraft.block.Block;

public class ModBlocks {

    public static Block windmillBlock1;

    public static void init() {
        windmillBlock1 = new WindmillBlock("windmillBlock1", 1, 16000);
    }
}
