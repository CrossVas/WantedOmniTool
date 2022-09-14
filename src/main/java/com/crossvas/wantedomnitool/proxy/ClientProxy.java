package com.crossvas.wantedomnitool.proxy;

import com.crossvas.wantedomnitool.WantedOmniTool;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class ClientProxy extends CommonProxy {

    public static KeyBinding modeKeyBinding;
    public static KeyBinding altKeyBinding;

    public static void init() {
    	modeKeyBinding = new KeyBinding("key.modeswitch.desc", Keyboard.KEY_F, "key." + WantedOmniTool.MODID + ".category");
    	altKeyBinding = new KeyBinding("key.alt.desc", Keyboard.KEY_LMENU, "key." + WantedOmniTool.MODID + ".category");
        ClientRegistry.registerKeyBinding(modeKeyBinding);
        ClientRegistry.registerKeyBinding(altKeyBinding);
    }

    @Override
    public void registerItemRenderer(Item item, int meta) {
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
