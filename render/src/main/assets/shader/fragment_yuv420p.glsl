precision highp float;
varying vec2 textureCoordinate;
uniform sampler2D SamplerY;
uniform sampler2D SamplerU;
uniform sampler2D SamplerV;
uniform mat3 colorCvtMat;
uniform vec3 colorOffset;
uniform float alpha;
void main() {
  vec3 yuv, color;
  yuv.x = texture2D(SamplerY, textureCoordinate).r;
  yuv.y = texture2D(SamplerU, textureCoordinate).r;
  yuv.z = texture2D(SamplerV, textureCoordinate).r;
  yuv.xyz = yuv.xyz + colorOffset;
  color = colorCvtMat * yuv;
  gl_FragColor = vec4(color, alpha);
}