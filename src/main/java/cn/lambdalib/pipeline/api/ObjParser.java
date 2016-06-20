package cn.lambdalib.pipeline.api;

import cn.lambdalib.pipeline.api.Material.Layout;
import cn.lambdalib.pipeline.api.Material.Mesh;
import cn.lambdalib.pipeline.api.Material.Vertex;
import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.Map.Entry;

import static cn.lambdalib.pipeline.core.Utils.getResourceStream;
import static cn.lambdalib.pipeline.core.Utils.log;

/**
 * Parses obj model file into the runtime {@link Material.Mesh}.
 */
public class ObjParser {

    public enum VertexAttr { Position, UV, Normal }

    /**
     * @param res The location of the texture
     * @param material The material of the mesh.
     * @param targetMesh The mesh to store the parsed mesh data in. The previous data will be overwritten.
     * @param vertexMapping A map from OBJ vertex attribute to sub mesh index.
     * @param groupMapping A map from face group to sub mesh index.
     * @return
     */
    public static void parse(ResourceLocation res,
                                 Material material, Mesh targetMesh,
                                 Map<VertexAttr, Layout> vertexMapping,
                                 Map<String, Integer> groupMapping) {
        parse(new InputStreamReader(getResourceStream(res)),
                new ObjParseTarget(material, targetMesh, vertexMapping, groupMapping));
    }

    private static void parse(Reader rdr0, ObjParseTarget t) {
        List<Vector3f> vs = new ArrayList<>();
        List<Vector2f> vts = new ArrayList<>();
        List<Vector3f> vns = new ArrayList<>();
        Multimap<String, ObjFace> faces = HashMultimap.create();

        BufferedReader rdr = new BufferedReader(rdr0);

        // Reads obj model info.
        try {
            String currentGroup = "Default";

            String ln;
            while ((ln = rdr.readLine()) != null) {
                ln = ln.trim();
                if (ln.isEmpty() || ln.charAt(0) == '#') {
                    continue;
                }

                Scanner scanner = new Scanner(ln);

                String token = scanner.next();
                switch (token) {
                    case "v":
                        vs.add(new Vector3f(scanner.nextFloat(), scanner.nextFloat(), scanner.nextFloat()));

                        break;

                    case "vt":
                        vts.add(new Vector2f(scanner.nextFloat(), scanner.nextFloat()));

                        break;

                    case "vn":
                        vns.add(new Vector3f(scanner.nextFloat(), scanner.nextFloat(), scanner.nextFloat()));

                        break;

                    case "g":
                        currentGroup = scanner.next();

                        break;

                    case "f":
                        scanner.useDelimiter(" ");
                        ObjFace of = new ObjFace(
                                new VertexIdt(scanner.next()),
                                new VertexIdt(scanner.next()),
                                new VertexIdt(scanner.next()));

                        faces.put(currentGroup,of);

                        break;

                    case "usemtl":

                        break;

                    default:
                        log.info("Unknown token " + token);

                        break;
                }
            }

            rdr.close();
        } catch (IOException ex) {
            Throwables.propagate(ex);
        } finally { // ...
            try {
                rdr.close();
            } catch (IOException ex) {
                Throwables.propagate(ex);
            }
        }

        // Convert into intermediate format
        List<Vertex> vertices = new ArrayList<>();
        Map<VertexIdt, Integer> generated = new HashMap<>();
        ArrayListMultimap<String, Integer> genFaces = ArrayListMultimap.create();

        GenContext ctx = new GenContext(vs, vts, vns, generated, vertices, t);

        for (String group : faces.keySet()) {
            Collection<Integer> list = genFaces.get(group);
            for (ObjFace face : faces.get(group)) {
                int i0 = genIndex(ctx, face.f0);
                int i1 = genIndex(ctx, face.f1);
                int i2 = genIndex(ctx, face.f2);

                list.add(i0);
                list.add(i1);
                list.add(i2);
            }
        }

        Map<String, int[]> resultFaces = new HashMap<>();
        for (String group : genFaces.keySet()) {
            List<Integer> list = genFaces.get(group);
            resultFaces.put(group, Ints.toArray(list));
        }

        Vertex[] verticesArr = new Vertex[vertices.size()];
        vertices.toArray(verticesArr);
        t.mesh.setVertices(verticesArr);

        for (String group : resultFaces.keySet()) {
            if (t.groupMapping.containsKey(group)) {
                int submesh = t.groupMapping.get(group);
                t.mesh.setSubIndices(submesh, resultFaces.get(group));
            }
        }
    }

