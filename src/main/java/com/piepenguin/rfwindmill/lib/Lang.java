package com.piepenguin.rfwindmill.lib;

import net.minecraft.util.StatCollector;

public class Lang {

    public static String localise(String str) {
        return StatCollector.translateToLocal("info." + Constants.MODID + "." + str);
    }
}
