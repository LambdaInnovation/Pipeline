package cn.lambdalib.pipeline.api;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public interface IMesh {

    void uploadVBO(FloatBuffer buffer);

    void uploadIBO(ByteBuffer buffer);

    int getFloatsPerVertex();

    int getVertexCount();

    int getIndicesBytes();

}
