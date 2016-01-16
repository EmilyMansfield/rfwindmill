package com.piepenguin.rfwindmill.recipes;


import com.google.common.base.Preconditions;
import com.piepenguin.rfwindmill.blocks.ModBlocks;
import com.piepenguin.rfwindmill.items.ModItems;
import com.piepenguin.rfwindmill.lib.Constants;
import com.piepenguin.rfwindmill.lib.ModConfiguration;
import com.piepenguin.rfwindmill.lib.Util;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

/**
 * Contains methods to register the recipes for all the items and blocks in the
 * mod. Uses vanilla recipes by default but if Thermal Foundation or Thermal
 * Expansion is loaded (and the functionality not disabled in the config file)
 * then the recipes will be modified to utilise those mods.
 */
public class ModRecipes {

    /**
     * Register Thermal Expansion and Thermal Foundation recipes if they are
     * loaded and the functionality is enabled in the config file otherwise
     * register the vanilla recipes.
     */
    public static void init() {
        registerCommonRecipes();
        if(Util.useThermalFoundation() && !ModConfiguration.useVanillaRecipes()) {
            registerThermalFoundationRotors();
        } else {
            registerVanillaRotors();
        }
        if(Util.useThermalExpansion() && !ModConfiguration.useVanillaRecipes()) {
            registerThermalExpansionTurbines();
        } else {
            registerVanillaTurbines();
        }
    }

    /**
     * Register recipes not dependent on the loaded mods.
     */
    private static void registerCommonRecipes() {
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.rotor1, "i  ", "iii", "  i", 'i', "ingotIron"));
    }

    /**
     * Register vanilla turbine recipes, utilising Forge ore dictionary materials
     * if relevant and loaded, otherwise recipes fall back to pure vanilla.
     * Tasty. Called when Thermal Expansion isn't installed or vanilla recipes
     * are forced.
     */
    private static void registerVanillaTurbines() {
        String ingotTin = getOreWithFallback("ingotTin", "ingotIron");

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 0), " x ", "ibi", "ixi", 'x', "dustRedstone", 'i', ingotTin, 'b', "blockIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 1), " x ", "ibi", "ixi", 'x', "dustRedstone", 'i', "ingotGold", 'b', "blockIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 2), " x ", "ibi", "oxo", 'x', "dustRedstone", 'i', Blocks.quartz_block, 'o', Blocks.obsidian, 'b', "blockIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 3), " x ", "dbd", "ixi", 'x', "dustRedstone", 'd', "gemDiamond", 'i', "ingotGold", 'b', "blockIron"));

        // Add upgrade recipes
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 1), "i i", " b ", "i i", 'i', "ingotGold", 'b', new ItemStack(ModBlocks.windmillBlock, 1, 0)));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 2), "i i", " b ", "o o", 'i', Blocks.quartz_block, 'o', Blocks.obsidian, 'b', new ItemStack(ModBlocks.windmillBlock, 1, 1)));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 3), "d d", " b ", "i i", 'i', "ingotGold", 'd', "gemDiamond", 'b', new ItemStack(ModBlocks.windmillBlock, 1, 2)));
    }

    /**
     * Register vanilla rotor recipes. Called when Thermal Foundation is not
     * loaded or vanilla recipes are forced. Rotor types created are
     * <ul>
     * <li>Gold Rotor</li>
     * <li>Nether Rotor</li>
     * <li>Diamond Rotor</li>
     * </ul>
     */
    private static void registerVanillaRotors() {
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.rotor2, "g  ", "gig", "  g", 'i', "ingotIron", 'g', "ingotGold"));
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.rotor3, "n  ", "non", "  n", 'n', Items.quartz, 'o', Blocks.obsidian));
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.rotor4, "d  ", "dgd", "  d", 'd', "gemDiamond", 'g', "ingotGold"));
    }

    /**
     * Register Thermal Foundation rotor recipes. Rotor types are
     * <ul>
     * <li>Electrum Rotor</li>
     * <li>Signalum Rotor</li>
     * <li>Enderium Rotor</li>
     * </ul>
     */
    private static void registerThermalFoundationRotors() {
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.rotor2, "e  ", "eie", "  e", 'i', "ingotInvar", 'e', "ingotElectrum"));
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.rotor3, "s  ", "ses", "  s", 's', "ingotSignalum", 'e', "ingotElectrum"));
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.rotor4, "e  ", "eie", "  e", 'e', "ingotEnderium", 'i', "ingotInvar"));
    }

    /**
     * Register Thermal Expansion turbine recipes.
     */
    private static void registerThermalExpansionTurbines() {
        ItemStack powerCoilSilver = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "powerCoilSilver", 1));
        ItemStack machineFrameBasic = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "frameMachineBasic", 1));
        ItemStack machineFrameHardened = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "frameMachineHardened", 1));
        ItemStack machineFrameReinforced = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "frameMachineReinforced", 1));
        ItemStack machineFrameResonant = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "frameMachineResonant", 1));
        ItemStack hardenedGlass = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "Glass", 1));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 0), " x ", "imi", " c ", 'x', "dustRedstone", 'i', "ingotTin", 'm', machineFrameBasic, 'c', powerCoilSilver));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 1), " x ", "imi", " c ", 'x', "dustRedstone", 'i', "ingotInvar", 'm', machineFrameHardened, 'c', powerCoilSilver));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 2), " x ", "imi", " c ", 'x', "dustRedstone", 'i', "ingotSilver", 'm', machineFrameReinforced, 'c', powerCoilSilver));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 3), " x ", "imi", " c ", 'x', "dustRedstone", 'i', "ingotPlatinum", 'm', machineFrameResonant, 'c', powerCoilSilver));

        // Add upgrade recipes
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 1), "igi", "jmj", "i i", 'i', "ingotInvar", 'j', "ingotInvar", 'g', "gearElectrum", 'm', new ItemStack(ModBlocks.windmillBlock, 1, 0)));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 2), "igi", "jmj", "i i", 'i', hardenedGlass, 'j', "ingotSilver", 'g', "gearSignalum", 'm', new ItemStack(ModBlocks.windmillBlock, 1, 1)));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 3), "igi", "jmj", "i i", 'i', "ingotSilver", 'j', "ingotPlatinum", 'g', "gearEnderium", 'm', new ItemStack(ModBlocks.windmillBlock, 1, 2)));
    }

    /**
     * Attempt to find the {@code pOre} in the Forge ore dictionary and return
     * it if found otherwise return the {@code pFallback}
     *
     * @param pOre      Ore name to search for (not necessarily an ore)
     * @param pFallback Material to fall back to if the {@code pOre} wasn't found
     * @return {@code pOre} if it's in the dictionary, {@code fallback} otherwise
     */
    public static String getOreWithFallback(String pOre, String pFallback) {
        if(OreDictionary.getOres(pOre).isEmpty()) {
            return pFallback;
        }
        return pOre;
    }
}
