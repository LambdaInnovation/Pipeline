package cn.lambdalib.pipeline.api;

import cn.lambdalib.pipeline.core.DefaultGraphicPipeline;
import cn.lambdalib.pipeline.core.GLBuffer;
import cn.lambdalib.pipeline.core.Lazy;

import java.util.function.Supplier;

public class PipelineFactory {

    public static GraphicPipeline create() {
        return new DefaultGraphicPipeline();
    }





    private PipelineFactory() {}



}
