package com.crossvas.wantedomnitool.utils;

import com.crossvas.wantedomnitool.WantedOmniTool;
import com.crossvas.wantedomnitool.items.IHasModel;
import com.crossvas.wantedomnitool.items.ModItems;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = WantedOmniTool.MODID)
public class EventHandler {

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> e) {
        e.getRegistry().registerAll(ModItems.toRegister.toArray(new Item[0]));
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e) {
        for (Item item : ModItems.toRegister) {
            if (item instanceof IHasModel) {
                ((IHasModel) item).registerModel();
            }
        }
    }
}
