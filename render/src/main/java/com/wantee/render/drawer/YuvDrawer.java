package com.wantee.render.drawer;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.wantee.common.Frame;
import com.wantee.render.Rotation;
import com.wantee.render.utils.OpenGLUtils;

import java.nio.ByteBuffer;

public class YuvDrawer extends BaseDrawer<ByteBuffer> {
    private static final String TAG = "YuvDrawer";
    // col major
    public static final float colorMatBt601Fullrange[] = {
            1.0f, 1.0f, 1.0f,
            0.0f, -0.344f, 1.772f,
            1.402f, -0.714f, 0.0f
    };

    public static final float colorMatBt709Fullrange[] = {
            1.0f, 1.0f, 1.0f,
            0.0f, -0.1873f, 1.8556f,
            1.5748f, -0.4681f, 0.0f,
    };

    public static final float colorMatBt601Videorange[] = {
            1.164f, 1.164f, 1.164f,
            0.0f, -0.392f, 2.017f,
            1.596f, -0.813f, 0.0f,
    };

    public static final float colorMatBt709Videorange[] = {
            1.1644f, 1.1644f, 1.1644f,
            0.0f, -0.2132f, 2.1124f,
            1.7927f, -0.5329f, 0.0f,
    };

    public static final float colorOffsetVideoRange[] = {
            -16.0f / 255.0f, -0.5f, -0.5f,
    };

    public static final float colorOffsetFullRange[] = {
            0.0f, -0.5f, -0.5f,
    };

    protected int mUniformYLocation;
    protected int mUniformULocation;
    protected int mUniformVLocation;
    protected int mUniformAlphaLoc;
    protected int mUniformColorMatLoc;
    protected int mUniformColorOffsetLoc;

    protected int[] mYuvTextures = { -1, -1, -1};
    protected float[] mColorOffset = colorOffsetFullRange;
    protected float[] mColorMat = colorMatBt601Fullrange;
    protected float mAlpha = 1.0f;

    private int mFrameWidth = 0;
    private int mFrameHeight = 0;
    private boolean mIsYuv420p = true;

    public YuvDrawer(Context context) {
        this(context, false);
    }

    public YuvDrawer(Context context, boolean needFrameBuffer) {
        super(OpenGLUtils.getShaderFromAssets(context, "shader/vertex_normal.glsl"),
                OpenGLUtils.getShaderFromAssets(context, "shader/fragment_yuv420p.glsl"), needFrameBuffer);
    }

    public YuvDrawer(String vertex, String fragment, boolean needFrameBuffer) {
        super(vertex, fragment, needFrameBuffer);
    }

    public void setColorSpace(float[] colorMatrix, float[] colorOffset) {
        if (colorMatrix == null || colorMatrix.length != colorMatBt601Fullrange.length ||
                colorOffset == null || colorOffset.length != colorOffsetFullRange.length) {
            return;
        }
        mColorMat = colorMatrix;
        mColorOffset = colorOffset;
    }

    @Override
    protected boolean onInit(int program) {
        mUniformYLocation = GLES20.glGetUniformLocation(program, "SamplerY");
        mUniformULocation = GLES20.glGetUniformLocation(program, "SamplerU");
        mUniformVLocation = GLES20.glGetUniformLocation(program, "SamplerV");
        mUniformAlphaLoc = GLES20.glGetUniformLocation(program, "alpha");
        mUniformColorMatLoc = GLES20.glGetUniformLocation(program, "colorCvtMat");
        mUniformColorOffsetLoc = GLES20.glGetUniformLocation(program, "colorOffset");
        return true;
    }

    public static void createTextures(int[] textures) {
        int num = textures.length;
        GLES20.glGenTextures(num, textures, 0);

        boolean genFail = false;
        for (int i = 0; i < num; i++) {
            if (textures[i] == 0) {
                Log.e(TAG, "[createTextures] fail for " + i);
                genFail = true;
            }
        }
        if (genFail) {
            final int error = GLES20.glGetError();
            Log.e(TAG, "[createTextures] fail for yuv");
        }

        for (int i = 0; i < num; i++) {
            int texture = textures[i];
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, GLES20.GL_TRUE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }
    }

    private boolean updateYUVTexture(Frame<ByteBuffer> frame) {
        ByteBuffer yuvData = frame.getData();
        int width = frame.getWidth();
        int height = frame.getHeight();
        if (mFrameWidth != width || mFrameHeight != height || mYuvTextures[0] == -1) {
            releaseTextures();
            createTextures(mYuvTextures);

            yuvData.position(0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYuvTextures[0]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, width, height, 0,
                    GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, yuvData);
            yuvData.position(width * height);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYuvTextures[1]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, mIsYuv420p ? GLES20.GL_LUMINANCE : GLES20.GL_LUMINANCE_ALPHA, width / 2, height / 2, 0,
                    mIsYuv420p ? GLES20.GL_LUMINANCE : GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, yuvData);
            if (mIsYuv420p) {
                yuvData.position(width * height * 5 / 4);
                GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYuvTextures[2]);
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, width / 2, height / 2, 0,
                        GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, yuvData);
            }
        } else {
            yuvData.position(0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYuvTextures[0]);
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, width, height, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, yuvData);
            yuvData.position(width * height);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYuvTextures[1]);
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, width / 2, height / 2, mIsYuv420p ? GLES20.GL_LUMINANCE : GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, yuvData);
            if (mIsYuv420p) {
                yuvData.position(width * height * 5 / 4);
                GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYuvTextures[2]);
                GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, width / 2, height / 2, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, yuvData);
            }
        }
        return true;
    }

    int i = 0;
    @Override
    protected Frame<Integer> onDraw(Frame<ByteBuffer> frame) {
        updateYUVTexture(frame);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYuvTextures[0]);
        GLES20.glUniform1i(mUniformYLocation, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYuvTextures[1]);
        GLES20.glUniform1i(mUniformULocation, 1);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYuvTextures[2]);
        GLES20.glUniform1i(mUniformVLocation, 2);
        GLES20.glUniform1f(mUniformAlphaLoc, mAlpha);
        GLES20.glUniform3fv(mUniformColorOffsetLoc, 1, mColorOffset, 0);
        GLES20.glUniformMatrix3fv(mUniformColorMatLoc, 1, false, mColorMat, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        int w = frame.getWidth();
        int h = frame.getHeight();
        if (mRotation == Rotation.Rotation90 || mRotation == Rotation.Rotation270) {
            w = frame.getHeight();
            h = frame.getWidth();
        }
        return new Frame<>((mFrameBufferTextures!= null && mFrameBufferTextures.length > 0) ? mFrameBufferTextures[0] : 0, w, h);
    }

    private void releaseTextures() {
        if (mYuvTextures[0] != -1) {
            GLES20.glDeleteTextures(mYuvTextures.length, mYuvTextures, 0);
        }
    }

    @Override
    public void release() {
        releaseTextures();
        super.release();
    }

    @Override
    public void reset() {
        super.reset();
        mYuvTextures[0] = -1;
        mYuvTextures[1] = -1;
        mYuvTextures[2] = -1;
    }
}
