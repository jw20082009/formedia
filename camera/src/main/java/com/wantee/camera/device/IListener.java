package com.wantee.camera.device;

import android.util.Size;

import java.util.List;

public interface IListener {
    Size onSelectPreviewSize(List<Size> size);
    void onOpened(String deviceId, int width, int height);
    void onClosed();
}
