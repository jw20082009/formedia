package com.wantee.camera.preview;

import android.media.Image;
import android.media.ImageReader;
import android.view.Surface;

public abstract class ImageReaderListener extends SimpleListener<ImageReader>{
    public ImageReader imageReader;

    public ImageReaderListener(CameraStatusListener statusListener) {
        super(statusListener);
    }

    @Override
    public Class<ImageReader> getDestinationClass() {
        return ImageReader.class;
    }

    @Override
    public Surface onCreateSurface(int previewWidth, int previewHeight) {
        imageReader = onCreateDestination(previewWidth, previewHeight);
        return imageReader.getSurface();
    }
}
