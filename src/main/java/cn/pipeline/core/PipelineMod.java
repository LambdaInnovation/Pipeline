package cn.pipeline.core;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;

@Mod(modid="pipeline", name="Pipeline", version=PipelineMod.VERSION)
public class PipelineMod {

    public static final String VERSION = "0.1";

    @Mod.EventHandler
    public static void init(FMLInitializationEvent evt) {
        Utils.log.info("Pipeline v" + VERSION + " is loading.");
    }

}
