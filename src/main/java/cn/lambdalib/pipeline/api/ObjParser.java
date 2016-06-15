package cn.lambdalib.pipeline.api;

import cn.lambdalib.pipeline.api.ObjModel.Vertex;
import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
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
 * Parses obj model file into the runtime {@link IMesh}.
 */
public class ObjParser {

    public static ObjModel parse(ResourceLocation res) {
        return parse(new InputStreamReader(getResourceStream(res)));
    }

    private static ObjModel parse(Reader rdr0) {
        List<Vector3f> vs = new ArrayList<>();
        List<Vector2f> vts = new ArrayList<>();
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

                    case "g":
                        currentGroup = scanner.next();

                        break;

                    case "f":
                        scanner.useDelimiter("[ /]");
                        ObjFace of = new ObjFace(
                                scanner.nextInt()-1, scanner.nextInt()-1, scanner.nextInt()-1,
                                scanner.nextInt()-1, scanner.nextInt()-1, scanner.nextInt()-1);

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

        GenContext ctx = new GenContext(vs, vts, generated, vertices);

        for (String group : faces.keySet()) {
            Collection<Integer> list = genFaces.get(group);
            for (ObjFace face : faces.get(group)) {
                int i0 = genIndex(ctx, new VertexIdt(face.v0, face.vt0));
                int i1 = genIndex(ctx, new VertexIdt(face.v1, face.vt1));
                int i2 = genIndex(ctx, new VertexIdt(face.v2, face.vt2));

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

        return new ObjModel(vertices.toArray(new Vertex[vertices.size()]), resultFaces);
    }

    private static int genIndex(GenContext ctx, VertexIdt idt) {
        int idx;
        if (ctx.generated.containsKey(idt)) {
            idx = ctx.generated.get(idt);
        } else {
            idx = ctx.vertices.size();
            ctx.generated.put(idt, ctx.vertices.size());
            ctx.vertices.add(new Vertex(ctx.vs.get(idt.vert), ctx.vts.get(idt.tex)));
        }

        return idx;
    }

    private static class GenContext {
        final List<Vector3f> vs;
        final List<Vector2f> vts;
        final Map<VertexIdt, Integer> generated;
        final List<Vertex> vertices;

        public GenContext(List<Vector3f> vs, List<Vector2f> vts,
                          Map<VertexIdt, Integer> generated, List<Vertex> vertices) {
            this.vs = vs;
            this.vts = vts;
            this.generated = generated;
            this.vertices = vertices;
        }
    }

    private static class ObjFace {
        final int v0, v1, v2;
        final int vt0, vt1, vt2;

        public ObjFace(int v0, int vt0, int v1, int vt1, int v2, int vt2) {
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
            this.vt0 = vt0;
            this.vt1 = vt1;
            this.vt2 = vt2;
        }
    }

    private static class VertexIdt {
        final int vert, tex;

        public VertexIdt(int vert, int tex) {
            this.vert = vert;
            this.tex = tex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            VertexIdt vertexIdt = (VertexIdt) o;

            if (vert != vertexIdt.vert) return false;
            return tex == vertexIdt.tex;

        }

        @Override
        public int hashCode() {
            int result = vert;
            result = 31 * result + tex;
            return result;
        }
    }

}
