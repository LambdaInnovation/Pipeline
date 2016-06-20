#version 330 core

uniform mat4 pvp_matrix;

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 uv;

layout (location = 2) in vec3 offset;

out vec2 v_uv;

void main() {
	gl_Position = pvp_matrix * vec4(position * 0.5 + offset, 1.0);
	v_uv = uv;
}
