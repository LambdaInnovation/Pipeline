package cn.lambdalib.pipeline.api;

import cn.lambdalib.pipeline.api.Material.Layout;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL20.GL_FLOAT_VEC2;
import static org.lwjgl.opengl.GL20.GL_FLOAT_VEC3;

public class Attribute {

    /**
     * Creates an attribute storing a float value.
     */
    public static Attribute attr(Layout layout, float x) {
        return new Attribute(layout, AttributeType.Float, new float[] { x });
    }

    /**
     * Creates an attribute storing a vec2 value.
     */
    public static Attribute attr(Layout layout, float x, float y) {
        return new Attribute(layout, AttributeType.Vec2, new float[] { x, y });
    }

    /**
     * Creates an attribute storing a vec3 value.
     */
    public static Attribute attr(Layout layout, float x, float y, float z) {
        return new Attribute(layout, AttributeType.Vec3, new float[] { x, y, z });
    }

    public enum AttributeType {
        Float(1), Vec2(2), Vec3(3);

        public final int dimension;

        public static AttributeType fromGL(int type) {
            switch (type) {
                case GL_FLOAT: return Float;
                case GL_FLOAT_VEC2: return Vec2;
                case GL_FLOAT_VEC3: return Vec3;
                default: throw new IllegalArgumentException("type " + type + " is not supported");
            }
        }

        AttributeType(int dimension) {
            this.dimension = dimension;
        }
    }

    public final Layout layout;
    public final AttributeType attrType;
    public final float[] value;

    private Attribute(Layout layout, AttributeType attrType, float[] value) {
        this.layout = layout;
        this.attrType = attrType;
        this.value = value;
    }

}
