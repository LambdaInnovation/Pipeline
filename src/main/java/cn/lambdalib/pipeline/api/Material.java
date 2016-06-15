package cn.lambdalib.pipeline.api;

import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

import static cn.lambdalib.pipeline.core.Utils.*;

public class Material {

    public static Material load(ShaderProgram program, LayoutMapping mapping) {
        throw notImplemented();
    }

    private Material() {}

    public Vertex newVertex(Attribute ...attrs) {
        throw notImplemented();
    }

    public Instance newInstance(Attribute ...attrs) {
        throw notImplemented();
    }

    public enum LayoutType {
        VERTEX, INSTANCE
    }

    public class Layout {

        public final int location;
        public final String name;
        public final LayoutType type;

        private Layout(int location, String name, LayoutType type) {
            this.location = location;
            this.name = name;
            this.type = type;
        }

    }

    public class Vertex {

        private final float[] values;

        private Vertex(float[] values) {
            this.values = values;
        }

        Material _enclosingMaterial() {
            return Material.this;
        }

    }

    public class Instance {

        private final float[] values;

        private Instance(float[] values) {
            this.values = values;
        }

        Material _enclosingMaterial() {
            return Material.this;
        }

    }


    public static class LayoutMapping {

        final Map<String, LayoutType> mappings = new HashMap<>();

        public static LayoutMapping create(Pair<String, LayoutType>... input) {
            LayoutMapping ret = new LayoutMapping();
            for (Pair<String, LayoutType> p : input) {
                ret.mappings.put(p.getLeft(), p.getRight());
            }
            return ret;
        }

    }

    public class Mesh {

        private Vertex[] vertices = new Vertex[0];

        public void setVertices(Vertex ...vertices) {

        }

        public void setIndices(int[] indices) {

        }

        public void setSubIndices(int subMesh, int[] indices) {

        }

        Material _enclosingMaterial() {
            return Material.this;
        }

    }

}
