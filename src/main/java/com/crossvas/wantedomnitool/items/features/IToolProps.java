package com.crossvas.wantedomnitool.items.features;

import com.crossvas.wantedomnitool.utils.MiscUtil;
import com.crossvas.wantedomnitool.utils.TextFormatter;
import com.google.common.base.CaseFormat;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;

public interface IToolProps {

    default ITextComponent getPropComp(ItemStack stack) {
        ToolProps props = getProps(stack);
        return TextFormatter.GOLD.translatable("message.text.mode.eff", props.formattedName);
    }

    default ToolProps getProps(ItemStack drill) {
        NBTTagCompound tag = MiscUtil.getTag(drill);
        return ToolProps.getFromId(tag.getByte("toolProps"));
    }

    default ToolProps getNextProps(ItemStack drill) {
        NBTTagCompound tag = MiscUtil.getTag(drill);
        return ToolProps.getFromId(tag.getByte("toolProps") + 1);
    }

    default void saveProps(ItemStack drill, ToolProps props) {
        NBTTagCompound tag = MiscUtil.getTag(drill);
        tag.setByte("toolProps", (byte) props.ordinal());
    }

    enum ToolProps {
        NORMAL(24.0F, 800, TextFormatter.BLUE, 84, 84, 252),
        LOW_POWER(12.0F, 400, TextFormatter.GREEN, 84, 251, 84),
        FINE(8.0F, 300, TextFormatter.AQUA, 84, 251, 251);

        private static final ToolProps[] VALUES = values();
        public final float efficiency;
        public final int energyCost;
        public final int r;
        public final int g;
        public final int b;
        public ITextComponent formattedName;

        ToolProps(float efficiency, int energyCost, TextFormatter format, int r, int g, int b) {
            this.formattedName = format.translatable("message.text.mode." + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_UNDERSCORE, name()));
            this.efficiency = efficiency;
            this.energyCost = energyCost;
            this.r = 255 - r;
            this.g = 255 - g;
            this.b = 255 - b;
        }

        public static ToolProps getFromId(int id) {
            return VALUES[id % VALUES.length];
        }
    }
}
