package com.piepenguin.rfwindmill.items;

import com.piepenguin.rfwindmill.lib.Constants;
import cpw.mods.fml.common.Loader;
import net.minecraft.item.Item;

public final class ModItems {

    public static Item rotor1;
    public static Item rotor2;
    public static Item rotor3;
    public static Item rotor4;

    public static void init() {
        rotor1 = new RFWItem("rotorIron");
        if(Loader.isModLoaded(Constants.THERMAL_EXPANSION_MOD_ID)) {
            rotor2 = new RFWItem("rotorElectrum");
            rotor3 = new RFWItem("rotorSignalum");
            rotor4 = new RFWItem("rotorEnderium");
        }
        else {
            rotor2 = new RFWItem("rotorGold");
            rotor3 = new RFWItem("rotorNether");
            rotor4 = new RFWItem("rotorDiamond");
        }
    }
}
