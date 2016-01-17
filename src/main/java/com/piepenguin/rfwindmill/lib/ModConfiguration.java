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
    private static double[] windmillEfficiency = new double[4];
    private static int[] windmillEnergyStorage = new int[4];
    private static double windmillEnergyTransferMultiplier;
    private static double[] rotorEfficiency = new double[4];
    private static double weatherMultiplierRain;
    private static double weatherMultiplierThunder;
    private static double handcrankPower;

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

        /* Recipes */
        forceVanillaRecipes = config.get(Configuration.CATEGORY_GENERAL,
                "ForceVanillaRecipes",
                false,
                "Ignore Thermal Expansion and use Vanilla recipes instead").getBoolean();
        forceIronRotor = config.get(Configuration.CATEGORY_GENERAL,
                "ForceIronRotorTexture",
                false,
                "Use the iron rotor texture regardless of the rotor material").getBoolean();

        /* Windmill efficiency */
        windmillEfficiency[0] = config.get(Configuration.CATEGORY_GENERAL,
                "WindmillBasicEfficiency",
                0.1,
                "How good the windmill is at extracting energy from the wind").getDouble();
        windmillEfficiency[1] = config.get(Configuration.CATEGORY_GENERAL,
                "WindmillHardenedEfficiency",
                0.35).getDouble();
        windmillEfficiency[2] = config.get(Configuration.CATEGORY_GENERAL,
                "WindmillReinforcedEfficiency",
                0.65).getDouble();
        windmillEfficiency[3] = config.get(Configuration.CATEGORY_GENERAL,
                "WindmillResonantEfficiency",
                0.95).getDouble();

        /* Windmill storage */
        windmillEnergyStorage[0] = config.get(Configuration.CATEGORY_GENERAL,
                "WindmillBasicEnergyStorage",
                16000,
                "Energy storage capacity of the windmill in RF").getInt();
        windmillEnergyStorage[1] = config.get(Configuration.CATEGORY_GENERAL,
                "WindmillHardenedEnergyStorage",
                32000).getInt();
        windmillEnergyStorage[2] = config.get(Configuration.CATEGORY_GENERAL,
                "WindmillReinforcedEnergyStorage",
                48000).getInt();
        windmillEnergyStorage[3] = config.get(Configuration.CATEGORY_GENERAL,
                "WindmillResonantEnergyStorage",
                64000).getInt();

        /* Windmill energy transfer multiplier */
        windmillEnergyTransferMultiplier = config.get(Configuration.CATEGORY_GENERAL,
                "WindmillEnergyTransferMultiplier",
                0.01,
                "Multiply by the storage to get the rate of energy transfer in RF/t").getDouble();

        /* Rotor efficiency */
        rotorEfficiency[0] = config.get(Configuration.CATEGORY_GENERAL,
                "RotorBasicEnergyMultiplier",
                0.3,
                "Multiplier applied to the windmill generation due to the rotor").getDouble();
        rotorEfficiency[1] = config.get(Configuration.CATEGORY_GENERAL,
                "RotorHardenedEnergyMultiplier",
                0.5).getDouble();
        rotorEfficiency[2] = config.get(Configuration.CATEGORY_GENERAL,
                "RotorReinforcedEnergyMultiplier",
                0.7).getDouble();
        rotorEfficiency[3] = config.get(Configuration.CATEGORY_GENERAL,
                "RotorResonantEnergyMultiplier",
                1.0).getDouble();

        /* Weather multipliers */
        weatherMultiplierRain = config.get(Configuration.CATEGORY_GENERAL,
                "WeatherRainEnergyGenerationMultiplier",
                1.2,
                "Multiplier applied to the windmill generation when it's raining").getDouble();
        weatherMultiplierThunder = config.get(Configuration.CATEGORY_GENERAL,
                "WeatherThunderEnergyGenerationMultiplier",
                1.5,
                "Multiplier applied to the windmill generation when it's raining").getDouble();

        /* Handcrank power */
        handcrankPower = config.get(Configuration.CATEGORY_GENERAL,
                "HandcrankPower",
                35,
                "Power the player can apply to the rotor, in RF/t").getDouble();

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

    public static float getWindmillEfficiency(int pType) {
        return (float) windmillEfficiency[pType];
    }

    public static int[] getWindmillEnergyStorage() {
        return windmillEnergyStorage;
    }

    public static float getWindmillEnergyTransferMultiplier() {
        return (float) windmillEnergyTransferMultiplier;
    }

    public static float getRotorEfficiency(int pType) {
        return (float) rotorEfficiency[pType];
    }

    public static float getWeatherMultiplierRain() {
        return (float) weatherMultiplierRain;
    }

    public static float getWeatherMultiplierThunder() {
        return (float) weatherMultiplierThunder;
    }

    public static float getHandcrankPower() {
        return (float) handcrankPower;
    }
}
