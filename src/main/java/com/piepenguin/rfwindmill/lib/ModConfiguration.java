package com.piepenguin.rfwindmill.lib;

import com.google.common.base.Preconditions;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * Handles configuration of the mod via a configuration file, and sets up the
 * default configuration if one doesn't exist.
 */
public class ModConfiguration {

    private static Configuration config;
    private static boolean forceVanillaRecipes;
    private static boolean forceIronRotor;
    private static int[] windmillEnergyGeneration = new int[4];
    private static int[] windmillEnergyStorage = new int[4];
    private static int windmillEnergyTransferMultiplier;
    // Config has no getFloat()
    private static double[] rotorEnergyMultiplier = new double[4];
    private static double weatherMultiplierRain;
    private static double weatherMultiplierThunder;

    public static void init(File pConfigFile) {
        if(pConfigFile != null) {
            config = new Configuration(pConfigFile);
        }
        FMLCommonHandler.instance().bus().register(new ModConfiguration());
        loadConfig();
    }

    /**
     * Load the configuration file or set defaults if one doesn't exist.
     * Mostly handled by Forge.
     */
    private static void loadConfig() {
        Preconditions.checkNotNull(config);
        forceVanillaRecipes = config.get(Configuration.CATEGORY_GENERAL,
                "ForceVanillaRecipes",
                false,
                "Ignore Thermal Expansion and use Vanilla recipes instead").getBoolean();
        forceIronRotor = config.get(Configuration.CATEGORY_GENERAL,
                "ForceIronRotorTexture",
                false,
                "Use the iron rotor texture regardless of the rotor material").getBoolean();
        windmillEnergyGeneration[0] = config.get(Configuration.CATEGORY_GENERAL,
                "WindmillBasicEnergyGeneration",
                1).getInt();
        windmillEnergyGeneration[1] = config.get(Configuration.CATEGORY_GENERAL,
                "WindmillHardenedEnergyGeneration",
                3).getInt();
        windmillEnergyGeneration[2] = config.get(Configuration.CATEGORY_GENERAL,
                "WindmillReinforcedEnergyGeneration",
                6).getInt();
        windmillEnergyGeneration[3] = config.get(Configuration.CATEGORY_GENERAL,
                "WindmillResonantEnergyGeneration",
                9).getInt();
        windmillEnergyStorage[0] = config.get(Configuration.CATEGORY_GENERAL,
                "WindmillBasicEnergyStorage",
                16000).getInt();
        windmillEnergyStorage[1] = config.get(Configuration.CATEGORY_GENERAL,
                "WindmillHardenedEnergyStorage",
                32000).getInt();
        windmillEnergyStorage[2] = config.get(Configuration.CATEGORY_GENERAL,
                "WindmillReinforcedEnergyStorage",
                48000).getInt();
        windmillEnergyStorage[3] = config.get(Configuration.CATEGORY_GENERAL,
                "WindmillResonantEnergyStorage",
                64000).getInt();
        windmillEnergyTransferMultiplier = config.get(Configuration.CATEGORY_GENERAL,
                "WindmillEnergyTransferMultiplier",
                40).getInt();
        rotorEnergyMultiplier[0] = config.get(Configuration.CATEGORY_GENERAL,
                "RotorBasicEnergyMultiplier",
                0.75).getDouble();
        rotorEnergyMultiplier[1] = config.get(Configuration.CATEGORY_GENERAL,
                "RotorHardenedEnergyMultiplier",
                1.00).getDouble();
        rotorEnergyMultiplier[2] = config.get(Configuration.CATEGORY_GENERAL,
                "RotorReinforcedEnergyMultiplier",
                1.25).getDouble();
        rotorEnergyMultiplier[3] = config.get(Configuration.CATEGORY_GENERAL,
                "RotorResonantEnergyMultiplier",
                1.50).getDouble();
        weatherMultiplierRain = config.get(Configuration.CATEGORY_GENERAL,
                "WeatherRainEnergyGenerationMultiplier",
                1.2).getDouble();
        weatherMultiplierThunder = config.get(Configuration.CATEGORY_GENERAL,
                "WeatherThunderEnergyGenerationMultiplier",
                1.5).getDouble();

        if(config.hasChanged()) {
            config.save();
        }
    }

    public static boolean useVanillaRecipes() {
        return forceVanillaRecipes;
    }

    public static boolean useIronRotorTexture() {
        return forceIronRotor;
    }

    public static int[] getWindmillEnergyGeneration() {
        return windmillEnergyGeneration;
    }

    public static int[] getWindmillEnergyStorage() {
        return windmillEnergyStorage;
    }

    public static int getWindmillEnergyTransferMultiplier() {
        return windmillEnergyTransferMultiplier;
    }

    public static float getRotorEnergyMultiplier(int pType) {
        return (float)rotorEnergyMultiplier[pType];
    }

    public static float getWeatherMultiplierRain() {
        return (float)weatherMultiplierRain;
    }

    public static float getWeatherMultiplierThunder() {
        return (float)weatherMultiplierThunder;
    }
}
