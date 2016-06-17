package cn.pipeline.example;

import cn.lambdalib.pipeline.api.Attribute.AttributeType;
import cn.lambdalib.pipeline.api.GraphicPipeline;
import cn.lambdalib.pipeline.api.Material;
import cn.lambdalib.pipeline.api.Material.*;
import cn.lambdalib.pipeline.api.ShaderProgram;
import cn.lambdalib.pipeline.core.Utils;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.apache.commons.lang3.tuple.Pair.of;
import static cn.lambdalib.pipeline.api.Attribute.attr;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class GuiRenderHook {

    private final GraphicPipeline pipeline = new GraphicPipeline();

    private final Material mat = Material.load(
            ShaderProgram.load(
                    Utils.toString(new ResourceLocation("pipex:shaders/ref.vert")),
                    Utils.toString(new ResourceLocation("pipex:shaders/ref.frag"))),
            LayoutMapping.create(
                    of("position", LayoutType.VERTEX),
                    of("uv", LayoutType.VERTEX),
                    of("color", LayoutType.VERTEX),

                    of("offset", LayoutType.INSTANCE),
                    of("scale", LayoutType.INSTANCE)
            )
    );

    private final Mesh mesh = mat.newMesh(MeshType.STATIC);
    private final Layout pos = mat.getLayout("position"),
                    uv  = mat.getLayout("uv"),
                    color = mat.getLayout("color"),
                    offset = mat.getLayout("offset"),
                    scale = mat.getLayout("scale");

    private final List<Instance> instances = new ArrayList<>();

    private final Random rand = new Random();

    private final ResourceLocation texture = new ResourceLocation("pipex:textures/test.png");

    public GuiRenderHook() {
        mesh.setVertices(
                vert(0,   0,   0, 0, 1, 1, 1, 1),
                vert(0,   .5f, 0, 0, 0, 1, 0, 1),
                vert(.5f, .5f, 0, 1, 0, 1, 0, 0),
                vert(.5f, 0,   0, 1, 1, 0, 0, 1)
        );

        mesh.setSubIndices(0, new int[] { 2, 1, 0, 3, 2, 0 });
        mesh.setSubIndices(1, new int[] { 2, 1, 0 });

        for (int i = 0; i < 1000; ++i) {
            instances.add(mat.newInstance(
                    attr(offset, rand.nextFloat() * 2 - 1, rand.nextFloat() * 2 - 1, 0),
                    attr(scale, rand.nextFloat() * 0.5f)
            ));
        }

    }

    private Vertex vert(float x, float y, float z, float u, float v, float r, float g, float b) {
        return mat.newVertex(attr(pos, x, y, z), attr(uv, u, v), attr(color, r, g, b));
    }

    @SubscribeEvent
    public void renderOverlay(RenderGameOverlayEvent evt) {
        if (evt.type == ElementType.CROSSHAIRS) {
            glEnable(GL_BLEND);
            glUseProgram(mat.getProgram().getProgramID());
            glUniform1f(glGetUniformLocation(mat.getProgram().getProgramID(), "aspect"),
                    evt.resolution.getScaledWidth() / evt.resolution.getScaledHeight());
            glUseProgram(0);

            Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
            for (Instance i : instances) {
                pipeline.draw(mat, mesh, i, 0);
            }
            pipeline.flush();

            evt.setCanceled(true);
        }
    }

}
