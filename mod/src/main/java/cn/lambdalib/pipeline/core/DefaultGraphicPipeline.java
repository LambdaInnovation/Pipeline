package cn.lambdalib.pipeline.core;

import cn.lambdalib.pipeline.api.GraphicPipeline;
import cn.lambdalib.pipeline.api.IInstanceData;
import cn.lambdalib.pipeline.api.IMaterial;
import cn.lambdalib.pipeline.api.IMesh;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.function.Supplier;

import static cn.lambdalib.pipeline.core.Utils.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL33.*;

public class DefaultGraphicPipeline implements GraphicPipeline {

    private Map<IMaterial, Multimap<IMesh, IInstanceData>> drawCalls = new HashMap<>();

    private Supplier<GLBuffer> instanceBuffer = Lazy.withInitializer(GLBuffer::create);
    private Supplier<VAO> vao = Lazy.withInitializer(VAO::create);

    @Override
    public void draw(IMaterial material, IMesh mesh, IInstanceData instanceData) {
        if (!drawCalls.containsKey(material)) {
            drawCalls.put(material, ArrayListMultimap.create());
        }

        drawCalls.get(material).put(mesh, instanceData);
    }

    @Override
    public void drawNow(IMaterial material, IMesh mesh, IInstanceData instanceData) {
        throw notImplemented();
    }

    @Override
    public void flush() {
        for (IMaterial mat : drawCalls.keySet()) {
            Multimap<IMesh, IInstanceData> subCalls = drawCalls.get(mat);
            for (IMesh mesh : subCalls.keySet()) {
                Collection<IInstanceData> instances = subCalls.get(mesh);
                glUseProgram(mat.getProgramID());

                // Update instance buffer
                FloatBuffer buffer = BufferUtils.createFloatBuffer(instances.size() * mat.getFloatsPerInstance());
                for (IInstanceData data : instances) {
                    data.upload(buffer);
                }
                buffer.flip();

                glBindVertexArray(vao.get().getID());

                // Begin draw
                int vibo = instanceBuffer.get().getID();

                glBindBuffer(GL_ARRAY_BUFFER, vibo);
                glBufferData(GL_ARRAY_BUFFER, buffer, GL_DYNAMIC_DRAW);

                // Prepare vertex layouts
                mat.beforeDrawCall(mesh.getVBO(), vibo);

                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mesh.getIBO());

                // Draw
                glDrawElementsInstanced(GL_TRIANGLES, mesh.getIndicesCount(), GL_UNSIGNED_BYTE, 0, instances.size());

                // Cleanup
                glBindVertexArray(0);
                glUseProgram(0);
            }
        }

        drawCalls.clear();
    }

}
