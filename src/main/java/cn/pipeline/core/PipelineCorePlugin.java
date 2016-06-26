package cn.pipeline.core;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

public class PipelineCorePlugin implements IFMLLoadingPlugin {

    private static boolean deobfEnabled;

    public static boolean isDeobfEnabled() {
        return deobfEnabled;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { "cn.pipeline.core.PipelineTransformer" };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        deobfEnabled = (Boolean) data.get("runtimeDeobfuscationEnabled");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

}
