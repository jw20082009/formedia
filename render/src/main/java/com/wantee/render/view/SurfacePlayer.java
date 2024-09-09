package com.wantee.render.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.Surface;

import com.wantee.common.handler.CommonHandler;
import com.wantee.common.log.Log;
import com.wantee.render.RenderMode;
import com.wantee.render.Rotation;
import com.wantee.render.filter.BufferDrawer;
import com.wantee.render.filter.DisplayDrawer;
import com.wantee.render.utils.OpenGLUtils;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SurfacePlayer {
    private static final String TAG = "GLSurfaceViewHelper";
    private GLSurfaceView mSurfaceView;
    private SurfaceTexture mSurfaceTexture;
    private int mOESTextureId = -1;
    private final Semaphore mPermit = new Semaphore(0);
    protected final float[] mSTMatrix = new float[16];
    private BufferDrawer mOesDrawer;
    private DisplayDrawer mDrawer;
    private int mDataWidth = 0;
    private int mDataHeight = 0;
    private int mSurfaceWidth = 0;
    private int mSurfaceHeight = 0;

    public void setView(GLSurfaceView view) {
        if (view != mSurfaceView) {
            mSurfaceView = view;
            mSurfaceView.setEGLContextClientVersion(2);
            mSurfaceView.setRenderer(mRender);
            mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }
        Context context = view.getContext();
        mDrawer = new DisplayDrawer(OpenGLUtils.getShaderFromAssets(context, "shader/vertex_oes_input.glsl"),
                OpenGLUtils.getShaderFromAssets(context, "shader/fragment_normal.glsl"));
        mOesDrawer = new BufferDrawer(OpenGLUtils.getShaderFromAssets(context, "shader/vertex_oes_input.glsl"),
                OpenGLUtils.getShaderFromAssets(context, "shader/fragment_oes_input.glsl")) {
            @Override
            protected int getTextureType() {
                return GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
            }
        };
    }

    public void onResume() {
        if (mSurfaceView != null) {
            Log.e(TAG, "onResume");
            mSurfaceView.onResume();
        }
    }

    public void onPause() {
        if (mSurfaceView != null) {
            Log.e(TAG, "onPause");
            mSurfaceView.onPause();
        }
    }

    public Surface tryObtainSurface(int width, int height) throws InterruptedException {
        SurfaceTexture surfaceTexture = tryObtainSurfaceTexture(width, height);
        return surfaceTexture == null? null: new Surface(surfaceTexture);
    }

    public SurfaceTexture tryObtainSurfaceTexture(int width, int height) throws InterruptedException {
        if (mSurfaceTexture == null) {
            boolean acquired = mPermit.tryAcquire(1000, TimeUnit.MILLISECONDS);
            if (!acquired) {
                Log.e(TAG, "tryObtainSurface tryAcquire timeout");
            }
        }
        if (mSurfaceTexture != null) {
            mDataWidth = height;
            mDataHeight = width;
            mSurfaceTexture.setDefaultBufferSize(width, height);
            mSurfaceTexture.setOnFrameAvailableListener(mFrameAvailableListener, CommonHandler.instance().handler());
            return mSurfaceTexture;
        }
        Log.e(TAG, "tryObtainSurfaceTexture null");
        return null;
    }

    final GLSurfaceView.Renderer mRender = new GLSurfaceView.Renderer() {
        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
            Log.e(TAG, "onSurfaceCreated");
            if (mOESTextureId != -1) {
                OpenGLUtils.deleteTexture(mOESTextureId);
            }
            mOESTextureId = OpenGLUtils.createOESTexture();
            if (mSurfaceTexture != null) {
                mSurfaceTexture.release();
            }
            mSurfaceTexture = new SurfaceTexture(mOESTextureId);
            mPermit.release();
        }

        @Override
        public void onSurfaceChanged(GL10 gl10, int w, int h) {
            Log.e(TAG, "onSurfaceChanged:" + w + "*" + h);
            mSurfaceWidth = w;
            mSurfaceHeight = h;
        }

        @Override
        public void onDrawFrame(GL10 gl10) {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mSTMatrix);
            int texId = -1;
            if (mOesDrawer != null && mDataWidth > 0 && mDataHeight > 0) {
                mOesDrawer.init();
                mOesDrawer.setDisplaySize(mDataWidth, mDataHeight);
                mOesDrawer.setTransformMatrix(mSTMatrix);
                texId = mOesDrawer.drawFrameBuffer(mOESTextureId, mDataWidth, mDataHeight);
            }
            if (mDrawer != null) {
                mDrawer.init();
                mDrawer.setDisplaySize(mSurfaceWidth, mSurfaceHeight);
                mDrawer.setRenderMode(RenderMode.CenterCrop);
                mDrawer.drawFrame(texId, mDataWidth, mDataHeight);
            }
        }
    };

    SurfaceTexture.OnFrameAvailableListener mFrameAvailableListener = surfaceTexture -> {
        if (mSurfaceView != null) {
            mSurfaceView.requestRender();
        }
    };
}
