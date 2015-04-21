package com.piepenguin.rfwindmill.blocks;

import com.piepenguin.rfwindmill.lib.Constants;
import com.piepenguin.rfwindmill.tileentities.TileEntityWindmillBlock1;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class WindmillBlock1 extends Block implements ITileEntityProvider {

    private String name = "windmillBlock1";

    public WindmillBlock1() {
        super(Material.rock);
        this.setBlockName(Constants.MODID + "_" + name);
        this.setCreativeTab(CreativeTabs.tabBlock);
        this.setBlockTextureName(Constants.MODID + ":" + name);
        GameRegistry.registerBlock(this, name);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityWindmillBlock1();
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }
}
