package com.wantee.camera.service;

interface ICameraService {
    int open(int type, in Surface surface, String captureRequest);
    int close();
}