package cn.pipeline.api.mc;

import cn.pipeline.core.RenderEventDispatch;
import org.lwjgl.util.vector.Matrix4f;

/**
 * Common utils used when rendering entities.
 */
public class EntityRenderUtils {

    /**
     * <summary>
     *     Return the projection matrix of current frame.
     * </summary>
     */
    public static Matrix4f getProjectionMatrix() {
        return RenderEventDispatch.projMatrix;
    }

    /**
     * <summary>
     *     Return the world view matrix of current frame, which transforms
     *     coordinates from player view space to camera space.
     * </summary>
     */
    public static Matrix4f getPlayerViewMatrix() {
        return RenderEventDispatch.playerViewMatrix;
    }

    /**
     * <summary>
     *     Returns the PVP(Player View Projection) matrix of current frame.
     * </summary>
     * <p>
     *     The return value is Projection * PlayerView,
     *      which transforms coordinates from player view space to NDC.
     * </p>
     */
    public static Matrix4f getPVPMatrix() {
        return RenderEventDispatch.pvpMatrix;
    }

}
