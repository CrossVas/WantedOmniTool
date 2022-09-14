package com.crossvas.wantedomnitool.utils;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

public class MiscUtil {

    public static String formatSimpleMessage(TextFormatting color, String text) {
        return color + I18n.format(text);
    }

    public static String formatComplexMessage(TextFormatting color1, String text1, TextFormatting color2, String text2) {
        return formatSimpleMessage(color1, text1) + " " + formatSimpleMessage(color2, text2);
    }

    public static NBTTagCompound getOrCreateNbtData(ItemStack paramItemStack) {
        NBTTagCompound nBTTagCompound = paramItemStack.getTagCompound();
        if (nBTTagCompound == null) {
            nBTTagCompound = new NBTTagCompound();
            paramItemStack.setTagCompound(nBTTagCompound);
            nBTTagCompound.setInteger("toolMode", 0);
        }
        return nBTTagCompound;
    }

}
