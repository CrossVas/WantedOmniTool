package com.crossvas.wantedomnitool.utils;

import com.crossvas.wantedomnitool.WantedOmniTool;
import com.crossvas.wantedomnitool.items.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Recipes {

    public static void init() {
        if (Loader.isModLoaded("thermalexpansion") && Loader.isModLoaded("thermalfoundation")) {
            GameRegistry.addShapedRecipe(new ResourceLocation(WantedOmniTool.MODID, "omnitoolTE"), new ResourceLocation("omnitoolTE"),
                    new ItemStack(ModItems.omnitool), " SD", "PCd", "cP ",
                    'D', "gearDiamond",
                    'P', "plateSteel",
                    'S', RefItems.te_blade,
                    'C', RefItems.te_tool_case,
                    'd', RefItems.te_drill_head,
                    'c', RefItems.te_capacitor);
        }
        if (Loader.isModLoaded("enderio")) {
            GameRegistry.addShapedRecipe(new ResourceLocation(WantedOmniTool.MODID, "omnitoolEIO"), new ResourceLocation("omnitoolEIO"),
                    new ItemStack(ModItems.omnitool), " SG", "CCS", "cC ",
                    'S', "itemInfinityRod",
                    'G', "gearDark",
                    'C', "itemChassiParts",
                    'c', RefItems.eio_charger_simple);
        }
    }
}
