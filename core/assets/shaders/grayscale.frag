#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

void main() {
        vec4 color = texture2D(u_texture, v_texCoords).rgba;
        float gray = 0.299 * color.r + 0.587 * color.g + 0.114 * color.b;
        vec3 grayscale = vec3(gray);

        gl_FragColor = vec4(grayscale, color.a);
}