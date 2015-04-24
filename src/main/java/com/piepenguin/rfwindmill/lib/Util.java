package com.piepenguin.rfwindmill.lib;

import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.common.ModAPIManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class Util {

    public static boolean hasWrench(EntityPlayer pPlayer, int pX, int pY, int pZ) {
        ItemStack tool = pPlayer.getCurrentEquippedItem();
        if(tool != null) {
            return (ModAPIManager.INSTANCE.hasAPI("BuildCraftAPI|tools") &&
                    (tool.getItem() instanceof IToolWrench) &&
                    ((IToolWrench)tool.getItem()).canWrench(pPlayer, pX, pY, pZ));

        }
        return false;
    }
}
