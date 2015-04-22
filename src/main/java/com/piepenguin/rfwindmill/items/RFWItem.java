package com.piepenguin.rfwindmill.items;

import com.piepenguin.rfwindmill.lib.Constants;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class RFWItem extends Item {

    private String name;

    public RFWItem(String pName) {
        name = pName;
        setUnlocalizedName(Constants.MODID + "_" + name);
        setCreativeTab(CreativeTabs.tabMaterials);
        setTextureName(Constants.MODID + ":" + name);
        GameRegistry.registerItem(this, name);
    }
}
