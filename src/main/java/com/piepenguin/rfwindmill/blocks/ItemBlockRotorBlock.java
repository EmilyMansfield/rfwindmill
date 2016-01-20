package com.piepenguin.rfwindmill.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockRotorBlock extends ItemBlock {

    public ItemBlockRotorBlock(Block pBlock) {
        super(pBlock);
    }

    @Override
    public String getUnlocalizedName(ItemStack pItemStack) {
        return getUnlocalizedName() + "." + RotorBlock.getName(pItemStack.getItemDamage() >> 2);
    }

    @Override
    public int getMetadata(int pMeta) {
        return pMeta;
    }
}
