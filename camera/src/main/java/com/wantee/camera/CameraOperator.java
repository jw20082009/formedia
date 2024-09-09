package com.wantee.camera;

import com.wantee.camera.device.Runtime;

public interface CameraOperator extends Runtime {
    int open(EquipmentType deviceType, int width, int height, String captureRequest, PreviewListener<?> listener);
    int close();
    boolean isOpened();
}


