package cn.lambdalib.pipeline.api;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.util.Map;

/**
 * A simple intermediate representation of OBJ model.
 */
public class ObjModel {

    public static class Vertex {

        public final Vector3f pos;
        public final Vector2f uv;

        public Vertex(Vector3f pos, Vector2f uv) {
            this.pos = pos;
            this.uv = uv;
        }

        public void store(FloatBuffer buffer) {
            pos.store(buffer);
            uv.store(buffer);
        }
    }

    final Vertex[] vertices;
    final Map<String, int[]> indices;

    public ObjModel(Vertex[] vertices, Map<String, int[]> indices) {
        this.vertices = vertices;
        this.indices = indices;
    }

    public Vertex[] getVertices() { return vertices; }

    public Map<String, int[]> getIndices() { return indices; }
}
