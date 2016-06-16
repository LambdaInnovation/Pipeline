package cn.lambdalib.pipeline.api;


import cn.lambdalib.pipeline.api.Material.Instance;
import cn.lambdalib.pipeline.api.Material.Layout;
import cn.lambdalib.pipeline.api.Material.LayoutType;
import cn.lambdalib.pipeline.api.Material.Mesh;
import cn.lambdalib.pipeline.core.GLBuffer;
import cn.lambdalib.pipeline.core.VAO;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class GraphicPipeline {

    private Map<Material, Multimap<Mesh, Instance>> drawCalls = new HashMap<>();

    public void draw(Material mat, Mesh mesh, Instance instance) {
        if (!drawCalls.containsKey(mat)) {
            drawCalls.put(mat, ArrayListMultimap.create());
        }

        drawCalls.get(mat).put(mesh, instance);
    }

    public void flush() {
        for (Material mat : drawCalls.keySet()) {
            Multimap<Mesh, Instance> thisMatDraws = drawCalls.get(mat);

            glUseProgram(mat.program.getProgramID());
            GLBuffer instanceVBO = GLBuffer.create();
            VAO vao = VAO.create();
            glBindVertexArray(vao.getID());

            // Per-instance data pointer
            glBindBuffer(GL_ARRAY_BUFFER, instanceVBO.getID());

            for (Layout layout : mat.instanceLayouts) {
                glEnableVertexAttribArray(layout.location);
                glVertexAttribPointer(layout.location, layout.attrType.dimension, GL_FLOAT, false,
                        mat.instanceFloats * 4,
                        layout.floatPadding * 4);
                glVertexAttribDivisor(layout.location, 1);
            }

            glBindBuffer(GL_ARRAY_BUFFER, 0);

            for (Mesh mesh : thisMatDraws.keySet()) {
                Collection<Instance> instances = thisMatDraws.get(mesh);

                // Per-vertex data pointer
                glBindBuffer(GL_ARRAY_BUFFER, mesh.getVBO().getID());
                for (Layout layout : mat.vertexLayouts) {
                    glEnableVertexAttribArray(layout.location);
                    glVertexAttribPointer(layout.location, layout.attrType.dimension, GL_FLOAT, false,
                            mat.vertexFloats * 4,
                            layout.floatPadding * 4);
                    glVertexAttribDivisor(layout.location, 0);
                }
                glBindBuffer(GL_ARRAY_BUFFER, 0);

                // Update instance bufffer
                FloatBuffer buffer = BufferUtils.createFloatBuffer(mat.instanceFloats * instances.size());
                for (Instance i : instances) {
                    buffer.put(i.values);
                }
                buffer.flip();

                glBindBuffer(GL_ARRAY_BUFFER, instanceVBO.getID());
                glBufferData(GL_ARRAY_BUFFER, buffer, GL_DYNAMIC_DRAW);
                glBindBuffer(GL_ARRAY_BUFFER, 0);

                // Bind IBO
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mesh.getIBOAll().getID());

                glPointSize(3.0f);
                glDrawElementsInstanced(GL_TRIANGLES, mesh.getIndiceCount(), GL_UNSIGNED_INT, 0, instances.size());

                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            }

            glBindVertexArray(0);
            glUseProgram(0);
        }

        drawCalls.clear();
    }


}
