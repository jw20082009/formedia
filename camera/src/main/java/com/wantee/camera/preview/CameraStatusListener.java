package com.wantee.camera.preview;

public interface CameraStatusListener {
    void onOpened(int requestId, int cameraDegree, int displayRotate, boolean isFacingFront);
    void onClosed(int requestId);
    void onError(int requestId, String errorMessage);
}
