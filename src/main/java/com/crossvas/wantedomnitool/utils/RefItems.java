package com.crossvas.wantedomnitool.utils;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class RefItems {

    @GameRegistry.ItemStackHolder(value = "thermalfoundation:material", meta = 657)
    public static ItemStack te_blade;

    @GameRegistry.ItemStackHolder(value = "thermalfoundation:material", meta = 656)
    public static ItemStack te_drill_head;

    @GameRegistry.ItemStackHolder(value = "thermalfoundation:material", meta = 640)
    public static ItemStack te_tool_case;

    @GameRegistry.ItemStackHolder(value = "thermalexpansion:capacitor")
    public static ItemStack te_capacitor;

    @GameRegistry.ItemStackHolder(value = "enderio:item_inventory_charger_simple")
    public static ItemStack eio_charger_simple;

}
