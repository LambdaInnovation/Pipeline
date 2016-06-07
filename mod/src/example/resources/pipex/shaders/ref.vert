#version 330 core

uniform mat4 wvpMatrix;

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 uv;
layout (location = 2) in vec3 color;
layout (location = 3) in vec3 offset;

out vec3 v_color;
out vec2 v_uv;

void main() {
    v_color = color;
    v_uv = uv;

    gl_Position = wvpMatrix * vec4(position, 1);
}
