package cn.lambdalib.pipeline.api;


public interface GraphicPipeline {

    void draw(IMaterial material, IMesh mesh, IInstanceData instanceData);

    void drawNow(IMaterial material, IMesh mesh, IInstanceData instanceData);

    void flush();

}