    private static int genIndex(GenContext ctx, VertexIdt idt) {
        int idx;
        if (ctx.generated.containsKey(idt)) {
            idx = ctx.generated.get(idt);
        } else {
            idx = ctx.vertices.size();
            ctx.generated.put(idt, ctx.vertices.size());

            Vertex v = ctx.t.material.newVertex();
            for (Entry<VertexAttr, Layout> entry : ctx.t.vertMapping.entrySet()) {
                VertexAttr attr = entry.getKey();
                Layout layout = entry.getValue();
                switch (attr) {
                    case Position: if (idt.vertIndex != -1) {
                        Vector3f pos = ctx.vs.get(idt.vertIndex);
                        v.setVec3(layout, pos.x, pos.y, pos.z);
                    } break;
                    case UV: if (idt.uvIndex != -1) {
                        Vector2f uv  = ctx.vts.get(idt.uvIndex);
                        v.setVec2(layout, uv.x, uv.y);
                    } break;
                    case Normal: if (idt.normalIndex != -1) {
                        Vector3f n = ctx.vns.get(idt.normalIndex);
                        v.setVec3(layout, n.x, n.y, n.z);
                    } break;
                }
            }

            ctx.vertices.add(v);
        }

        return idx;
    }

    private static class ObjParseTarget {

        public final Material material;
        public final Mesh mesh;
        public final Map<VertexAttr, Layout> vertMapping;
        public final Map<String, Integer> groupMapping;

        public ObjParseTarget(Material material, Mesh mesh, Map<VertexAttr, Layout> vertMapping, Map<String, Integer> groupMapping) {
            this.material = material;
            this.mesh = mesh;
            this.vertMapping = vertMapping;
            this.groupMapping = groupMapping;
        }
    }

    private static class GenContext {
        final List<Vector3f> vs;
        final List<Vector2f> vts;
        final List<Vector3f> vns;

        final Map<VertexIdt, Integer> generated;
        final List<Vertex> vertices;
        final ObjParseTarget t;

        public GenContext(List<Vector3f> vs, List<Vector2f> vts,
                          List<Vector3f> vns,
                          Map<VertexIdt, Integer> generated,
                          List<Vertex> vertices, ObjParseTarget t) {
            this.vs = vs;
            this.vts = vts;
            this.vns = vns;
            this.generated = generated;
            this.vertices = vertices;
            this.t = t;
        }
    }

    private static class ObjFace {
        final VertexIdt f0, f1, f2;

        ObjFace(VertexIdt f0, VertexIdt f1, VertexIdt f2) {
            this.f0 = f0;
            this.f1 = f1;
            this.f2 = f2;
        }
    }

    private static class VertexIdt {
        VertexIdt(String input) {
            Scanner scanner = new Scanner(input);
            scanner.useDelimiter("/");

            vertIndex = scanner.nextInt() - 1;

            if (!scanner.hasNext()) return;

            String s = scanner.next();
            if (!s.isEmpty()) {
                uvIndex = Integer.parseInt(s) - 1;
            }

            if (!scanner.hasNextInt()) return;

            normalIndex = scanner.nextInt() - 1;
        }

        int vertIndex = -1;
        int uvIndex = -1;
        int normalIndex = -1;


        @Override
        public boolean equals(Object o) {
            VertexIdt vertex = (VertexIdt) o;
            if (vertIndex != vertex.vertIndex) return false;
            if (uvIndex != vertex.uvIndex) return false;
            return normalIndex == vertex.normalIndex;
        }

        @Override
        public int hashCode() {
            int result = vertIndex;
            result = 31 * result + uvIndex;
            result = 31 * result + normalIndex;
            return result;
        }
    }

}
