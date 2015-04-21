package com.piepenguin.rfwindmill.blocks;

import com.piepenguin.rfwindmill.lib.Constants;
import com.piepenguin.rfwindmill.tileentities.TileEntityWindmillBlock;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

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
        return icons[side];
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityWindmillBlock(maximumEnergyGeneration, maximumEnergyTransfer, capacity);
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }
}
