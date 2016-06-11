package cn.lambdalib.pipeline.api;

/**
 * A material is a wrapper of a GLSL shader program. It stores uniform information to be used when
 *  drawing in {@link GraphicPipeline}.
 */
public interface IMaterial {

    int getProgramID();

    /**
     * Invoked just before a draw call is to be emitted. The material shall now upload relevant data/states
     *  to GL, as well as update vertex format.
     */
    void beforeDrawCall(int vertexVBO, int instanceVBO);

    /**
     * @return How many floats used per instance data
     */
    int getFloatsPerInstance();

}
