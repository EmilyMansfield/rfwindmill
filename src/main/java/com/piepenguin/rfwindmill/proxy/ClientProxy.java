package com.piepenguin.rfwindmill.proxy;

import com.piepenguin.rfwindmill.tileentities.RenderTileEntityRotorBlock;
import com.piepenguin.rfwindmill.tileentities.TileEntityRotorBlock;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

public class ClientProxy extends CommonProxy {

    public void registerTileEntities() {
        super.registerTileEntities();
        GameRegistry.registerTileEntity(TileEntityRotorBlock.class, TileEntityRotorBlock.publicName);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRotorBlock.class, new RenderTileEntityRotorBlock());
    }
}
