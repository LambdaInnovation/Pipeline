package cn.pipeline.example;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid="pipeline-ex", name="PipelineMod example", version="0.1")
public class PipelineExample {

    @EventHandler
    public void postInit(FMLPostInitializationEvent evt) {
        MinecraftForge.EVENT_BUS.register(new GuiRenderHook());
    }

}
