package com.wantee.render.filter;

import android.graphics.Rect;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.wantee.common.log.Log;
import com.wantee.render.Rotation;
import com.wantee.render.RenderMode;
import com.wantee.render.utils.OpenGLUtils;

import java.nio.FloatBuffer;

public class DisplayDrawer {
    protected RenderMode mRenderMode = RenderMode.FitCenter;
    protected int mDisplayWidth;
    protected int mDisplayHeight;
    protected FloatBuffer mVertexBuffer;
    protected FloatBuffer mTextureBuffer;
    protected Rotation mRotation = Rotation.Rotation0;
    protected String mVertexShader;
    protected String mFragmentShader;

    protected int mProgramHandle = -1;
    protected int mPositionHandle = -1;
    protected int mTextureCoordinateHandle = -1;
    protected int mInputTextureHandle = -1;
    protected int mTransformMatrixHandle;
    private float[] mTransformMatrix = OpenGLUtils.getOriginalMatrix();
    public DisplayDrawer(String vertexShader, String fragmentShader) {
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;
    }

    public boolean init() {
        if (!isInitialized()) {
            mProgramHandle = OpenGLUtils.createProgram(mVertexShader, mFragmentShader);
            mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");

            mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
            mInputTextureHandle = GLES20.glGetUniformLocation(mProgramHandle, "inputTexture");
            mTransformMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "transformMatrix");
            return true;
        }
        return false;
    }

    public boolean isInitialized() {
        return mProgramHandle >= 0;
    }

    public void setDisplayDegree(Rotation rotation) {
        mRotation = rotation;
        mTextureBuffer = OpenGLUtils.createTextureBuffer(rotation);
    }

    public boolean setDisplaySize(int width, int height) {
        if (width != mDisplayWidth || height != mDisplayHeight) {
            mDisplayWidth = width;
            mDisplayHeight = height;
            return true;
        }
        return false;
    }

    public void setRenderMode(RenderMode mode) {
        mRenderMode = mode;
    }

    public void drawFrame(int texId, int width, int height) {
        if (!isInitialized()) {
            Log.e("DisplayDrawer", "drawFrame when not initialized");
            return;
        }
        Rect rect = mRenderMode.layout(width, height, mDisplayWidth, mDisplayHeight);
        GLES20.glViewport(rect.left, rect.top, rect.width(), rect.height());
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        // 使用当前的program
        GLES20.glUseProgram(mProgramHandle);

        onDraw(texId);
    }

    public void setTransformMatrix(float[] transformMatrix) {
        mTransformMatrix = transformMatrix;
    }

    public void release() {
        if (isInitialized()) {
            GLES20.glDeleteProgram(mProgramHandle);
            mProgramHandle = -1;
        }
    }

    protected void onDraw(int texId) {
        if (mPositionHandle == -1) {
            Log.e("DisplayDrawer", "onDraw when not initialized");
            return;
        }
        float[] transformMatrix = mTransformMatrix;
        if (transformMatrix != null) {
            GLES20.glUniformMatrix4fv(mTransformMatrixHandle, 1, false, transformMatrix, 0);
        }
        // 绑定顶点坐标缓冲
        getVertexBuffer().position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, OpenGLUtils.CoordsPerVertex,
                GLES20.GL_FLOAT, false, 0, getVertexBuffer());
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // 绑定纹理坐标缓冲
        getTextureBuffer().position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2,
                GLES20.GL_FLOAT, false, 0, getTextureBuffer());
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        // 绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(getTextureType(), texId);
        GLES20.glUniform1i(mInputTextureHandle, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, OpenGLUtils.getVertexCount());
        // 解绑
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
        GLES20.glBindTexture(getTextureType(), 0);
        GLES20.glUseProgram(0);
    }

    protected FloatBuffer getVertexBuffer() {
        if (mVertexBuffer == null) {
            mVertexBuffer = OpenGLUtils.createFloatBuffer(OpenGLUtils.CUBE);
        }
        return mVertexBuffer;
    }

    protected FloatBuffer getTextureBuffer() {
        if (mTextureBuffer == null) {
            mTextureBuffer = OpenGLUtils.createTextureBuffer(mRotation);
        }
        return mTextureBuffer;
    }

    protected int getTextureType() {
        return GLES20.GL_TEXTURE_2D;
    }

}
