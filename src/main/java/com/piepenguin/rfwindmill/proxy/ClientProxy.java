package com.piepenguin.rfwindmill.proxy;

import com.piepenguin.rfwindmill.tileentities.RenderTileEntityRotorBlock;
import com.piepenguin.rfwindmill.tileentities.TileEntityRotorBlock;
import cpw.mods.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {

    public void registerTileEntities() {
        super.registerTileEntities();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRotorBlock.class, new RenderTileEntityRotorBlock());
    }
}
