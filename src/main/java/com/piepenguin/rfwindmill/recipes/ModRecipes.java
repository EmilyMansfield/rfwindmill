package com.piepenguin.rfwindmill.recipes;


import com.google.common.base.Preconditions;
import com.piepenguin.rfwindmill.blocks.ModBlocks;
import com.piepenguin.rfwindmill.items.ModItems;
import com.piepenguin.rfwindmill.lib.Constants;
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
        if (Loader.isModLoaded(Constants.THERMAL_EXPANSION_MOD_ID)) {
            registerThermalExpansionRecipes();
        } else {
            registerVanillaRecipes();
        }
    }

    public static void registerCommonRecipes() {
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.rotor1, "i  ", "iii", "  i", 'i', "ingotIron"));

    }

    public static void registerVanillaRecipes() {
        String ingotTin = getOreWithFallback("ingotTin", "ingotIron");

        // No Thermal Expansion so use gold rotor, nether rotor, and diamond rotor
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.rotor2, "g  ", "gig", "  g", 'i', "ingotIron", 'g', "ingotGold"));
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.rotor3, "n  ", "non", "  n", 'n', Items.quartz, 'o', Blocks.obsidian));
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.rotor4, "d  ", "dgd", "  d", 'd', "gemDiamond", 'g', "ingotGold"));

        GameRegistry.addRecipe(new ShapedOreRecipe(ModBlocks.windmillBlock1, " x ", "ibi", "iri", 'x', ModItems.rotor1, 'i', ingotTin, 'r', "dustRedstone", 'b', "blockIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(ModBlocks.windmillBlock2, " x ", "ibi", "iri", 'x', ModItems.rotor2, 'i', "ingotGold", 'r', "dustRedstone", 'b', "blockIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(ModBlocks.windmillBlock3, " x ", "ibi", "oro", 'x', ModItems.rotor3, 'i', Blocks.quartz_block, 'o', Blocks.obsidian, 'r', "dustRedstone", 'b', "blockIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(ModBlocks.windmillBlock4, " x ", "dbd", "iri", 'x', ModItems.rotor4, 'd', "gemDiamond", 'i', "ingotGold", 'r', "dustRedstone", 'b', "blockIron"));
    }

    public static void registerThermalExpansionRecipes() {
        ItemStack powerCoilSilver = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "powerCoilSilver", 1));
        ItemStack machineFrameBasic = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "frameMachineBasic", 1));
        ItemStack machineFrameHardened = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "frameMachineHardened", 1));
        ItemStack machineFrameReinforced = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "frameMachineReinforced", 1));
        ItemStack machineFrameResonant = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "frameMachineResonant", 1));

        // Thermal Expansion loaded so use electrum rotor, signalum rotor, and enderium rotor
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.rotor2, "e  ", "eie", "  e", 'i', "ingotInvar", 'e', "ingotElectrum"));
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.rotor3, "s  ", "ses", "  s", 's', "ingotSignalum", 'e', "ingotElectrum"));
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.rotor4, "e  ", "eie", "  e", 'e', "ingotEnderium", 'i', "ingotInvar"));

        GameRegistry.addRecipe(new ShapedOreRecipe(ModBlocks.windmillBlock1, " x ", "imi", " c ", 'x', ModItems.rotor1, 'i', "ingotTin", 'm', machineFrameBasic, 'c', powerCoilSilver));
        GameRegistry.addRecipe(new ShapedOreRecipe(ModBlocks.windmillBlock2, " x ", "imi", " c ", 'x', ModItems.rotor2, 'i', "ingotInvar", 'm', machineFrameHardened, 'c', powerCoilSilver));
        GameRegistry.addRecipe(new ShapedOreRecipe(ModBlocks.windmillBlock3, " x ", "imi", " c ", 'x', ModItems.rotor3, 'i', "ingotSilver", 'm', machineFrameReinforced, 'c', powerCoilSilver));
        GameRegistry.addRecipe(new ShapedOreRecipe(ModBlocks.windmillBlock4, " x ", "imi", " c ", 'x', ModItems.rotor4, 'i', "ingotPlatinum", 'm', machineFrameResonant, 'c', powerCoilSilver));
    }

    public static String getOreWithFallback(String ore, String fallback) {
        if(OreDictionary.getOres(ore).isEmpty()) {
            return fallback;
        }
        return ore;
    }
}
