package cn.lambdalib.pipeline.api;

import cn.lambdalib.pipeline.api.Attribute.AttributeType;
import cn.lambdalib.pipeline.core.GLBuffer;
import cn.lambdalib.pipeline.core.Lazy;
import cn.lambdalib.pipeline.core.VAO;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import static cn.lambdalib.pipeline.core.Utils.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class Material {

    /**
     * <p>
     * Create a material from given `ShaderProgram`. The Material shall
     *  own the program, and any modification to the program's execution environment
     *  (e.g. uniforms) without using this `Material` yields undefined results. </p>
     *
     *  TODO: Auto determine the data type in LayoutMapping.
     */
    public static Material load(ShaderProgram program, LayoutMapping mapping) {
        return new Material(program, mapping);
    }

    final ShaderProgram program;
    final Map<String, Layout> layouts = new HashMap<>();
    final List<Layout>
        vertexLayouts = new ArrayList<>(),
        instanceLayouts = new ArrayList<>();

    final int vertexFloats, instanceFloats;

    private Material(ShaderProgram program, LayoutMapping mapping) {
        this.program = program;

        for (Entry<String, Pair<LayoutType, AttributeType>> entry : mapping.mappings.entrySet()) {
            String name = entry.getKey();
            LayoutType layoutType = entry.getValue().getLeft();
            AttributeType attrType = entry.getValue().getRight();

            int index = glGetAttribLocation(program.getProgramID(), name);
            Preconditions.checkState(index != -1, "Invalid attribute: " + name);

            List<Layout> subList;
            switch (layoutType) {
                case VERTEX:
                    subList = vertexLayouts;
                    break;
                case INSTANCE:
                    subList = instanceLayouts;
                    break;
                default: throw new RuntimeException();
            }

            int padding = calcFloats(subList);
            Layout layout = new Layout(index, name, layoutType, attrType, padding);

            layouts.put(name, layout);
            subList.add(layout);
        }

        vertexFloats = calcFloats(vertexLayouts);
        instanceFloats = calcFloats(instanceLayouts);
    }

    private int calcFloats(List<Layout> list) {
        if (list.isEmpty()) {
            return 0;
        } else {
            Layout lastLayout = list.get(list.size() - 1);
            return lastLayout.floatPadding + lastLayout.attrType.dimension;
        }
    }

    public Mesh newMesh(MeshType type) {
        return new Mesh(type);
    }

    public Vertex newVertex(Attribute ...attrs) {
        return new Vertex(fill(LayoutType.VERTEX, attrs));
    }

    public Instance newInstance(Attribute ...attrs) {
        return new Instance(fill(LayoutType.INSTANCE, attrs));
    }

    public Layout getLayout(String name) {
        return Preconditions.checkNotNull(layouts.get(name), "Layout " + name + " doesn't exist");
    }

    public ShaderProgram getProgram() {
        return program;
    }

    private float[] fill(LayoutType type, Attribute ...attrs) {
        float[] arr = new float[type == LayoutType.VERTEX ? vertexFloats : instanceFloats];
        for (Attribute attr : attrs) {
            assert attr.layout.type == type;

            int padding = attr.layout.floatPadding;
            float[] in = attr.value;

            for (int i = 0; i < in.length; ++i) {
                arr[i + padding] = in[i];
            }
        }

        return arr;
    }

    public enum LayoutType {
        VERTEX, INSTANCE
    }

    public enum MeshType {
        STATIC, DYNAMIC
    }

    public class Layout {

        public final int location;
        public final String name;
        public final LayoutType type;
        public final AttributeType attrType;

        final int floatPadding;

        private Layout(int location, String name,
                       LayoutType type, AttributeType attrType,
                       int floatPadding) {
            this.location = location;
            this.name = name;
            this.type = type;
            this.attrType = attrType;
            this.floatPadding = floatPadding;
        }

    }

    public class Vertex {

        final float[] values;

        private Vertex(float[] values) {
            this.values = values;
        }

        Material _enclosingMaterial() {
            return Material.this;
        }

    }

    public class Instance {

        final float[] values;

        private Instance(float[] values) {
            this.values = values;
        }

        Material _enclosingMaterial() {
            return Material.this;
        }

    }


    public static class LayoutMapping {

        final Map<String, Pair<LayoutType, AttributeType>> mappings = new HashMap<>();

        public static LayoutMapping create(Pair<String, Pair<LayoutType, AttributeType>>... input) {
            LayoutMapping ret = new LayoutMapping();
            for (Pair<String, Pair<LayoutType, AttributeType>> p : input) {
                ret.mappings.put(p.getLeft(), p.getRight());
            }
            return ret;
        }

    }

    public class Mesh {

        private final int bufferHint;

        private Supplier<GLBuffer>
            vbo_ = Lazy.withInitializer(GLBuffer::create),
            ibo_ = Lazy.withInitializer(GLBuffer::create);

        private Map<Integer, SubMesh> subMeshes = new HashMap<>();

        private boolean indicesChanged = true;
        private int indiceCount = 0;

        public Mesh(MeshType type) {
            if (type == MeshType.STATIC) {
                bufferHint = GL_STATIC_DRAW;
            } else {
                bufferHint = GL_DYNAMIC_DRAW;
            }
        }

        public void setVertices(Vertex ...vertices) {
            GLBuffer vbo = vbo_.get();

            FloatBuffer buffer = BufferUtils.createFloatBuffer(vertexFloats * vertices.length);
            for (Vertex v : vertices) {
                buffer.put(v.values);
            }
            buffer.flip();

            glBindBuffer(GL_ARRAY_BUFFER, vbo.getID());
            glBufferData(GL_ARRAY_BUFFER, buffer, bufferHint);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }

        public void setIndices(int[] indices) {
            setSubIndices(0, indices);
        }

        public void setSubIndices(int subMesh, int[] indices) {
            subMeshes.put(subMesh, new SubMesh(indices));
            indicesChanged = true;

            indiceCount = 0;
            for (SubMesh mesh : subMeshes.values()) {
                indiceCount += mesh.indices.length;
            }
        }

        GLBuffer getVBO() {
            return vbo_.get();
        }

        int getIndiceCount() {
            return indiceCount;
        }

        GLBuffer getIBOAll() {
            // Optimize: If there is only one sub mesh, return directly that sub mesh's buffer.
            if (subMeshes.size() == 1) {
                return subMeshes.values().iterator().next().buffer.get();
            }

            GLBuffer ret = ibo_.get();

            if (indicesChanged) {
                indicesChanged = false;

                int totalSize = 0;
                for (SubMesh mesh : subMeshes.values()) {
                    totalSize += mesh.indices.length;
                }

                IntBuffer buffer = BufferUtils.createIntBuffer(totalSize);
                for (SubMesh mesh : subMeshes.values()) {
                    buffer.put(mesh.indices);
                }
                buffer.flip();

                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ret.getID());
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, bufferHint);
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            }

            return ret;
        }

        GLBuffer getIBOSub(int subIndex) {
            return Preconditions.checkNotNull(
                    subMeshes.get(subIndex),
                    "No submesh with ID " + subIndex).buffer.get();
        }

        private class SubMesh {
            final int[] indices;
            final Supplier<GLBuffer> buffer;

            SubMesh(int[] indices) {
                this.indices = indices;
                this.buffer = Lazy.withInitializer(() -> {
                    GLBuffer ret = GLBuffer.create();

                    IntBuffer buffer = BufferUtils.createIntBuffer(indices.length);
                    buffer.put(indices);
                    buffer.flip();

                    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ret.getID());
                    glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, bufferHint);
                    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

                    return ret;
                });
            }

        }

    }

}
