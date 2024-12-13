package com.wantee.camera.preview;

import android.graphics.SurfaceTexture;
import android.view.Surface;

public abstract class SurfaceTextureListener extends SimpleListener<SurfaceTexture> {

    public int previewWidth;
    public int previewHeight;
    public SurfaceTexture surfaceTexture;

    public SurfaceTextureListener(CameraStatusListener statusListener) {
        super(statusListener);
    }

    @Override
    public Class<SurfaceTexture> getDestinationClass() {
        return SurfaceTexture.class;
    }

    @Override
    public Surface onCreateSurface(int previewWidth, int previewHeight) {
        return new Surface(onCreateDestination(previewWidth, previewHeight));
    }
}
