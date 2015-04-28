package com.piepenguin.rfwindmill.tileentities;

import net.minecraft.tileentity.TileEntity;

public class TileEntityRotorBlock extends TileEntity {

    private float rotation = 0.0f;
    private float scale = 0.0f;

    public static String publicName = "tileEntityRotorBlock";

    @Override
    public void updateEntity() {
        if(worldObj.isRemote) {
            rotation += 0.05f;
            scale = 1.0f;
        }
    }

    public float getRotation() {
        return rotation;
    }

    public float getScale() {
        return scale;
    }
}
