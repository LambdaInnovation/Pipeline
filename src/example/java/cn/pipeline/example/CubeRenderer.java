package cn.pipeline.example;

import cn.lambdalib.pipeline.api.GraphicPipeline;
import cn.lambdalib.pipeline.api.Material;
import cn.lambdalib.pipeline.api.Material.*;
import cn.lambdalib.pipeline.api.ShaderProgram;
import cn.lambdalib.pipeline.api.mc.MCPipeline;
import cn.lambdalib.pipeline.api.mc.event.RenderAllEntityEvent;
import com.google.common.primitives.Ints;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.*;

import static org.apache.commons.lang3.tuple.Pair.of;
import static cn.lambdalib.pipeline.api.Attribute.attr;
import static org.lwjgl.opengl.GL20.*;

/**
 * An example for manually injecting into entity rendering pipeline.
 */
public class CubeRenderer {

    public static boolean activated = false;

    private final Random rand = new Random();

    private final Material mat =
            Material.load(
                    ShaderProgram.load(
                            new ResourceLocation("pipex:shaders/cube.vert"),
                            new ResourceLocation("pipex:shaders/cube.frag")
                    ),
                    LayoutMapping.create(
                            of("position", LayoutType.VERTEX),
                            of("offset", LayoutType.INSTANCE),
                            of("size", LayoutType.INSTANCE),
                            of("color", LayoutType.INSTANCE)
                    )
            );

    private final Mesh mesh = mat.newMesh(MeshType.STATIC);

    private final Layout
        l_position = mat.getLayout("position"),
        l_offset = mat.getLayout("offset"),
        l_size = mat.getLayout("size"),
        l_color = mat.getLayout("color");

    private final int
        loc_pvp_matrix = glGetUniformLocation(mat.getProgram().getProgramID(), "pvp_matrix"),
        loc_player_pos = glGetUniformLocation(mat.getProgram().getProgramID(), "player_pos");

    {
        final float s = 0.5f;
        mesh.setVertices(
                vert(-s, -s,  s),
                vert(-s, -s, -s),
                vert( s, -s, -s),
                vert( s, -s,  s),
                vert(-s,  s,  s),
                vert(-s,  s, -s),
                vert( s,  s, -s),
                vert( s,  s,  s)
        );
        List<Integer> is = new ArrayList<>();
        addFace(is, 3, 2, 1, 0);
        addFace(is, 6, 2, 3, 7);
        addFace(is, 7, 3, 0, 4);
        addFace(is, 4, 0, 1, 5);
        addFace(is, 5, 1, 2, 6);
        addFace(is, 4, 5, 6, 7);
        mesh.setIndices(Ints.toArray(is));
    }

    private class Cube {

        float posX, posY, posZ;
        float r, g, b;
        float size;
        long timeOffset = System.currentTimeMillis();

        public Cube() {
            posX = randCenter((float) player().posX, 100);
            posY = randCenter((float) player().posY, 100);
            posZ = randCenter((float) player().posZ, 100);
            size = randCenter(2, 2f);
            r = rand.nextFloat();
            g = rand.nextFloat();
            b = rand.nextFloat();
        }

        Instance toInstance() {
            float time = (System.currentTimeMillis() - timeOffset) / 1000.0f;
            float ry = posY + (float) Math.sin(time) * 2;

            return mat.newInstance(
                    attr(l_offset, posX, ry, posZ),
                    attr(l_size, size + (float) Math.cos(time) * 0.5f),
                    attr(l_color, r, g, b)
            );
        }

    }

    private final Set<Cube> cubes = new HashSet<>();

    private float randCenter(float init, float range) {
        return init + randCenter() * range;
    }

    private float randCenter() { return rand.nextFloat() - 0.5f; }

    private EntityPlayer player() {
        return Minecraft.getMinecraft().thePlayer;
    }

    private Vertex vert(float x, float y, float z) {
        return mat.newVertex(attr(l_position, x, y, z));
    }

    private void addFace(List<Integer> arr, int i0, int i1, int i2, int i3) {
        arr.add(i2); arr.add(i1); arr.add(i0); arr.add(i3); arr.add(i2); arr.add(i0);
    }

    @SubscribeEvent
    public void renderEntity(RenderAllEntityEvent evt) {
        if (activated) {
            GraphicPipeline pipeline = MCPipeline.entity();
            EntityPlayer player = player();

            // Remove out-of-range cubes
            Iterator<Cube> iter = cubes.iterator();
            while (iter.hasNext()) {
                Cube c = iter.next();
                if (player.getDistanceSq(c.posX, c.posY, c.posZ) > 10000) {
                    iter.remove();
                }
            }

            // Spawn cubes
            while (cubes.size() < 1000) {
                cubes.add(new Cube());
            }

            FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
            evt.pvpMatrix().store(buffer);
            buffer.flip();

            glUseProgram(mat.getProgram().getProgramID());

            glUniformMatrix4(loc_pvp_matrix, false, buffer);

            glUniform3f(loc_player_pos,
                    (float) RenderManager.renderPosX,
                    (float) RenderManager.renderPosY,
                    (float) RenderManager.renderPosZ);
            glUseProgram(0);

            for (Cube c : cubes) {
                pipeline.draw(mat, mesh, c.toInstance());
            }
        }
    }

}
