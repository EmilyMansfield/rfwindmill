package com.piepenguin.rfwindmill.proxy;

import com.piepenguin.rfwindmill.tileentities.TileEntityWindmillBlock1;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy {

    public void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityWindmillBlock1.class, TileEntityWindmillBlock1.publicName);
    }
}
