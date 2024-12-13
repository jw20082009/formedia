package com.wantee.camera.preview;

import android.view.Surface;

public interface CameraListener <T> extends CameraStatusListener{
    Class<T> getDestinationClass();
    T onCreateDestination(int previewWidth, int previewHeight);
    Surface onCreateSurface(int previewWidth, int previewHeight);
}
