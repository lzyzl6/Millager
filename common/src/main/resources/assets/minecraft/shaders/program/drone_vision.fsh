#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 OutSize;
uniform float Time;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);
    vec3 tinted = clamp(color.rgb * vec3(0.5, 0.75, 1.5), 0.0, 1.0);
    float scanline = sin((texCoord.y * OutSize.y * 1.2) + (Time * 0.1)) * 0.5 + 0.5;
    vec3 finalColor = tinted * (1.0 - vec3(scanline * 0.06));
    fragColor = vec4(clamp(finalColor, 0.0, 1.0), 1.0);
}
