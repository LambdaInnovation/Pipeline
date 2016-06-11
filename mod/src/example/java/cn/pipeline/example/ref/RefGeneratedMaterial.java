package cn.pipeline.example.ref;

import cn.lambdalib.pipeline.api.IInstanceData;
import cn.lambdalib.pipeline.api.IMaterial;
import cn.lambdalib.pipeline.api.IMesh;
import cn.lambdalib.pipeline.core.GLBuffer;
import cn.lambdalib.pipeline.core.Lazy;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL33.*;

public class RefGeneratedMaterial implements IMaterial {

    public static Mesh.Vertex newVertex(Vector3f position, Vector2f uv) {
        return new Mesh.Vertex(position, uv);
    }

    public static Mesh newMesh(Mesh.Vertex[] vertices, byte[] indices) {
        return new Mesh(vertices, indices);
    }

    public static InstanceData newInstance(Vector3f color, Vector3f offset, float scale) {
        return new InstanceData(color, offset, scale);
    }


    private final int programID;

    public RefGeneratedMaterial(int programID) {
        this.programID = programID;
    }

    @Override
    public int getProgramID() {
        return programID;
    }

    @Override
    public void beforeDrawCall(int vbo, int instanceVbo) {
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);
        glEnableVertexAttribArray(4);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 20, 0);
        glVertexAttribDivisor(0, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 20, 12);
        glVertexAttribDivisor(1, 0);

        glBindBuffer(GL_ARRAY_BUFFER, instanceVbo);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 28, 0);
        glVertexAttribDivisor(2, 1);
        glVertexAttribPointer(3, 3, GL_FLOAT, false, 28, 12);
        glVertexAttribDivisor(3, 1);
        glVertexAttribPointer(4, 1, GL_FLOAT, false, 28, 24);
        glVertexAttribDivisor(4, 1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    @Override
    public int getFloatsPerInstance() {
        return 7;
    }

    public static class Mesh implements IMesh {

        private final Vertex[] vertices;
        private final byte[] indices;

        private Lazy<GLBuffer>
            vertexBuffer = new Lazy<>(),
            indexBuffer = new Lazy<>();

        public static class Vertex {
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

        public Mesh(Vertex[] vertices, byte[] indices) {
            this.vertices = vertices;
            this.indices = indices;
        }

        @Override
        public int getIndicesCount() {
            return indices.length;
        }

        @Override
        public int getVertexCount() {
            return vertices.length;
        }

        @Override
        public int getVBO() {
            return vertexBuffer.get(() -> {
                GLBuffer vbo = GLBuffer.create();
                glBindBuffer(GL_ARRAY_BUFFER, vbo.getID());

                FloatBuffer buf = BufferUtils.createFloatBuffer(vertices.length * 5);
                for (Vertex v : vertices) {
                    v.position.store(buf);
                    v.uv.store(buf);
                }
                buf.flip();

                glBufferData(GL_ARRAY_BUFFER, buf, GL_STATIC_DRAW);
                glBindBuffer(GL_ARRAY_BUFFER, 0);

                return vbo;
            }).getID();
        }

        @Override
        public int getIBO() {
            return indexBuffer.get(() -> {
                GLBuffer ibo = GLBuffer.create();
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo.getID());

                ByteBuffer buf = BufferUtils.createByteBuffer(indices.length);
                buf.put(indices);
                buf.flip();

                glBufferData(GL_ELEMENT_ARRAY_BUFFER, buf, GL_STATIC_DRAW);

                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

                return ibo;
            }).getID();
        }

        @Override
        public int getIBO(int subIndex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getPrimitiveType() {
            return GL_UNSIGNED_BYTE;
        }
    }

    public static class InstanceData implements IInstanceData {

        private final Vector3f color, offset;
        private final float scale;

        public InstanceData(Vector3f color, Vector3f offset, float scale) {
            this.color = color;
            this.offset = offset;
            this.scale = scale;
        }

        @Override
        public void upload(FloatBuffer buffer) {
            color.store(buffer);
            offset.store(buffer);
            buffer.put(scale);
        }

    }

}
