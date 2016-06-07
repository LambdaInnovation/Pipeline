package cn.pipeline.example.ref;

import cn.lambdalib.pipeline.api.IInstanceData;
import cn.lambdalib.pipeline.api.IMaterial;
import cn.lambdalib.pipeline.api.IMesh;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL33.*;

public class RefGeneratedMaterial implements IMaterial {

    private final int shaderID;

    public RefGeneratedMaterial(int shaderID) {
        this.shaderID = shaderID;
    }

    @Override
    public int getShaderID() {
        return shaderID;
    }

    @Override
    public void beforeDrawCall() {
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 32, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 32, 12);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 32, 20);
    }


    public static class Mesh implements IMesh {

        private final Vertex[] vertices;
        private final int[] indices;

        public class Vertex {
            public final Vector3f position;
            public final Vector2f uv;

            public Vertex(Vector3f position, Vector2f uv) {
                this.position = position;
                this.uv = uv;
            }

            public void store(FloatBuffer buf) {
                position.store(buf);
                uv.store(buf);
            }
        }

        public Mesh(Vertex[] vertices, int[] indices) {
            this.vertices = vertices;
            this.indices = indices;
        }

        @Override
        public void uploadVBO(FloatBuffer buffer) {
            for (Vertex v : vertices) {
                v.position.store(buffer);
                v.uv.store(buffer);
            }
        }

        @Override
        public void uploadIBO(ByteBuffer buffer) {
            if (vertices.length < Byte.MAX_VALUE) {
                for (int i : indices) buffer.put((byte) i);
            } else if (vertices.length < Short.MAX_VALUE) {
                for (int i : indices) buffer.putShort((short) i);
            } else {
                for (int i : indices) buffer.putInt(i);
            }
        }

        @Override
        public int getFloatsPerVertex() {
            return 5;
        }

        @Override
        public int getVertexCount() {
            return vertices.length;
        }

        @Override
        public int getIndicesBytes() {
            if (vertices.length < Byte.MAX_VALUE) {
                return Byte.BYTES;
            } else if (vertices.length < Short.MAX_VALUE) {
                return Short.BYTES;
            } else {
                return Integer.BYTES;
            }
        }
    }

    public static class InstanceData implements IInstanceData {

        private final Vector3f color, offset;

        public InstanceData(Vector3f color, Vector3f offset) {
            this.color = color;
            this.offset = offset;
        }

        @Override
        public void upload(FloatBuffer buffer) {
            color.store(buffer);
            offset.store(buffer);
        }

        @Override
        public int getFloatsPerInstance() {
            return 6;
        }

    }

}
