// 默认滤镜
attribute vec4 aPosition;           // 图像顶点坐标
attribute vec4 aTextureCoord;       // 图像纹理坐标
varying vec2 textureCoordinate;     // 图像纹理坐标
void main() {
    gl_Position = aPosition;
    textureCoordinate = aTextureCoord.xy;
}