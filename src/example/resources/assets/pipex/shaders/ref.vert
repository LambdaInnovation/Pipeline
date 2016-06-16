#version 330 core

uniform float aspect;

// Per-vertex
layout (location = 0) in vec3 position;
layout (location = 1) in vec2 uv;
layout (location = 2) in vec3 color;

// Per-instance
layout (location = 3) in vec3 offset;
layout (location = 4) in float scale;

out vec3 v_color;
out vec2 v_uv;

void main() {
    v_color = color;
    v_uv = uv;

    vec3 pos = position;
    pos *= scale;
    pos.x /= 1.6;

    gl_Position = vec4(pos + offset, 0.3);
}
