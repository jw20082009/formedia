package com.wantee.render.drawer;

import android.graphics.Rect;
import android.opengl.GLES20;

import com.wantee.common.Frame;
import com.wantee.common.log.Log;
import com.wantee.render.RenderMode;
import com.wantee.render.Rotation;
import com.wantee.render.utils.OpenGLUtils;

import java.nio.FloatBuffer;

public abstract class BaseDrawer<T> {
    private final String TAG = "BaseDrawer";
    protected int mProgramHandle = -1;
    protected int mPositionHandle = -1;
    protected int mTextureCoordinateHandle = -1;
    protected String mVertexShader;
    protected String mFragmentShader;

    protected FloatBuffer mVertexBuffer;
    protected FloatBuffer mTextureBuffer;
    protected Rotation mRotation = Rotation.Rotation0;
    protected boolean mFlipVertical = false;
    protected boolean mFlipHorizontal = false;

    protected int mDisplayWidth;
    protected int mDisplayHeight;
    protected RenderMode mRenderMode = RenderMode.FitCenter;

    protected int[] mFrameBuffers;
    protected int[] mFrameBufferTextures;
    protected boolean mDisplaySizeChanged = true;
    private boolean mNeedFrameBuffer = false;

    public BaseDrawer(String vertexShader, String fragmentShader, boolean needFrameBuffer) {
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;
        mNeedFrameBuffer = needFrameBuffer;
    }

    public BaseDrawer(String vertexShader, String fragmentShader) {
        this(vertexShader, fragmentShader, false);
    }

    public boolean init() {
        if (!isInitialized()) {
            mProgramHandle = OpenGLUtils.createProgram(mVertexShader, mFragmentShader);
            mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
            mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
            return onInit(mProgramHandle);
        }
        return false;
    }

    protected abstract boolean onInit(int program);

    public boolean isInitialized() {
        return mProgramHandle >= 0;
    }

    public void setDisplayDegree(Rotation rotation, final boolean flipHorizontal,
                                 final boolean flipVertical) {
        if (mRotation != rotation || mFlipVertical != flipVertical || mFlipHorizontal != flipHorizontal || mTextureBuffer == null) {
            mRotation = rotation;
            mFlipVertical = flipVertical;
            mFlipHorizontal = flipHorizontal;
            mTextureBuffer = OpenGLUtils.createRotationTextureBuffer(rotation, flipHorizontal, flipVertical);
        }
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

    public boolean setDisplaySize(int width, int height) {
        if (width != mDisplayWidth || height != mDisplayHeight) {
            mDisplayWidth = width;
            mDisplayHeight = height;
            mDisplaySizeChanged = true;
            return true;
        }
        return false;
    }

    public void setRenderMode(RenderMode mode) {
        mRenderMode = mode;
    }

    public Frame<Integer> drawFrame(Frame<T> frame) {
        return drawFrame(frame, null);
    }

    public Frame<Integer> drawFrame(Frame<T> frame, Runnable runnable) {
        if (!isInitialized() || frame == null || frame.getData() == null) {
            Log.e(TAG, "drawFrame failed, program:" + mProgramHandle+ ", frame:" + frame + ", frameData:" + (frame == null? "unknown": frame.getData()));
            return null;
        }
        if (mDisplaySizeChanged) {
            onDisplaySizeChanged(mDisplayWidth, mDisplayHeight);
            mDisplaySizeChanged = false;
        }
        Rect rect = mRenderMode.layout(frame.getWidth(), frame.getHeight(), mDisplayWidth, mDisplayHeight);
        GLES20.glViewport(rect.left, rect.top, rect.width(), rect.height());
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, getFrameBuffer());
        // 使用当前的program
        GLES20.glUseProgram(mProgramHandle);
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
        Frame<Integer> resultFrame = onDraw(frame);
        if (runnable != null) {
            runnable.run();
        }
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
        GLES20.glUseProgram(0);
        return resultFrame;
    }

    protected abstract Frame<Integer> onDraw(Frame<T> frame);

    protected void onDisplaySizeChanged(int width, int height) {
        if (mNeedFrameBuffer) {
            createFrameBuffer(width, height);
        }
    }

    protected int getFrameBuffer() {
        return (mFrameBuffers!= null && mFrameBuffers.length > 0) ? mFrameBuffers[0] : 0;
    }

    public void release() {
        if (isInitialized()) {
            GLES20.glDeleteProgram(mProgramHandle);
        }
        destroyFrameBuffer();
        reset();
    }

    public void reset() {
        mProgramHandle = -1;
        resetFrameBuffers();
    }

    private void createFrameBuffer(int width, int height) {
        if (!isInitialized()) {
            return;
        }
        if (mFrameBuffers != null) {
            destroyFrameBuffer();
        }
        mFrameBuffers = new int[1];
        mFrameBufferTextures = new int[1];
        OpenGLUtils.createFrameBuffer(mFrameBuffers, mFrameBufferTextures, width, height);
        GLES20.glClearColor(0f, 0f, 0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    private void destroyFrameBuffer() {
        if (!isInitialized()) {
            return;
        }
        if (mFrameBufferTextures != null) {
            GLES20.glDeleteTextures(1, mFrameBufferTextures, 0);
        }
        if (mFrameBuffers != null) {
            GLES20.glDeleteFramebuffers(1, mFrameBuffers, 0);
        }
        resetFrameBuffers();
    }

    public void resetFrameBuffers() {
        mFrameBufferTextures = null;
        mFrameBuffers = null;
    }
}
