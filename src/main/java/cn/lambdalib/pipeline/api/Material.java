package cn.lambdalib.pipeline.api;

import cn.lambdalib.pipeline.api.Attribute.AttributeType;
import cn.lambdalib.pipeline.core.GLBuffer;
import cn.lambdalib.pipeline.core.Lazy;
import cn.lambdalib.pipeline.core.Utils;
import cn.lambdalib.pipeline.core.VAO;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.ByteBuffer;
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
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL31.glGetActiveUniformName;

public class Material {

    /**
     * <p>
     * Create a material from given `ShaderProgram`. The Material shall
     *  own the program, and any modification to the program's execution environment
     *  (e.g. uniforms) without using this `Material` yields undefined results. </p>
     */
    public static Material load(ShaderProgram program, LayoutMapping mapping) {
        return new Material(program, mapping);
    }

    final ShaderProgram program;

    final Map<String, Layout> layouts = new HashMap<>();

    private final Map<String, Uniform> uniformLocations = new HashMap<>();

    final List<Layout>
        vertexLayouts = new ArrayList<>(),
        instanceLayouts = new ArrayList<>();

    final int vertexFloats, instanceFloats;

    private Material(ShaderProgram program, LayoutMapping mapping) {
        this.program = program;

        IntBuffer sizeBuffer = BufferUtils.createIntBuffer(1),
                typeBuffer = BufferUtils.createIntBuffer(1),
                lenBuffer = BufferUtils.createIntBuffer(1);

        {
            int uniformCount = glGetProgrami(program.getProgramID(), GL_ACTIVE_UNIFORMS);

            ByteBuffer nameBuffer = BufferUtils.createByteBuffer(glGetProgrami(program.getProgramID(),
                    GL_ACTIVE_UNIFORM_MAX_LENGTH));

            for (int i = 0; i < uniformCount; ++i) {
                nameBuffer.clear();
                typeBuffer.clear();
                sizeBuffer.clear();
                lenBuffer.clear();

                glGetActiveUniform(program.getProgramID(), i, lenBuffer, sizeBuffer, typeBuffer, nameBuffer);

                String name = toString(lenBuffer.get(), nameBuffer);
                int location = glGetUniformLocation(program.getProgramID(), name);

                uniformLocations.put(name, new Uniform(name, i));
            }
        }


        int attributeCount = glGetProgrami(program.getProgramID(), GL_ACTIVE_ATTRIBUTES);
        ByteBuffer nameBuffer = BufferUtils.createByteBuffer(
                glGetProgrami(program.getProgramID(), GL_ACTIVE_ATTRIBUTE_MAX_LENGTH));

        for (int i = 0; i < attributeCount; ++i) {
            nameBuffer.clear();
            typeBuffer.clear();
            sizeBuffer.clear();
            lenBuffer.clear();

            glGetActiveAttrib(program.getProgramID(), i, lenBuffer, sizeBuffer, typeBuffer, nameBuffer);

            String name = toString(lenBuffer.get(), nameBuffer);

            int index = glGetAttribLocation(program.getProgramID(), name);
            int type = typeBuffer.get();

            LayoutType layoutType = Preconditions.checkNotNull(mapping.mappings.get(name),
                    "Attribute " + name + " 's input not defined");
            AttributeType attrType = AttributeType.fromGL(type);

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

    private String toString(int len, ByteBuffer buffer) {
        StringBuilder sb = new StringBuilder();
        while (len --> 0) {
            sb.append((char) buffer.get());
        }
        return sb.toString();
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

    public UniformBlock newUniformBlock() {
        return new UniformBlock();
    }

    public Layout getLayout(String name) {
        return Preconditions.checkNotNull(layouts.get(name), "Layout " + name + " doesn't exist");
    }

    /**
     * Update the uniforms in given {@link UniformBlock} into the material.
     *  It must be created from this `Material`, otherwise the behaviour is undefined.
     */
    public void setUniforms(UniformBlock uniforms) {
        glUseProgram(program.getProgramID());

        for (Entry<Integer, UniformDispatcher> entry : uniforms.dispatcher.entrySet()) {
            int index = entry.getKey();
            entry.getValue().dispatch(index);
        }

        glUseProgram(0);
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

    }

    public class Instance {

        final float[] values;

        private Instance(float[] values) {
            this.values = values;
        }

    }


    public static class LayoutMapping {

        final Map<String, LayoutType> mappings = new HashMap<>();

        public static LayoutMapping create(Pair<String, LayoutType>... input) {
            LayoutMapping ret = new LayoutMapping();
            for (Pair<String, LayoutType> p : input) {
                ret.mappings.put(p.getLeft(), p.getRight());
            }
            return ret;
        }

    }

    private interface UniformDispatcher {

        void dispatch(int idx);

    }

    /**
     * Stores uniform parameters to be uploaded to the material.
     */
    public class UniformBlock {

        private final Map<Integer, UniformDispatcher> dispatcher = new HashMap<>();

        public UniformBlock setFloat(String id, float x) {
            dispatcher.put(index(id), idx -> glUniform1f(idx, x));
            return this;
        }

        public UniformBlock setVec2(String id, float x, float y) {
            dispatcher.put(index(id), idx -> glUniform2f(idx, x, y));
            return this;
        }

        public UniformBlock setVec3(String id, float x, float y, float z) {
            dispatcher.put(index(id), idx -> glUniform3f(idx, x, y, z));
            return this;
        }

        public UniformBlock setMat4(String id, Matrix4f matrix) {
            FloatBuffer buf = BufferUtils.createFloatBuffer(16);
            matrix.store(buf);
            buf.flip();

            dispatcher.put(index(id), idx -> glUniformMatrix4(idx, false, buf));

            return this;
        }

        private int index(String id) {
            return Preconditions.checkNotNull(uniformLocations.get(id)).index;
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

    private class Uniform {
        final String name;
        final int index;

        public Uniform(String name, int index) {
            this.name = name;
            this.index = index;
        }
    }

}
