package com.piepenguin.rfwindmill.tileentities;

import com.piepenguin.rfwindmill.lib.Util;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityRotorBlock extends TileEntity {

    private float rotation = 0.0f;
    private float scale = 0.0f;
    private static final String NBT_TEXTURE = "RFWRotorTexture";
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

    public void readSyncableDataFromNBT(NBTTagCompound pNbt) {
        texture = pNbt.getInteger(NBT_TEXTURE);
    }

    public void writeSyncableDataToNBT(NBTTagCompound pNbt) {
        pNbt.setInteger(NBT_TEXTURE, texture);
    }

    @Override
    public void readFromNBT(NBTTagCompound pNbt) {
        super.readFromNBT(pNbt);
        readSyncableDataFromNBT(pNbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound pNbt) {
        super.writeToNBT(pNbt);
        writeSyncableDataToNBT(pNbt);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound syncData = new NBTTagCompound();
        writeSyncableDataToNBT(syncData);

        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, syncData);
    }

    @Override
    public void onDataPacket(NetworkManager pNet, S35PacketUpdateTileEntity pPacket) {
        readSyncableDataFromNBT(pPacket.func_148857_g());
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
