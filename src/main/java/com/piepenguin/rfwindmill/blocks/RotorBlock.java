package com.piepenguin.rfwindmill.blocks;

import com.piepenguin.rfwindmill.lib.Constants;
import com.piepenguin.rfwindmill.lib.Util;
import com.piepenguin.rfwindmill.tileentities.TileEntityRotorBlock;
import com.piepenguin.rfwindmill.tileentities.TileEntityWindmillBlock;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Rotor blocks are created when a {@link WindmillBlock} is right clicked with
 * a rotor item, and are necessary for the {@link WindmillBlock} to produce RF.
 * Each {@link RotorBlock} does not deal with energy generation directly.
 * The lowest 2 bits of the metadata store the direction that the rotor is
 * facing and the highest 2 store the texture id.
 */
public class RotorBlock extends Block implements ITileEntityProvider {

    private IIcon[] icons = new IIcon[2];

    public RotorBlock() {
        super(Material.iron);
        setStepSound(Block.soundTypeMetal);
        this.setBlockName(Constants.MODID + "_" + "rotor");
        GameRegistry.registerBlock(this, ItemBlockRotorBlock.class, "rotor");
    }

    /**
     * Return a string equal the name of the general version of the rotor.
     * 0 for a rotor, 1 for a crank.
     *
     * @param type Either 0 or 1
     * @return Either "rotor" or "crank"
     */
    public static String getName(int type) {
        return type == 0 ? "rotor" : "crank";
    }

    @Override
    public void registerBlockIcons(IIconRegister pIconRegister) {
        for(int i = 0; i < 2; ++i) {
            icons[i] = pIconRegister.registerIcon(Constants.MODID + ":" + getName(i));
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int pSide, int pMeta) {
        return icons[pMeta >> 2];
    }

    @Override
    public int damageDropped(int pMeta) {
        return pMeta;
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
        TileEntityRotorBlock rotorEntity = (TileEntityRotorBlock) pWorld.getTileEntity(pX, pY, pZ);
        if(rotorEntity != null) {
            ForgeDirection rotorDir = Util.intToDirection(rotorEntity.getBlockMetadata() & 3).getOpposite();
            int parentX = pX + rotorDir.offsetX;
            int parentY = pY + rotorDir.offsetY;
            int parentZ = pZ + rotorDir.offsetZ;
            TileEntityWindmillBlock windmillEntity = (TileEntityWindmillBlock) pWorld.getTileEntity(parentX, parentY, parentZ);
            if(windmillEntity != null) {
                windmillEntity.setRotor(-1, ForgeDirection.NORTH);
            }
        }
        // Dismantle the rotor
        dismantle(pWorld, pX, pY, pZ);
    }

    @Override
    public boolean onBlockActivated(World pWorld, int pX, int pY, int pZ, EntityPlayer pPlayer, int pSide, float pDx, float pDy, float pDz) {
        // Cannot be activated by a block activator, must be a player
        if(pPlayer == null || pPlayer instanceof FakePlayer) {
            return false;
        }
        TileEntityRotorBlock rotorEntity = (TileEntityRotorBlock) pWorld.getTileEntity(pX, pY, pZ);
        if(rotorEntity != null) {
            ForgeDirection rotorDir = Util.intToDirection(rotorEntity.getBlockMetadata() & 3).getOpposite();
            int parentX = pX + rotorDir.offsetX;
            int parentY = pY + rotorDir.offsetY;
            int parentZ = pZ + rotorDir.offsetZ;
            TileEntityWindmillBlock windmillEntity = (TileEntityWindmillBlock) pWorld.getTileEntity(parentX, parentY, parentZ);
            if(windmillEntity != null) {
                // Cannot crank if no rotor attached
                if(windmillEntity.hasCrank()) {
                    windmillEntity.handcrank();
                }
            }
        }
        return true;
    }

    /**
     * Remove the block from the world and drop the corresponding rotor as an
     * item. Does not notify the parent {@link WindmillBlock} of any changes.
     *
     * @param pWorld Minecraft {@link World}
     * @param pX     X coordinate of the block
     * @param pY     Y coordinate of the block
     * @param pZ     Z coordinate of the block
     */
    public void dismantle(World pWorld, int pX, int pY, int pZ) {
        TileEntityRotorBlock entity = (TileEntityRotorBlock) pWorld.getTileEntity(pX, pY, pZ);
        ItemStack itemStack = new ItemStack(entity.getRotorItem(), 1, entity.getBlockMetadata() >> 2);
        // Delete the block
        pWorld.setBlockToAir(pX, pY, pZ);
        // Drop the item
        EntityItem entityItem = new EntityItem(pWorld, pX + 0.5, pY + 0.5, pZ + 0.5, itemStack);
        entityItem.motionX = 0;
        entityItem.motionZ = 0;
        pWorld.spawnEntityInWorld(entityItem);
    }

    /**
     * Checks the area around the specified position for a 3x3x1 plane of free
     * blocks in the plane normal to the specified direction, which corresponds
     * to the bounding box of the rotor when its rendered.
     *
     * @param pWorld  Minecraft {@link World}
     * @param pX      X coordinate where the block is trying to be placed
     * @param pY      Y coordinate where the block is trying to be placed
     * @param pZ      Z coordinate where the block is trying to be placed
     * @param pPlayer Player trying to place the block
     * @param pDir    Direction the rotor should be facing in
     * @return {@code true} if the rotor can be placed, and {@code false}
     * otherwise
     */
    public static boolean canPlace(World pWorld, int pX, int pY, int pZ, EntityPlayer pPlayer, ForgeDirection pDir) {
        // Check if air is free in a 3x3x1 space in the same plane as the rotor
        // This is hideous but I don't know enough Java to fix it :(
        if(pWorld.isAirBlock(pX, pY + 1, pZ) && pWorld.isAirBlock(pX, pY, pZ) && pWorld.isAirBlock(pX, pY - 1, pZ)) {
            if(pDir == ForgeDirection.NORTH || pDir == ForgeDirection.SOUTH) {
                // Need east/west i.e. xy plane
                if(pWorld.isAirBlock(pX - 1, pY + 1, pZ) && pWorld.isAirBlock(pX + 1, pY + 1, pZ) &&
                        pWorld.isAirBlock(pX - 1, pY, pZ) && pWorld.isAirBlock(pX + 1, pY, pZ) &&
                        pWorld.isAirBlock(pX - 1, pY - 1, pZ) && pWorld.isAirBlock(pX + 1, pY - 1, pZ)) {
                    return true;
                }
            } else {
                // Need north/south i.e. yz plane
                if(pWorld.isAirBlock(pX, pY + 1, pZ - 1) && pWorld.isAirBlock(pX, pY + 1, pZ + 1) &&
                        pWorld.isAirBlock(pX, pY, pZ - 1) && pWorld.isAirBlock(pX, pY, pZ + 1) &&
                        pWorld.isAirBlock(pX, pY - 1, pZ - 1) && pWorld.isAirBlock(pX, pY - 1, pZ + 1)) {
                    return true;
                }
            }
        }
        return false;
    }
}
