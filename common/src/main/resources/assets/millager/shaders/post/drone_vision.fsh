#version 150

uniform sampler2D InSampler;
uniform vec2 OutSize;
uniform float Time;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = texture(InSampler, texCoord);

    vec3 glassTint = vec3(0.5, 0.75, 1.5);
    vec3 tinted = color.rgb * glassTint;

    tinted = clamp(tinted, 0.0, 1.0);

    float scanlineFreq = 1.2;
    float scanlineSpeed = 0.1;
    float scanlineVal = sin((texCoord.y * OutSize.y * scanlineFreq) + (Time * scanlineSpeed)) * 0.5 + 0.5;

    float scanlineStrength = 0.06;
    vec3 finalRGB = tinted * (1.0 - (vec3(scanlineVal) * scanlineStrength));

    fragColor = vec4(clamp(finalRGB, 0.0, 1.0), color.a);
}