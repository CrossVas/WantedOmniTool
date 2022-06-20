package com.crossvas.wantedomnitool.proxy;

import com.crossvas.wantedomnitool.WantedOmniTool;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class ClientProxy extends CommonProxy {

    public static KeyBinding keyBinding;

    public static void init() {
        keyBinding = new KeyBinding("key.modeswitch.desc", Keyboard.KEY_F, "key." + WantedOmniTool.MODID + ".category");
        ClientRegistry.registerKeyBinding(keyBinding);
    }

    @Override
    public void registerItemRenderer(Item item, int meta) {
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
