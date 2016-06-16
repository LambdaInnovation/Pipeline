#version 330 core

uniform sampler2D u_texture;

in vec3 v_color;
in vec2 v_uv;

out vec4 outColor;

void main() {
    outColor = vec4(v_color, 0.6) * texture(u_texture, v_uv);
}
