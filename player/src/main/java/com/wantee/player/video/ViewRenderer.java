package com.wantee.player.video;

import android.opengl.GLSurfaceView;

import com.wantee.common.log.Log;
import com.wantee.player.video.RenderSource.Source;
import com.wantee.common.Frame;
import com.wantee.render.RenderMode;
import com.wantee.render.drawer.TransformDrawer;

import java.util.LinkedList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ViewRenderer<T> implements GLSurfaceView.Renderer, RenderSource.ImageListener<T> {
    private static final String TAG = "ViewRenderer";
    private final LinkedList<TransformDrawer> mDrawers = new LinkedList<>();
    private GLSurfaceView mSurfaceView;
    private final Source<T> mSource;
    private boolean mFirstFrame = false;
    private int mSurfaceWidth = 0;
    private int mSurfaceHeight = 0;

    public ViewRenderer(Source<T> source) {
        mSource = source;
        if (mSource != null) {
            mSource.setImageListener(this);
        }
    }

    public synchronized void setView(GLSurfaceView view) {
        if (view != mSurfaceView) {
            mSurfaceView = view;
            mSurfaceView.setEGLContextClientVersion(2);
            mSurfaceView.setRenderer(this);
            mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }
    }

    public synchronized void onResume() {
        if (mSurfaceView != null) {
            Log.e(TAG, "onResume");
            mSurfaceView.onResume();
        }
    }

    public synchronized void onPause() {
        if (mSurfaceView != null) {
            Log.e(TAG, "onPause");
            mSurfaceView.queueEvent(() -> {
                if (mSource != null) {
                    mSource.releaseRender();
                }
                for (TransformDrawer drawer: mDrawers) {
                    drawer.release();
                }
            });
            mSurfaceView.onPause();
        }
    }

    public synchronized T onSizeChanged(int width, int height) {
        if (mSource != null) {
            mFirstFrame = true;
            return mSource.onSizeChanged(width, height);
        }
        return null;
    }

    public synchronized void setDisplayDegree(int degree, boolean flipHorizontal, boolean flipVertical) {
        if (mSource != null) {
            mSource.setDisplayDegree(degree, flipHorizontal, flipVertical);
        }
    }

    public synchronized void releaseSource() {
        Log.e(TAG, "releaseSource:" + mSource);
        if (mSource != null) {
            mSource.releaseSource();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.e(TAG, "onSurfaceCreated");
        if (mSource != null && mSurfaceView != null) {
            mSource.initRender(mSurfaceView.getContext());
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.e(TAG, "onSurfaceChanged:" + width + "*" + height);
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        if (mSurfaceView == null) {
            return;
        }
        if (mDrawers.isEmpty()) {
            mDrawers.add(new TransformDrawer(mSurfaceView.getContext()));
        }
        for(TransformDrawer drawer: mDrawers) {
            drawer.reset();
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Frame<Integer> sourceTex = null;
        if (mSource != null) {
            sourceTex = mSource.onRenderTexture();
        }
        if (sourceTex == null) {
            return;
        }
        for (TransformDrawer drawer: mDrawers) {
            drawer.init();
            drawer.setDisplaySize(mSurfaceWidth, mSurfaceHeight);
            drawer.setRenderMode(RenderMode.CenterCrop);
            drawer.drawFrame(sourceTex);
        }
    }

    @Override
    public void onImageAvailable(T imageWrapper) {
        if (mFirstFrame) {
            Log.e(TAG, "onImageAvailable firstFrame");
            mFirstFrame = false;
        }
        if (mSurfaceView != null) {
            mSurfaceView.requestRender();
        }
    }
}
