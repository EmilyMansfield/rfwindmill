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
    private static float[] windmillEfficiency = new float[4];
    private static int[] windmillEnergyStorage = new int[4];
    private static int windmillEnergyTransferMultiplier;
    // Config has no getFloat()
    private static double[] rotorEnergyMultiplier = new double[4];
    private static double weatherMultiplierRain;
    private static double weatherMultiplierThunder;
    private static double handcrankEnergyMultiplier;
    private static float windGenerationBase;
    private static float angularVelocityPerRF;

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
        windGenerationBase = (float)config.get(Configuration.CATEGORY_GENERAL,
                "WindGenerationBase",
                160.0,
                "The amount of energy in the wind in RF/t").getDouble();
        windmillEfficiency[0] = (float)config.get(Configuration.CATEGORY_GENERAL,
                "WindmillBasicEfficiency",
                0.05,
                "How good the windmill is at extracting energy from the wind").getDouble();
        windmillEfficiency[1] = (float)config.get(Configuration.CATEGORY_GENERAL,
                "WindmillHardenedEfficiency",
                0.2).getDouble();
        windmillEfficiency[2] = (float)config.get(Configuration.CATEGORY_GENERAL,
                "WindmillReinforcedEfficiency",
                0.5).getDouble();
        windmillEfficiency[3] = (float)config.get(Configuration.CATEGORY_GENERAL,
                "WindmillResonantEfficiency",
                0.9).getDouble();
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
        windmillEnergyTransferMultiplier = config.get(Configuration.CATEGORY_GENERAL,
                "WindmillEnergyTransferMultiplier",
                4,
                "Multiply by the base wind energy generation to get the rate of energy transfer in RF/t").getInt();
        rotorEnergyMultiplier[0] = config.get(Configuration.CATEGORY_GENERAL,
                "RotorBasicEnergyMultiplier",
                0.625,
                "Multiplier applied to the windmill generation due to the rotor").getDouble();
        rotorEnergyMultiplier[1] = config.get(Configuration.CATEGORY_GENERAL,
                "RotorHardenedEnergyMultiplier",
                0.75).getDouble();
        rotorEnergyMultiplier[2] = config.get(Configuration.CATEGORY_GENERAL,
                "RotorReinforcedEnergyMultiplier",
                0.875).getDouble();
        rotorEnergyMultiplier[3] = config.get(Configuration.CATEGORY_GENERAL,
                "RotorResonantEnergyMultiplier",
                1.0).getDouble();
        weatherMultiplierRain = config.get(Configuration.CATEGORY_GENERAL,
                "WeatherRainEnergyGenerationMultiplier",
                1.2,
                "Multiplier applied to the windmill generation when it's raining").getDouble();
        weatherMultiplierThunder = config.get(Configuration.CATEGORY_GENERAL,
                "WeatherThunderEnergyGenerationMultiplier",
                1.5,
                "Multiplier applied to the windmill generation when it's raining").getDouble();
        handcrankEnergyMultiplier = config.get(Configuration.CATEGORY_GENERAL,
                "HandcrankEnergyMultiplier",
                0.4,
                "Multiplier applied to energy generation when turning rotors by hand").getDouble();
        angularVelocityPerRF = (float)config.get(Configuration.CATEGORY_GENERAL,
                "AngularVelocityPerRF",
                0.15,
                "Degrees per RF per tick that the rotor rotates by").getDouble();
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

    public static float[] getWindmillEfficiency() {
        return windmillEfficiency;
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

    public static float getHandcrankEnergyMultiplier() {
        return (float)handcrankEnergyMultiplier;
    }

    public static float getWindGenerationBase() {
        return windGenerationBase;
    }

    public static float getAngularVelocityPerRF() {
        return angularVelocityPerRF;
    }
}
