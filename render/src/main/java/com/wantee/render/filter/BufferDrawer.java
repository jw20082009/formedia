package com.wantee.render.filter;

import android.graphics.Rect;
import android.opengl.GLES20;

import com.wantee.render.utils.OpenGLUtils;

public abstract class BufferDrawer extends DisplayDrawer {
    // FBO
    protected int[] mFrameBuffers;
    protected int[] mFrameBufferTextures;

    public BufferDrawer(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    @Override
    public boolean setDisplaySize(int width, int height) {
        if (super.setDisplaySize(width, height)) {
            createFrameBuffer();
            return true;
        }
        return false;
    }

    private void createFrameBuffer() {
        if (!isInitialized()) {
            return;
        }
        if (mFrameBuffers != null) {
            destroyFrameBuffer();
        }
        mFrameBuffers = new int[1];
        mFrameBufferTextures = new int[1];
        OpenGLUtils.createFrameBuffer(mFrameBuffers, mFrameBufferTextures, mDisplayWidth, mDisplayHeight);
        GLES20.glClearColor(0f, 0f, 0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    private void destroyFrameBuffer() {
        if (!isInitialized()) {
            return;
        }
        if (mFrameBufferTextures != null) {
            GLES20.glDeleteTextures(1, mFrameBufferTextures, 0);
            mFrameBufferTextures = null;
        }
        if (mFrameBuffers != null) {
            GLES20.glDeleteFramebuffers(1, mFrameBuffers, 0);
            mFrameBuffers = null;
        }
    }

    public int drawFrameBuffer(int texId, int width, int height) {
        if (!isInitialized()) {
            return -1;
        }

        Rect rect = mRenderMode.layout(width, height, mDisplayWidth, mDisplayHeight);
        GLES20.glViewport(rect.left, rect.top, rect.width(), rect.height());
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        // 使用当前的program
        GLES20.glUseProgram(mProgramHandle);
        onDraw(texId);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return mFrameBufferTextures[0];
    }

    @Override
    protected abstract int getTextureType();

    @Override
    public void release() {
        super.release();
        destroyFrameBuffer();
    }
}
