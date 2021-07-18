#version 150
#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
	vec4 tex = texture(Sampler0, texCoord0);
	vec4 color = tex * ColorModulator;
	if (tex.a > 0.9) {
		color = color * vertexColor;
	}
	color.a = 1.0;
	fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
