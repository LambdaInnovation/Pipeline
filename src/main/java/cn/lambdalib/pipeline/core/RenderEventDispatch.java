package cn.lambdalib.pipeline.core;

import cn.lambdalib.pipeline.api.GraphicPipeline;
import cn.lambdalib.pipeline.api.mc.event.RenderAllEntityEvent;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_MODELVIEW_MATRIX;
import static org.lwjgl.opengl.GL11.GL_PROJECTION_MATRIX;
import static org.lwjgl.opengl.GL11.glGetFloat;

/**
 * Internal class don't touch or use!
 */
public class RenderEventDispatch {

    private static final FloatBuffer matrixAcqBuffer = BufferUtils.createFloatBuffer(16);

    public static final Matrix4f
        projMatrix = new Matrix4f(),
        playerViewMatrix = new Matrix4f(),
        pvpMatrix = new Matrix4f();

    public static final GraphicPipeline entityPipeline = new GraphicPipeline();

    // Following method signatures used by ASM

    public static void beginRenderEntities(float partialTicks) {
        int pass = MinecraftForgeClient.getRenderPass();

        if (pass == 0) {
            // Update matrices
            acquireMatrix(GL_MODELVIEW_MATRIX, playerViewMatrix);
            acquireMatrix(GL_PROJECTION_MATRIX, projMatrix);
            Matrix4f.mul(projMatrix, playerViewMatrix, pvpMatrix);
        }

        MinecraftForge.EVENT_BUS.post(new RenderAllEntityEvent(pass, partialTicks));
    }

    public static void endRenderEntities(float partialTicks) {
        entityPipeline.flush();
    }

    //

    private static void acquireMatrix(int matrixType, Matrix4f dst) {
        matrixAcqBuffer.clear();
        glGetFloat(matrixType, matrixAcqBuffer);
        dst.load(matrixAcqBuffer);
    }

}
