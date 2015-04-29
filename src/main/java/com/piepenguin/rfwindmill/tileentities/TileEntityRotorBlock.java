package com.piepenguin.rfwindmill.tileentities;

import com.piepenguin.rfwindmill.blocks.RotorBlock;
import com.piepenguin.rfwindmill.blocks.WindmillBlock;
import com.piepenguin.rfwindmill.lib.Util;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityRotorBlock extends TileEntity {

    private float rotation = 0.0f;
    private float scale = 0.0f;
    public static String publicName = "tileEntityRotorBlock";

    @Override
    public void updateEntity() {
        if(worldObj.isRemote) {
            ForgeDirection rotorDir = Util.intToDirection(getBlockMetadata()).getOpposite();
            int parentX = xCoord + rotorDir.offsetX;
            int parentY = yCoord + rotorDir.offsetY;
            int parentZ = zCoord + rotorDir.offsetZ;
            TileEntityWindmillBlock entity = (TileEntityWindmillBlock)worldObj.getTileEntity(parentX, parentY, parentZ);
            if(entity != null && entity.getEnergyGeneration() > 1e-3) {
                rotation += entity.getEnergyGeneration() / entity.getMaximumEnergyGeneration();
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
}
