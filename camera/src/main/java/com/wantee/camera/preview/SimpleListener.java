package com.wantee.camera.preview;

public abstract class SimpleListener<T> implements CameraListener<T> {

    CameraStatusListener mStatusListener;
    public SimpleListener(CameraStatusListener statusListener) {
        mStatusListener = statusListener;
    }

    @Override
    public void onOpened(int requestId, int cameraDegree, int displayRotate, boolean isFacingFront) {
        if (mStatusListener != null) {
            mStatusListener.onOpened(requestId, cameraDegree, displayRotate, isFacingFront);
        }
    }

    @Override
    public void onClosed(int requestId) {
        if (mStatusListener != null) {
            mStatusListener.onClosed(requestId);
        }
    }

    @Override
    public void onError(int requestId, String errorMessage) {
        if (mStatusListener != null) {
            mStatusListener.onError(requestId, errorMessage);
        }
    }

}
