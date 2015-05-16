package com.piepenguin.rfwindmill.recipes;


import com.google.common.base.Preconditions;
import com.piepenguin.rfwindmill.blocks.ModBlocks;
import com.piepenguin.rfwindmill.items.ModItems;
import com.piepenguin.rfwindmill.lib.Constants;
import com.piepenguin.rfwindmill.lib.ModConfiguration;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ModRecipes {

    public static void init() {
        registerCommonRecipes();
        if (Loader.isModLoaded(Constants.THERMAL_EXPANSION_MOD_ID) && !ModConfiguration.useVanillaRecipes()) {
            registerThermalExpansionRecipes();
        } else {
            registerVanillaRecipes();
        }
    }

    private static void registerCommonRecipes() {
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.rotor1, "i  ", "iii", "  i", 'i', "ingotIron"));
    }

    private static void registerVanillaRecipes() {
        String ingotTin = getOreWithFallback("ingotTin", "ingotIron");

        // No Thermal Expansion so use gold rotor, nether rotor, and diamond rotor
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.rotor2, "g  ", "gig", "  g", 'i', "ingotIron", 'g', "ingotGold"));
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.rotor3, "n  ", "non", "  n", 'n', Items.quartz, 'o', Blocks.obsidian));
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.rotor4, "d  ", "dgd", "  d", 'd', "gemDiamond", 'g', "ingotGold"));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 0), " x ", "ibi", "ixi", 'x', "dustRedstone", 'i', ingotTin, 'b', "blockIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 1), " x ", "ibi", "ixi", 'x', "dustRedstone", 'i', "ingotGold", 'b', "blockIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 2), " x ", "ibi", "oxo", 'x', "dustRedstone", 'i', Blocks.quartz_block, 'o', Blocks.obsidian, 'b', "blockIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 3), " x ", "dbd", "ixi", 'x', "dustRedstone", 'd', "gemDiamond", 'i', "ingotGold", 'b', "blockIron"));
    }

    private static void registerThermalExpansionRecipes() {
        ItemStack powerCoilSilver = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "powerCoilSilver", 1));
        ItemStack machineFrameBasic = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "frameMachineBasic", 1));
        ItemStack machineFrameHardened = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "frameMachineHardened", 1));
        ItemStack machineFrameReinforced = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "frameMachineReinforced", 1));
        ItemStack machineFrameResonant = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "frameMachineResonant", 1));
        ItemStack hardenedGlass = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "Glass", 1));

        // Thermal Expansion loaded so use electrum rotor, signalum rotor, and enderium rotor
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.rotor2, "e  ", "eie", "  e", 'i', "ingotInvar", 'e', "ingotElectrum"));
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.rotor3, "s  ", "ses", "  s", 's', "ingotSignalum", 'e', "ingotElectrum"));
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.rotor4, "e  ", "eie", "  e", 'e', "ingotEnderium", 'i', "ingotInvar"));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 0), " x ", "imi", " c ", 'x', "dustRedstone", 'i', "ingotTin", 'm', machineFrameBasic, 'c', powerCoilSilver));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 1), " x ", "imi", " c ", 'x', "dustRedstone", 'i', "ingotInvar", 'm', machineFrameHardened, 'c', powerCoilSilver));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 2), " x ", "imi", " c ", 'x', "dustRedstone", 'i', "ingotSilver", 'm', machineFrameReinforced, 'c', powerCoilSilver));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 3), " x ", "imi", " c ", 'x', "dustRedstone", 'i', "ingotPlatinum", 'm', machineFrameResonant, 'c', powerCoilSilver));

        // Add upgrade recipes
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 1), "igi", "jmj", "iji", 'i', "ingotInvar", 'j', "ingotInvar", 'g', "gearElectrum", 'm', new ItemStack(ModBlocks.windmillBlock, 1, 0)));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 2), "igi", "jmj", "iji", 'i', hardenedGlass, 'j', "ingotSilver", 'g', "gearSignalum", 'm', new ItemStack(ModBlocks.windmillBlock, 1, 1)));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.windmillBlock, 1, 3), "igi", "jmj", "iji", 'i', "ingotSilver", 'j', "ingotPlatinum", 'g', "gearEnderium", 'm', new ItemStack(ModBlocks.windmillBlock, 1, 2)));
    }

    public static String getOreWithFallback(String pOre, String pFallback) {
        if(OreDictionary.getOres(pOre).isEmpty()) {
            return pFallback;
        }
        return pOre;
    }
}
