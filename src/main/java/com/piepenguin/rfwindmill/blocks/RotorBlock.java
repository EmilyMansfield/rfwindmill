package com.piepenguin.rfwindmill.blocks;

import com.piepenguin.rfwindmill.lib.Constants;
import com.piepenguin.rfwindmill.tileentities.TileEntityRotorBlock;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

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
}
