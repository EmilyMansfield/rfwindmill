package com.piepenguin.rfwindmill.proxy;

import com.piepenguin.rfwindmill.tileentities.TileEntityRotorBlock;
import com.piepenguin.rfwindmill.tileentities.TileEntityWindmillBlock;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy {

    public void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityWindmillBlock.class, TileEntityWindmillBlock.publicName);
        GameRegistry.registerTileEntity(TileEntityRotorBlock.class, TileEntityRotorBlock.publicName);
    }
}
