package com.piepenguin.rfwindmill.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/**
 * ItemBlock version of {@link WindmillBlock}, used when the block
 * is stored in an inventory.
 */
public class ItemBlockWindmillBlock extends ItemBlock {

    public ItemBlockWindmillBlock(Block pBlock) {
        super(pBlock);
        setHasSubtypes(true);
    }

    @Override
    public String getUnlocalizedName(ItemStack pItemStack) {
        String name;
        switch(pItemStack.getItemDamage()) {
            default:
            case 0:
                name = "basic";
                break;
            case 1:
                name = "hardened";
                break;
            case 2:
                name = "reinforced";
                break;
            case 3:
                name = "resonant";
                break;
        }
        return getUnlocalizedName() + "." + name;
    }

    @Override
    public int getMetadata(int pMeta) {
        return pMeta;
    }
}
