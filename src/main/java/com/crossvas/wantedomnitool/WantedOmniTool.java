package com.crossvas.wantedomnitool;

import com.crossvas.wantedomnitool.keyboard.Keyboard;
import com.crossvas.wantedomnitool.network.NetworkHandler;
import com.crossvas.wantedomnitool.proxy.CommonProxy;
import com.crossvas.wantedomnitool.utils.Recipes;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

@Mod(modid = WantedOmniTool.MODID, name = WantedOmniTool.NAME, version = WantedOmniTool.VERSION, dependencies = WantedOmniTool.DEPS)
public class WantedOmniTool {
    public static final String MODID = "wantedomnitool";
    public static final String NAME = "Wanted Omni Tool";
    public static final String VERSION = "1.12.2-2.0.8";
    public static final String DEPS = "required-after:redstoneflux; after:enderio; after:thermalexpansion; after:thermalfoundation";

    @SidedProxy(clientSide = "com.crossvas.wantedomnitool.proxy.ClientProxy", serverSide = "com.crossvas.wantedomnitool.proxy.CommonProxy")
    public static CommonProxy proxy;

    @SidedProxy(clientSide = "com.crossvas.wantedomnitool.keyboard.KeyboardClient", serverSide = "com.crossvas.wantedomnitool.keyboard.Keyboard")
    public static Keyboard keyboard;

    @SidedProxy(clientSide = "com.crossvas.wantedomnitool.network.NetworkHandlerClient", serverSide = "com.crossvas.wantedomnitool.network.NetworkHandler")
    public static NetworkHandler network;

    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        proxy.init();
        keyboard.init();
        network.init();
    }

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent e) {
        Recipes.init();
        logger.log(Level.INFO, "Recipe Initialization done!");

    }
}
