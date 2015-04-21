package com.piepenguin.rfwindmill.blocks;

import com.piepenguin.rfwindmill.lib.Constants;
import com.piepenguin.rfwindmill.tileentities.TileEntityWindmillBlock;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class WindmillBlock extends Block implements ITileEntityProvider {

    private int maxmimumEnergyGeneration;
    private int maxmimumEnergyTransfer;
    private int capacity;

    private String name;

    public WindmillBlock(String pName, int pMaximumEnergyGeneration, int pCapacity) {
        super(Material.rock);
        maxmimumEnergyGeneration = pMaximumEnergyGeneration;
        maxmimumEnergyTransfer = pMaximumEnergyGeneration * 8;
        capacity = pCapacity;
        name = pName;
        this.setBlockName(Constants.MODID + "_" + name);
        this.setCreativeTab(CreativeTabs.tabBlock);
        this.setBlockTextureName(Constants.MODID + ":" + name);
        GameRegistry.registerBlock(this, name);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityWindmillBlock(maxmimumEnergyGeneration, maxmimumEnergyTransfer, capacity);
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }
}
