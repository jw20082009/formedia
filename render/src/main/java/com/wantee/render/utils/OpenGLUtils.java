package com.wantee.render.utils;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.wantee.render.Rotation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class OpenGLUtils {

    /**
     * 创建OES 类型的Texture
     */
    public static int createOESTexture() {
        return createTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
    }

    /**
     * 创建Texture对象
     */
    public static int createTexture(int textureType) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureId = textures[0];
        GLES20.glBindTexture(textureType, textureId);
        GLES20.glTexParameterf(textureType, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(textureType, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(textureType, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(textureType, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        checkGlError("createTexture");
        return textureId;
    }

    public static void deleteTexture(int texture) {
        int[] textures = new int[1];
        textures[0] = texture;
        deleteTexture(textures);
    }

    public static void deleteTexture(int[] textures) {
        if (textures == null || textures.length == 0) {
            return;
        }
        GLES20.glDeleteTextures(textures.length, textures, 0);
    }

    private static final int SIZEOF_FLOAT = 4;

    public static final int CoordsPerVertex = 2;

    public static final float TEXTURE_NO_ROTATION[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };

    public static final float TEXTURE_ROTATED_90[] = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
    };
    public static final float TEXTURE_ROTATED_180[] = {
            1.0f, 0.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
    };
    public static final float TEXTURE_ROTATED_270[] = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };

    public static final float CUBE[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };

    /**
     * 创建FloatBuffer
     *
     * @param coords
     * @return
     */
    public static FloatBuffer createFloatBuffer(float[] coords) {
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * SIZEOF_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(coords);
        fb.position(0);
        return fb;
    }

    public static FloatBuffer createTextureBuffer(Rotation rotation) {
        switch (rotation) {
            case Rotation90:
                return createFloatBuffer(TEXTURE_ROTATED_90);
            case Rotation180:
                return createFloatBuffer(TEXTURE_ROTATED_180);
            case Rotation270:
                return createFloatBuffer(TEXTURE_ROTATED_270);
            case Rotation0:
            default:
                return createFloatBuffer(TEXTURE_NO_ROTATION);
        }
    }

    public static int getVertexCount() {
        return CUBE.length / CoordsPerVertex;
    }

    /**
     * 创建program
     */
    public static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        checkGlError("glCreateProgram");
        GLES20.glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);
        checkGlError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }

    /**
     * 加载Shader
     */
    public static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    /**
     * 创建Sampler2D的Framebuffer 和 Texture
     */
    public static void createFrameBuffer(int[] frameBuffer, int[] frameBufferTexture,
                                         int width, int height) {
        GLES20.glGenFramebuffers(frameBuffer.length, frameBuffer, 0);
        GLES20.glGenTextures(frameBufferTexture.length, frameBufferTexture, 0);
        for (int i = 0; i < frameBufferTexture.length; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameBufferTexture[i]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[i]);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, frameBufferTexture[i], 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        }
        checkGlError("createFrameBuffer");
    }

    /**
     * 从Assets文件夹中读取shader字符串
     *
     * @param path shader相对路径
     */
    public static String getShaderFromAssets(Context context, String path) {
        InputStream inputStream = null;
        try {
            inputStream = context.getResources().getAssets().open(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getShaderStringFromStream(inputStream);
    }

    /**
     * 从输入流中读取shader字符创
     */
    private static String getShaderStringFromStream(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            reader.close();
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static float[] getOriginalMatrix(){
        return new float[]{
                1,0,0,0,
                0,1,0,0,
                0,0,1,0,
                0,0,0,1
        };
    }

    public static void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            throw new RuntimeException(op + ": glError 0x" + Integer.toHexString(error));
        }
    }
}
