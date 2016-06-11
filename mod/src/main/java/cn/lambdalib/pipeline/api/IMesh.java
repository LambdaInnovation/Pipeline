package cn.lambdalib.pipeline.api;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public interface IMesh {

    int getVertexCount();

    int getIndicesCount();

    /**
     * @return A VBO containing all vertex data of this mesh
     */
    int getVBO();

    /**
     * @return An IBO containing all vertex indices of this mesh
     */
    int getIBO();

    /**
     * @return An IBO containing vertex indices of given sub mesh index
     */
    int getIBO(int subIndex);

    /**
     * @return Primitive type for indices
     */
    int getPrimitiveType();

}
