package cn.lambdalib.pipeline.api;

/**
 * A material is a wrapper of a GLSL shader program. It stores uniform information to be used when
 *  drawing in {@link GraphicPipeline}.
 */
public interface IMaterial {

    int getShaderID();

    /**
     * Invoked just before a draw call is to be emitted. The material shall now upload relevant data/states
     *  to GL, as well as update vertex format.
     */
    void beforeDrawCall();

}
