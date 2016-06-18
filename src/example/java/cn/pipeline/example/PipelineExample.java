package cn.pipeline.example;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid="pipeline-ex", name="PipelineMod example", version="0.1")
public class PipelineExample {

    @EventHandler
    public void postInit(FMLPostInitializationEvent evt) {
        MinecraftForge.EVENT_BUS.register(new GuiRenderHook());
        MinecraftForge.EVENT_BUS.register(new CubeRenderer());
    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent evt) {
        evt.registerServerCommand(new CallbackCommand("plgui", () -> GuiRenderHook.activated = !GuiRenderHook.activated));
        evt.registerServerCommand(new CallbackCommand("plcube", () -> CubeRenderer.activated = !CubeRenderer.activated));
    }

    private class CallbackCommand extends CommandBase {

        private final String name;
        private final Runnable callback;

        public CallbackCommand(String name, Runnable callback) {
            this.name = name;
            this.callback = callback;
        }

        @Override
        public String getCommandName() {
            return name;
        }

        @Override
        public String getCommandUsage(ICommandSender ics) {
            return "";
        }

        @Override
        public void processCommand(ICommandSender ics, String[] str) {
            callback.run();
        }
    }

}
