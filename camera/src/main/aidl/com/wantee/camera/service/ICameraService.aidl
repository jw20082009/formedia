package com.wantee.camera.service;
import com.wantee.camera.service.ICameraListener;
interface ICameraService {
    int open(int deviceType, int width, int height, String captureRequest, ICameraListener listener);
    int close();
    boolean isOpened();
}