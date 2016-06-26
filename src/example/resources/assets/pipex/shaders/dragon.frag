#version 330 core

uniform sampler2D tree_tex;

in vec2 v_uv;

void main() {
    vec2 uv = v_uv;
    uv.y = 1 - uv.y;
	gl_FragColor = vec4(texture(tree_tex, uv).rgb, 0.8);
}
