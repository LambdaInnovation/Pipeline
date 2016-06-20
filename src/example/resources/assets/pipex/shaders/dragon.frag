#version 330 core

uniform sampler2D tree_tex;

in vec2 v_uv;

void main() {
    vec2 uv = v_uv;
    uv.y = 1 - uv.y;
	gl_FragColor = texture(tree_tex, uv);
}
