package com.piepenguin.rfwindmill.blocks;

import com.piepenguin.rfwindmill.items.ModItems;
import com.piepenguin.rfwindmill.lib.Constants;
import com.piepenguin.rfwindmill.lib.EnergyStorage;
import com.piepenguin.rfwindmill.lib.Util;
import com.piepenguin.rfwindmill.tileentities.TileEntityRotorBlock;
import com.piepenguin.rfwindmill.tileentities.TileEntityWindmillBlock;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class RotorBlock extends BlockContainer {

    public RotorBlock() {
        super(Material.iron);
        this.setBlockName(Constants.MODID + "_" + "rotor");
        GameRegistry.registerBlock(this, "rotor");
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World pWorld, int pMeta) {
        return new TileEntityRotorBlock();
    }

    @Override
    public void onBlockHarvested(World pWorld, int pX, int pY, int pZ, int pSide, EntityPlayer pPlayer) {
        // Tell the parent windmill block that it no longer has a rotor
        TileEntityRotorBlock rotorEntity = (TileEntityRotorBlock)pWorld.getTileEntity(pX, pY, pZ);
        ForgeDirection rotorDir = Util.intToDirection(rotorEntity.getBlockMetadata()).getOpposite();
        int parentX = pX + rotorDir.offsetX;
        int parentY = pY + rotorDir.offsetY;
        int parentZ = pZ + rotorDir.offsetZ;
        TileEntityWindmillBlock windmillEntity = (TileEntityWindmillBlock)pWorld.getTileEntity(parentX, parentY, parentZ);
        windmillEntity.setRotor(false);
        // Drop the item
        ItemStack itemStack = new ItemStack(ModItems.rotor1);
        pWorld.setBlockToAir(pX, pY, pZ);
        EntityItem entityItem = new EntityItem(pWorld, pX + 0.5, pY + 0.5, pZ + 0.5, itemStack);
        entityItem.motionX = 0;
        entityItem.motionZ = 0;
        pWorld.spawnEntityInWorld(entityItem);
    }

    public static boolean canPlace(World pWorld, int pX, int pY, int pZ, EntityPlayer pPlayer, ForgeDirection pDir ) {
        // Check if air is free in a 3x3x1 space in the same plane as the rotor
        // This is hideous but I don't know enough Java to fix it :(
        if(pWorld.isAirBlock(pX,pY+1,pZ) && pWorld.isAirBlock(pX,pY,pZ) && pWorld.isAirBlock(pX,pY-1,pZ)) {
            if(pDir == ForgeDirection.NORTH || pDir == ForgeDirection.SOUTH) {
                // Need east/west i.e. xy plane
                if(pWorld.isAirBlock(pX-1,pY+1,pZ) && pWorld.isAirBlock(pX+1,pY+1,pZ) &&
                        pWorld.isAirBlock(pX-1,pY,pZ) && pWorld.isAirBlock(pX+1,pY,pZ) &&
                        pWorld.isAirBlock(pX-1,pY-1,pZ) && pWorld.isAirBlock(pX+1,pY-1,pZ)) {
                    return true;
                }
            }
            else {
                // Need north/south i.e. yz plane
                if(pWorld.isAirBlock(pX,pY+1,pZ-1) && pWorld.isAirBlock(pX,pY+1,pZ+1) &&
                        pWorld.isAirBlock(pX,pY,pZ-1) && pWorld.isAirBlock(pX,pY,pZ+1) &&
                        pWorld.isAirBlock(pX,pY-1,pZ-1) && pWorld.isAirBlock(pX,pY-1,pZ+1)) {
                    return true;
                }
            }
        }
        return false;
    }
}
