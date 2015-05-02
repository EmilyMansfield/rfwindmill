package com.piepenguin.rfwindmill.tileentities;

import com.piepenguin.rfwindmill.lib.Util;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityRotorBlock extends TileEntity {

    private float rotation = 0.0f;
    private float scale = 0.0f;
    private int texture = 0;
    public static String publicName = "tileEntityRotorBlock";

    @Override
    public void updateEntity() {
        if(worldObj.isRemote) {
            ForgeDirection turbineDir = Util.intToDirection(getBlockMetadata()).getOpposite();
            int parentX = xCoord + turbineDir.offsetX;
            int parentY = yCoord + turbineDir.offsetY;
            int parentZ = zCoord + turbineDir.offsetZ;
            TileEntityWindmillBlock entity = (TileEntityWindmillBlock)worldObj.getTileEntity(parentX, parentY, parentZ);
            if(entity != null) {
                rotation += entity.getCurrentEnergyGeneration() / entity.getMaximumEnergyGeneration();
            }
            scale = 1.0f;
        }
    }

    public float getRotation() {
        return rotation;
    }

    public float getScale() {
        return scale;
    }

    public int getTexture() {
        return texture;
    }

    public void setTexture(int pTexture) {
        texture = pTexture;
    }
}
