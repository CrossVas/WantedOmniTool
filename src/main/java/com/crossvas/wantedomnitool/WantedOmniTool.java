package com.crossvas.wantedomnitool;

import com.crossvas.wantedomnitool.proxy.ClientProxy;
import com.crossvas.wantedomnitool.proxy.CommonProxy;
import com.crossvas.wantedomnitool.utils.Recipes;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = WantedOmniTool.MODID, name = WantedOmniTool.NAME, version = WantedOmniTool.VERSION, dependencies = WantedOmniTool.DEPS)
public class WantedOmniTool {
    public static final String MODID = "wantedomnitool";
    public static final String NAME = "Wanted Omni Tool";
    public static final String VERSION = "@VERSION@";
    public static final String DEPS = "after:redstoneflux; after:enderio; after:thermalexpansion; after:thermalfoundation";

    @SidedProxy(clientSide = "com.crossvas.wantedomnitool.proxy.ClientProxy", serverSide = "com.crossvas.wantedomnitool.proxy.CommonProxy")
    public static CommonProxy proxy;

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        ClientProxy.init();
    }

    @EventHandler
    public static void postInit(FMLPostInitializationEvent e) {
        Recipes.init();
    }
}
