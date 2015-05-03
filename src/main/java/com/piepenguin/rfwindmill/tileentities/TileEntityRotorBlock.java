package com.piepenguin.rfwindmill.tileentities;

import com.piepenguin.rfwindmill.items.ModItems;
import com.piepenguin.rfwindmill.items.RFWItem;
import com.piepenguin.rfwindmill.lib.ModConfiguration;
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
    private static final String NBT_ROTOR_TYPE = "RFWRotorType";
    private int type = 0;
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
        type = pNbt.getInteger(NBT_ROTOR_TYPE);
    }

    public void writeSyncableDataToNBT(NBTTagCompound pNbt) {
        pNbt.setInteger(NBT_ROTOR_TYPE, type);
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

    public int getType() {
        return type;
    }

    public void setType(int pType) {
        type = pType;
    }

    public int getTexture() {
        if(ModConfiguration.useIronRotorTexture()) {
            return 0;
        }
        switch(type) {
            default:
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return Util.useThermalExpansion() ? 2 : 4;
            case 3:
                return Util.useThermalExpansion() ? 3 : 5;
        }
    }

    public RFWItem getRotorItem() {
        switch(type) {
            default:
            case 0:
                return ModItems.rotor1;
            case 1:
                return ModItems.rotor2;
            case 2:
                return ModItems.rotor3;
            case 3:
                return ModItems.rotor4;
        }
    }
}
