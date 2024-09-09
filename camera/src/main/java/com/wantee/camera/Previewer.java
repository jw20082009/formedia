package com.wantee.camera;

import android.graphics.SurfaceTexture;
import android.util.Size;
import android.view.Surface;

public abstract class Previewer<T> {

    public abstract PreviewType type();

    public abstract T createDestination(int previewWidth, int previewHeight);
}
