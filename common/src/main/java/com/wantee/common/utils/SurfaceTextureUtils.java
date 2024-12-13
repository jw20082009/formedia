package com.wantee.common.utils;

import android.graphics.SurfaceTexture;
import android.os.Handler;

import com.wantee.common.log.Log;

/**
 * SurfaceTexture使用全局单例管理，避免生产端和消费端状态不一致，导致一端销毁了SurfaceTexture，另一端还在使用的情况
 */
public enum SurfaceTextureUtils {
    Camera, Decode, Encode; /**枚举值预定义了SurfaceTexture将被使用到的场景，原则上同一场景中不应该存在多个SurfaceTexture实例*/

    private final String TAG = "SurfaceTextureUtils";
    private SurfaceTexture mSurfaceTexture;
    private final Object mLock = new Object();
    private long mAttachedThreadId = -1;

    public SurfaceTexture create(int width, int height) {
        return create(width, height, null, null);
    }

    public SurfaceTexture create(int width, int height, SurfaceTexture.OnFrameAvailableListener listener, Handler handler) {
        synchronized (mLock) {
            if (mSurfaceTexture != null) {
                mSurfaceTexture.release();
            }
            mSurfaceTexture = new SurfaceTexture(-1);
            mSurfaceTexture.setDefaultBufferSize(width, height);
            mAttachedThreadId = -1;
            if (listener != null) {
                mSurfaceTexture.setOnFrameAvailableListener(listener, handler);
            }
            mLock.notifyAll();
            return mSurfaceTexture;
        }
    }

    public void attachToGLContext(int texId) {
        synchronized (mLock) {
            if (mSurfaceTexture != null) {
                long threadId = Thread.currentThread().getId();
                if (threadId != mAttachedThreadId) {
                    if (mAttachedThreadId != -1) {
                        mSurfaceTexture.detachFromGLContext();
                    }
                    mAttachedThreadId = threadId;
                    Log.e(TAG, "[" + name() + "], attachToGLContext:" + texId + ", threadId:" + threadId);
                    mSurfaceTexture.attachToGLContext(texId);
                }
            }
        }
    }

    public void detachFromGLContext() {
        synchronized (mLock) {
            if (mSurfaceTexture != null) {
                Log.e(TAG, "[" + name() + "], detachFromGLContext");
                mSurfaceTexture.detachFromGLContext();
                mAttachedThreadId = -1;
            }
        }
    }

    public void setDefaultBufferSize(int width, int height) {
        synchronized (mLock) {
            if (mSurfaceTexture != null) {
                mSurfaceTexture.setDefaultBufferSize(width, height);
            }
        }
    }

    public void updateTexImage() {
        synchronized (mLock) {
            if (mSurfaceTexture != null) {
                mSurfaceTexture.updateTexImage();
            }
        }
    }

    public void getTransformMatrix(float[] mtx) {
        synchronized (mLock) {
            if (mSurfaceTexture != null) {
                mSurfaceTexture.getTransformMatrix(mtx);
            }
        }
    }

    public SurfaceTexture getOrCreate(int width, int height) {
        synchronized (mLock) {
            if (mSurfaceTexture == null) {
                mSurfaceTexture = new SurfaceTexture(-1);
                mSurfaceTexture.detachFromGLContext();
                mAttachedThreadId = -1;
            }
            mSurfaceTexture.setDefaultBufferSize(width, height);
            return mSurfaceTexture;
         }
    }

    public SurfaceTexture get() {
        try {
            return get(0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public SurfaceTexture get(long timeMilliSec) throws InterruptedException {
        synchronized (mLock) {
            if (mSurfaceTexture == null) {
                if (timeMilliSec < 0) {
                    mLock.wait();
                } else if (timeMilliSec != 0) {
                    mLock.wait(timeMilliSec);
                }
            }
            return mSurfaceTexture;
        }
    }

    public void setOnFrameAvailableListener(SurfaceTexture.OnFrameAvailableListener listener, Handler handler) {
        synchronized (mLock) {
            if (mSurfaceTexture != null) {
                mSurfaceTexture.setOnFrameAvailableListener(listener, handler);
            }
        }
    }

    public void release() {
        synchronized (mLock) {
            if (mSurfaceTexture != null) {
                mSurfaceTexture.release();
                mSurfaceTexture = null;
            }
        }
    }
}
