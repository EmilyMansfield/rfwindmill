package com.piepenguin.rfwindmill.items;

import com.piepenguin.rfwindmill.lib.Constants;
import cpw.mods.fml.common.Loader;

/**
 * Creates {@link RFWItem} instances of all the items in the mod.
 */
public final class ModItems {

    public static RFWItem rotor1;
    public static RFWItem rotor2;
    public static RFWItem rotor3;
    public static RFWItem rotor4;
    public static RFWItem rotor5;

    /**
     * Creates {@link RFWItem} instances of all the items in the mod.
     */
    public static void init() {
        rotor1 = new RFWItem("rotorIron");
        if(Loader.isModLoaded(Constants.THERMAL_EXPANSION_MOD_ID)) {
            rotor2 = new RFWItem("rotorElectrum");
            rotor3 = new RFWItem("rotorSignalum");
            rotor4 = new RFWItem("rotorEnderium");
        } else {
            rotor2 = new RFWItem("rotorGold");
            rotor3 = new RFWItem("rotorNether");
            rotor4 = new RFWItem("rotorDiamond");
        }
        rotor5 = new RFWItem("rotorCrank");
    }
}
