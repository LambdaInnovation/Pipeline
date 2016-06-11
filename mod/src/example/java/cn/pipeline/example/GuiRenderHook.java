package cn.pipeline.example;

import cn.lambdalib.pipeline.api.GraphicPipeline;
import cn.lambdalib.pipeline.api.PipelineFactory;
import cn.lambdalib.pipeline.api.ShaderProgram;
import cn.lambdalib.pipeline.core.Utils;
import cn.pipeline.example.ref.RefGeneratedMaterial;
import cn.pipeline.example.ref.RefGeneratedMaterial.InstanceData;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import static cn.pipeline.example.ref.RefGeneratedMaterial.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GuiRenderHook {

    private ShaderProgram program = ShaderProgram.load(
            new ResourceLocation("pipex:shaders/ref.vert"),
            new ResourceLocation("pipex:shaders/ref.frag"));

    private Random rng = new Random();

    private RefGeneratedMaterial mat = new RefGeneratedMaterial(program.getProgramID());
    private Mesh mesh = newMesh(
            new Mesh.Vertex[]{
                newVertex(new Vector3f(0, 0, 0), new Vector2f(0, 1)),
                newVertex(new Vector3f(0, 1, 0), new Vector2f(0, 0)),
                newVertex(new Vector3f(1, 1, 0), new Vector2f(1, 0)),
                newVertex(new Vector3f(1, 0, 0), new Vector2f(1, 1))
            },
            new byte[] { 0, 1, 2, 0, 2, 3 }
    );

    private final ResourceLocation texture = new ResourceLocation("pipex:textures/test.png");

    private GraphicPipeline pipeline = PipelineFactory.create();

    private List<InstanceData> instances = new ArrayList<>();

    {
        glUseProgram(program.getProgramID());

        glUniform1i(glGetUniformLocation(program.getProgramID(), "u_texture"), 0);

        glUseProgram(0);


        System.out.println("GuiRenderHook initialized.");
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent evt) {
        if (evt.type == ElementType.CROSSHAIRS) {

            if (Math.random() < 0.05) {
                instances.clear();
                for (int i = 0; i < rng.nextInt(500); ++i) {
                    instances.add(newInstance(new Vector3f(rng.nextFloat(), rng.nextFloat(), rng.nextFloat()),
                            new Vector3f(rng.nextFloat()*2-1, rng.nextFloat()*2-1, 0), 0.25f + rng.nextFloat() * 0.25f));
                }
            }

            for (InstanceData i : instances) {
                pipeline.draw(mat, mesh, i);
            }

            glDisable(GL_DEPTH_TEST);
            glDisable(GL_CULL_FACE);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

            pipeline.flush();

            glEnable(GL_DEPTH_TEST);
            glEnable(GL_CULL_FACE);
        }
    }

}
