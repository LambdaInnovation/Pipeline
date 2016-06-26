package cn.pipeline.example;

import cn.pipeline.api.GraphicPipeline;
import cn.pipeline.api.Material;
import cn.pipeline.api.Material.*;
import cn.pipeline.api.ObjParser;
import cn.pipeline.api.ObjParser.VertexAttr;
import cn.pipeline.api.ShaderProgram;
import cn.pipeline.api.mc.EntityRenderUtils;
import cn.pipeline.api.mc.MCPipeline;
import cn.pipeline.api.state.StateContext;
import cn.pipeline.api.state.StateContext.BlendFunction;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.tuple.Pair.of;

public class DragonRenderer {

    public static void init() {
        GameRegistry.registerBlock(new DragonBlock(), "DragonBlock");
        GameRegistry.registerTileEntity(DragonTileEntity.class, "DragonTile");

        // Note that normally rendering registry should be done in proxy, but this demo is client-only so registering here is fine.
        ClientRegistry.bindTileEntitySpecialRenderer(DragonTileEntity.class, new TileEntitySpecialRenderer() {

            final Material material = Material.load(
                    ShaderProgram.load(
                            new ResourceLocation("pipex:shaders/dragon.vert"),
                            new ResourceLocation("pipex:shaders/dragon.frag")
                    ),
                    LayoutMapping.create(
                            of("position", LayoutType.VERTEX),
                            of("uv", LayoutType.VERTEX),
                            of("offset", LayoutType.INSTANCE)
                    )
            );

            final Mesh mesh = material.newMesh(MeshType.STATIC);

            final Layout l_offset = material.getLayout("offset");

            final ResourceLocation tex = new ResourceLocation("pipex:textures/dragon.png");

            {
                Map<VertexAttr, Layout> vertexMapping = new HashMap<>();
                vertexMapping.put(VertexAttr.Position, material.getLayout("position"));
                vertexMapping.put(VertexAttr.UV, material.getLayout("uv"));

                Map<String, Integer> groupMapping = new HashMap<>();
                groupMapping.put("Cube1", 0);
                groupMapping.put("Cube2", 1);
                groupMapping.put("Cube3", 2);

                long time0 = System.currentTimeMillis();
                ObjParser.parse(new ResourceLocation("pipex:models/dragon.obj"), material, mesh, vertexMapping, groupMapping);
                System.out.println("Took " + (System.currentTimeMillis() - time0) + " ms parsing obj file.");

                StateContext ctx = material.stateContext();
                ctx.setTexBinding2D(0, tex);
                ctx.setBlendEnabled(true);
                ctx.setBlendFunc(BlendFunction.SrcAlpha, BlendFunction.One);
            }

            @Override
            public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
                GraphicPipeline pipeline = MCPipeline.entity();

                material.setUniforms(material.newUniformBlock()
                     .setMat4("pvp_matrix", EntityRenderUtils.getPVPMatrix())
                );

                pipeline.draw(material, mesh,
                        material.newInstance().setVec3(l_offset, (float) x, (float) y, (float) z));
            }
        });
    }

    public static class DragonBlock extends Block implements ITileEntityProvider {

        public DragonBlock() {
            super(net.minecraft.block.material.Material.wood);
            setCreativeTab(CreativeTabs.tabMisc);
        }

        @Override
        public TileEntity createNewTileEntity(World world, int meta) {
            return new DragonTileEntity();
        }
    }

    public static class DragonTileEntity extends TileEntity {

        @Override
        public AxisAlignedBB getRenderBoundingBox() {
            return INFINITE_EXTENT_AABB;
        }
    }

}
