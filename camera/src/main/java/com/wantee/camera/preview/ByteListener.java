package com.wantee.camera.preview;

import android.view.Surface;

public class ByteListener extends SimpleListener<byte[]>{
    public ByteListener(CameraStatusListener statusListener) {
        super(statusListener);
    }

    @Override
    public Class<byte[]> getDestinationClass() {
        return byte[].class;
    }

    @Override
    public byte[] onCreateDestination(int previewWidth, int previewHeight) {
        return new byte[previewHeight * previewWidth * 3 / 2];
    }

    @Override
    public Surface onCreateSurface(int previewWidth, int previewHeight) {
        throw new RuntimeException("unsupported onCreateSurface when use ByteListener");
    }
}
