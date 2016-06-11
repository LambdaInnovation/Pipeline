package cn.lambdalib.pipeline.api;

import java.nio.FloatBuffer;

/**
 * Stores per-instance data.
 */
public interface IInstanceData {

    void upload(FloatBuffer buffer);

}
