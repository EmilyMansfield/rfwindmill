package com.piepenguin.rfwindmill.blocks;

import com.piepenguin.rfwindmill.lib.Constants;
import com.piepenguin.rfwindmill.lib.Lang;
import com.piepenguin.rfwindmill.tileentities.TileEntityWindmillBlock;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class WindmillBlock extends Block implements ITileEntityProvider {

    private int maximumEnergyGeneration;
    private int maximumEnergyTransfer;
    private int capacity;

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
//        int direction = MathHelper.floor_double((double)(entityLivingBase.rotationYaw * 4.0f / 360.0f) + 2.5) & 3;
        int direction = MathHelper.floor_double((double) (entityLivingBase.rotationYaw * 4.0f / 360.0f) + 0.50) & 3;

        world.setBlockMetadataWithNotify(x, y, z, direction, 2);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float dx, float dy, float dz) {
        if(!world.isRemote && player.isSneaking()) {
            printChatInfo(world, x, y, z, player);
            return true;
        }
        return false;
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
