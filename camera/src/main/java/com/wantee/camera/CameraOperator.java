package com.wantee.camera;

import com.wantee.camera.device.Runtime;
import com.wantee.camera.preview.CameraListener;

public interface CameraOperator extends Runtime {
    int open(EquipmentType deviceType, int width, int height, String captureRequest, CameraListener<?> listener);
    int setListener(CameraListener<?> listener);
    int close();
    boolean isOpened();
}


