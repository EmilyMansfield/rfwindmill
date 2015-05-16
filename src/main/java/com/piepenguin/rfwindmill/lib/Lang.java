package com.piepenguin.rfwindmill.lib;

import net.minecraft.util.StatCollector;

/**
 * Contains localisation functions.
 */
public class Lang {

    /**
     * Localise the given info string. Used for info strings only, not block
     * names or textures.
     * @param pText identifier for the info string
     * @return Localised version of the string
     */
    public static String localise(String pText) {
        return StatCollector.translateToLocal("info." + Constants.MODID + "." + pText);
    }
}
