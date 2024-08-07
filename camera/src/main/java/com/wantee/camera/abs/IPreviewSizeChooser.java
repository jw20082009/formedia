package com.wantee.camera.abs;

import android.util.Size;

public interface IPreviewSizeChooser {
    Size onSelectPreviewSize(Size[] supportedPreviewSizes);
}