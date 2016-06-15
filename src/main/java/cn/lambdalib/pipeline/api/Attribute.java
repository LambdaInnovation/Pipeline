package cn.lambdalib.pipeline.api;

import cn.lambdalib.pipeline.api.Material.Layout;

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
        Vec2, Vec3, Float
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
