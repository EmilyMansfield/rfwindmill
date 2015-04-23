package com.piepenguin.rfwindmill.blocks;

import com.piepenguin.rfwindmill.lib.Constants;
import com.piepenguin.rfwindmill.lib.EnergyStorage;
import com.piepenguin.rfwindmill.lib.Lang;
import com.piepenguin.rfwindmill.tileentities.TileEntityWindmillBlock;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class WindmillBlock extends Block implements ITileEntityProvider {

    protected final int maximumEnergyGeneration;
    protected final int maximumEnergyTransfer;
    protected final int capacity;

    private String name;
    private IIcon[] icons = new IIcon[6];

    public WindmillBlock(String pName, int pMaximumEnergyGeneration, int pCapacity) {
        super(Material.rock);
        maximumEnergyGeneration = pMaximumEnergyGeneration;
        maximumEnergyTransfer = pMaximumEnergyGeneration * 8;
        capacity = pCapacity;
        name = pName;
        this.setBlockName(Constants.MODID + "_" + name);
        this.setCreativeTab(CreativeTabs.tabBlock);
        GameRegistry.registerBlock(this, name);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icons[0] = iconRegister.registerIcon(Constants.MODID + ":" + name + "Side");
        icons[1] = iconRegister.registerIcon(Constants.MODID + ":" + name + "Side");
        icons[2] = iconRegister.registerIcon(Constants.MODID + ":" + name + "Front");
        icons[3] = iconRegister.registerIcon(Constants.MODID + ":" + name + "Front");
        icons[4] = iconRegister.registerIcon(Constants.MODID + ":" + name + "Side");
        icons[5] = iconRegister.registerIcon(Constants.MODID + ":" + name + "Side");
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        switch(side) {
            case 0:
                return icons[0];
            case 1:
                return icons[1];
            case 2:
                return icons[(meta == 0 || meta == 2) ? 2 : 4];
            case 3:
                return icons[(meta == 0 || meta == 2) ? 2 : 4];
            case 4:
                return icons[(meta == 1 || meta == 3) ? 2 : 4];
            case 5:
                return icons[(meta == 1 || meta == 3) ? 2 : 4];
            default:
                return icons[0];
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityWindmillBlock(maximumEnergyGeneration, maximumEnergyTransfer, capacity);
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        int direction = MathHelper.floor_double((double) (entityLivingBase.rotationYaw * 4.0f / 360.0f) + 0.50) & 3;
        world.setBlockMetadataWithNotify(x, y, z, direction, 2);
        if(itemStack.stackTagCompound != null) {
            TileEntityWindmillBlock entity = (TileEntityWindmillBlock)world.getTileEntity(x, y, z);
            entity.setEnergyStored(itemStack.stackTagCompound.getInteger(EnergyStorage.NBT_ENERGY));
        }
        super.onBlockPlacedBy(world, x, y, z, entityLivingBase, itemStack);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float dx, float dy, float dz) {
        if(!world.isRemote && player.isSneaking()) {
            printChatInfo(world, x, y, z, player);
            return true;
        }
        return false;
    }

    @Override
    public void onBlockHarvested(World pWorld, int pX, int pY, int pZ, int pSide, EntityPlayer pPlayer) {
        if(!pWorld.isRemote) {
            dismantle(pWorld, pX, pY, pZ);
        }
    }

    private void dismantle(World pWorld, int pX, int pY, int pZ) {
        ItemStack itemStack = new ItemStack(this);

        TileEntityWindmillBlock entity = (TileEntityWindmillBlock)pWorld.getTileEntity(pX, pY, pZ);
        int energy = entity.getEnergyStored();
        if(energy > 0) {
            if(itemStack.getTagCompound() == null) {
                itemStack.setTagCompound(new NBTTagCompound());
            }
            itemStack.getTagCompound().setInteger(EnergyStorage.NBT_ENERGY, energy);
        }
        pWorld.setBlockToAir(pX, pY, pZ);
        EntityItem entityItem = new EntityItem(pWorld, pX + 0.5, pY + 0.5, pZ + 0.5, itemStack);
        entityItem.motionX = 0;
        entityItem.motionZ = 0;
        pWorld.spawnEntityInWorld(entityItem);
    }

    private void printChatInfo(World world, int x, int y, int z, EntityPlayer player) {
        TileEntityWindmillBlock entity = (TileEntityWindmillBlock)world.getTileEntity(x, y, z);
        String msg = String.format("%s: %d/%d RF",
                Lang.localise("energy.stored"),
                entity.getEnergyStored(),
                entity.getMaxEnergyStored());
        player.addChatMessage(new ChatComponentText(msg));
    }
}
