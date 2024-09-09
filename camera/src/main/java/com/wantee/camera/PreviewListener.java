package com.wantee.camera;

import android.graphics.SurfaceTexture;
import android.view.Surface;

import com.wantee.common.log.Log;

import java.util.ArrayList;
import java.util.List;

public interface PreviewListener<T> extends CameraListener{
    Previewer<T> onStartPreview(int requestCode, EquipmentType equipmentType);
}


interface CameraListener {
    void onOpened(int requestCode, int displayRotate);
    void onClosed(int requestCode);
    void onError(int requestCode, String errorMessage);
}

class ListenerWrapper implements PreviewListener {
    private final String TAG = "ListenerWrapper";
    private final List<CameraListener> mListeners = new ArrayList<>();
    private PreviewListener<Surface> mSurfaceListener;
    private PreviewListener<SurfaceTexture> mSurfaceTextureListener;

    public void addCameraListener(CameraListener listener) {
        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }

    public void setSurfaceListener(PreviewListener<Surface> listener) {
        synchronized (mListeners) {
            mSurfaceListener = listener;
            addCameraListener(listener);
        }
    }

    public void setSurfaceTextureListener(PreviewListener<SurfaceTexture> listener) {
        synchronized (mListeners) {
            mSurfaceTextureListener = listener;
            addCameraListener(listener);
        }
    }

    @Override
    public Previewer<?> onStartPreview(int requestCode, EquipmentType equipmentType) {
        synchronized (mListeners) {
            if (equipmentType.previewClazz() == Surface.class && mSurfaceListener != null) {
                return mSurfaceListener.onStartPreview(requestCode, equipmentType);
            } else if (equipmentType.previewClazz() == SurfaceTexture.class && mSurfaceTextureListener != null) {
                return mSurfaceTextureListener.onStartPreview(requestCode, equipmentType);
            }
            Log.e(TAG, "onStartPreview unsupported equipmentType:" + equipmentType.name() + ", surfaceListener:" + mSurfaceListener + ", surfaceTextureListener:" + mSurfaceTextureListener);
            return null;
        }
    }

    @Override
    public void onOpened(int requestCode, int displayRotate) {
        synchronized (mListeners) {
            for (CameraListener listener: mListeners) {
                listener.onOpened(requestCode, displayRotate);
            }
        }
    }

    @Override
    public void onClosed(int requestCode) {
        synchronized (mListeners) {
            for (CameraListener listener: mListeners) {
                listener.onClosed(requestCode);
            }
        }
    }

    @Override
    public void onError(int requestCode, String errorMessage) {
        synchronized (mListeners) {
            for (CameraListener listener: mListeners) {
                listener.onError(requestCode, errorMessage);
            }
        }
    }
}

